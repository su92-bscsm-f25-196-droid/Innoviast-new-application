package com.example.ui

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.utils.DateUtils
import com.example.utils.ImportExportHelper
import com.example.utils.SecurityUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(
    private val repository: TaskRepository,
    private val sharedPrefs: SharedPreferences
) : ViewModel() {

    // Current User Session State
    val currentUserId = MutableStateFlow("guest")
    val currentUser = MutableStateFlow<UserEntity?>(null)

    // Filter flows
    val searchQuery = MutableStateFlow("")
    val selectedFilterCategory = MutableStateFlow<String?>(null)
    val selectedFilterPriority = MutableStateFlow<String?>(null)
    val selectedFilterStatus = MutableStateFlow<String?>("Pending") // default to Pending
    val selectedDateRange = MutableStateFlow<String?>(null) // Today, Tomorrow, This Week, Overdue
    val selectedFilterFavorite = MutableStateFlow<Boolean?>(null)
    val selectedFilterPinned = MutableStateFlow<Boolean?>(null)
    val sortBy = MutableStateFlow("Due Date") // Newest, Oldest, Priority, Due Date, Alphabetical

    // Dynamically switch task streams based on active user
    val allTasks: Flow<List<TaskWithSubtasks>> = currentUserId.flatMapLatest { userId ->
        repository.getAllTasksFlow(userId)
    }

    val archivedTasks: Flow<List<TaskWithSubtasks>> = currentUserId.flatMapLatest { userId ->
        repository.getArchivedTasksFlow(userId)
    }

    // Combined filtered stream for the MAIN UI
    data class TaskFilters(
        val query: String = "",
        val category: String? = null,
        val priority: String? = null,
        val status: String? = "Pending",
        val dateRange: String? = null,
        val favorite: Boolean? = null,
        val pinned: Boolean? = null,
        val sortBy: String = "Due Date"
    )

    data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    private val filtersFlow: Flow<TaskFilters> = combine(
        combine(searchQuery, selectedFilterCategory, selectedFilterPriority, selectedFilterStatus) { query, cat, prio, status ->
            Quad(query, cat, prio, status)
        },
        combine(selectedDateRange, selectedFilterFavorite, selectedFilterPinned, sortBy) { dateRange, fav, pin, sort ->
            Quad(dateRange, fav, pin, sort)
        }
    ) { firstHalf, secondHalf ->
        TaskFilters(
            query = firstHalf.first,
            category = firstHalf.second,
            priority = firstHalf.third,
            status = firstHalf.fourth,
            dateRange = secondHalf.first,
            favorite = secondHalf.second,
            pinned = secondHalf.third,
            sortBy = secondHalf.fourth
        )
    }

    val uiTasks: StateFlow<List<TaskWithSubtasks>> = allTasks.combine(filtersFlow) { tasks, filters ->
        var filteredList = tasks

        // Status Filter
        filteredList = when (filters.status) {
            "Pending" -> filteredList.filter { !it.task.isCompleted && it.task.status != "Archived" }
            "Completed" -> filteredList.filter { it.task.isCompleted && it.task.status != "Archived" }
            "All" -> filteredList.filter { it.task.status != "Archived" }
            else -> filteredList.filter { it.task.status != "Archived" }
        }

        // Search Filter
        if (filters.query.isNotEmpty()) {
            val q = filters.query
            filteredList = filteredList.filter { item ->
                item.task.title.contains(q, ignoreCase = true) ||
                        item.task.category.contains(q, ignoreCase = true) ||
                        item.task.description.contains(q, ignoreCase = true) ||
                        item.task.notes.contains(q, ignoreCase = true) ||
                        item.subtasks.any { it.title.contains(q, ignoreCase = true) }
            }
        }

        // Category Filter
        if (filters.category != null) {
            filteredList = filteredList.filter { it.task.category == filters.category }
        }

        // Priority Filter
        if (filters.priority != null) {
            filteredList = filteredList.filter { it.task.priority == filters.priority }
        }

        // Favorite Filter
        if (filters.favorite != null) {
            filteredList = filteredList.filter { it.task.isFavorite == filters.favorite }
        }

        // Pinned Filter
        if (filters.pinned != null) {
            filteredList = filteredList.filter { it.task.isPinned == filters.pinned }
        }

        // Date Range Filter
        if (filters.dateRange != null) {
            filteredList = filteredList.filter { item ->
                when (filters.dateRange) {
                    "Due Today" -> DateUtils.isToday(item.task.dueDate)
                    "Due Tomorrow" -> DateUtils.isTomorrow(item.task.dueDate)
                    "This Week" -> DateUtils.isThisWeek(item.task.dueDate)
                    "Overdue" -> DateUtils.isOverdue(item.task.dueDate, item.task.isCompleted)
                    else -> true
                }
            }
        }

        // Sorting
        filteredList = when (filters.sortBy) {
            "Newest" -> filteredList.sortedByDescending { it.task.createdAt }
            "Oldest" -> filteredList.sortedBy { it.task.createdAt }
            "Priority" -> filteredList.sortedBy {
                when (it.task.priority) {
                    "High" -> 1
                    "Medium" -> 2
                    "Low" -> 3
                    else -> 4
                }
            }
            "Due Date" -> filteredList.sortedWith(
                compareBy<TaskWithSubtasks> { it.task.dueDate == 0L }
                    .thenBy { it.task.dueDate }
                    .thenBy { it.task.dueTime }
            )
            "Alphabetical" -> filteredList.sortedBy { it.task.title.lowercase() }
            else -> filteredList
        }

        // Pinned tasks ALWAYS top
        filteredList.sortedByDescending { it.task.isPinned }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistics Stream (Dynamic based on ALL tasks in system, active + archived)
    val statisticsState: StateFlow<DashboardStats> = allTasks.map { list ->
        calculateStats(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    init {
        // Automatically check if a user was remembered on app startup
        autoLogin()
    }

    private fun calculateStats(list: List<TaskWithSubtasks>): DashboardStats {
        val activeList = list.filter { it.task.status != "Archived" }
        val total = activeList.size
        val completed = activeList.count { it.task.isCompleted }
        val pending = total - completed
        val overdue = activeList.count { DateUtils.isOverdue(it.task.dueDate, it.task.isCompleted) }
        val highPriority = activeList.count { it.task.priority == "High" }
        val pinned = activeList.count { it.task.isPinned }
        val favorites = activeList.count { it.task.isFavorite }
        val today = activeList.count { DateUtils.isToday(it.task.dueDate) }
        val tomorrow = activeList.count { DateUtils.isTomorrow(it.task.dueDate) }

        val percentage = if (total > 0) (completed.toFloat() / total * 100).toInt() else 0

        // Calculate productivity score
        var score = completed * 15 - overdue * 10
        if (score < 0) score = 0
        if (score > 100) score = 100
        if (total == 0) score = 100

        // Weekly completed stats: group completed in last 7 days by day of week
        val weekStats = IntArray(7) // Mon=0, Tue=1 ... Sun=6
        val calendar = Calendar.getInstance()
        activeList.filter { it.task.isCompleted }.forEach { item ->
            calendar.timeInMillis = item.task.updatedAt
            val day = calendar.get(Calendar.DAY_OF_WEEK)
            val index = when (day) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            weekStats[index]++
        }

        // Category stats mapping
        val categoryStats = mutableMapOf<String, Int>()
        activeList.forEach { item ->
            val cat = item.task.category
            categoryStats[cat] = (categoryStats[cat] ?: 0) + 1
        }

        return DashboardStats(
            totalTasks = total,
            completedTasks = completed,
            pendingTasks = pending,
            overdueTasks = overdue,
            highPriorityTasks = highPriority,
            pinnedTasks = pinned,
            favoriteTasks = favorites,
            todayTasks = today,
            tomorrowTasks = tomorrow,
            completionPercentage = percentage,
            productivityScore = score,
            weeklyCompletedDistribution = weekStats.toList(),
            categoryTaskDistribution = categoryStats
        )
    }

    // Task operations
    fun createTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        dueDate: Long,
        dueTime: String,
        reminder: String,
        notes: String,
        subtasks: List<String>,
        recurrence: String = "None"
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                userId = currentUserId.value,
                title = title,
                description = description,
                category = category,
                priority = priority,
                dueDate = dueDate,
                dueTime = dueTime,
                reminder = reminder,
                status = "Pending",
                isCompleted = false,
                isPinned = false,
                isFavorite = false,
                recurrence = recurrence,
                notes = notes,
                sortOrder = 0
            )
            val subtaskEntities = subtasks.filter { it.trim().isNotEmpty() }.map {
                SubtaskEntity(taskId = 0, title = it, isCompleted = false)
            }
            repository.insertTaskWithSubtasks(task, subtaskEntities)
        }
    }

    fun updateTask(
        id: Int,
        title: String,
        description: String,
        category: String,
        priority: String,
        dueDate: Long,
        dueTime: String,
        reminder: String,
        notes: String,
        subtasks: List<SubtaskEntity>,
        recurrence: String = "None"
    ) {
        viewModelScope.launch {
            val existing = repository.getTaskById(id) ?: return@launch
            val updatedTask = existing.task.copy(
                userId = currentUserId.value,
                title = title,
                description = description,
                category = category,
                priority = priority,
                dueDate = dueDate,
                dueTime = dueTime,
                reminder = reminder,
                recurrence = recurrence,
                notes = notes,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateTaskWithSubtasks(updatedTask, subtasks)
        }
    }

    fun toggleTaskCompletion(taskWithSubtasks: TaskWithSubtasks) {
        viewModelScope.launch {
            val task = taskWithSubtasks.task
            val isNowCompleted = !task.isCompleted
            val newStatus = if (isNowCompleted) "Completed" else "Pending"

            val updatedTask = task.copy(
                isCompleted = isNowCompleted,
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateTask(updatedTask)

            // If task is being marked as completed AND it has recurrence, we spawn the next occurrence!
            if (isNowCompleted && task.recurrence != "None") {
                val nextDate = DateUtils.getNextOccurrence(task.dueDate, task.recurrence)
                if (nextDate != 0L) {
                    val nextTask = task.copy(
                        id = 0, // auto-generate
                        dueDate = nextDate,
                        status = "Pending",
                        isCompleted = false,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    // Reset subtask completions for the next occurrence
                    val resetSubtasks = taskWithSubtasks.subtasks.map {
                        SubtaskEntity(taskId = 0, title = it.title, isCompleted = false)
                    }
                    repository.insertTaskWithSubtasks(nextTask, resetSubtasks)
                }
            }
        }
    }

    fun toggleSubtaskCompletion(subtask: SubtaskEntity) {
        viewModelScope.launch {
            val updated = subtask.copy(isCompleted = !subtask.isCompleted)
            repository.updateSubtask(updated)
        }
    }

    fun togglePin(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isPinned = !task.isPinned))
        }
    }

    fun toggleFavorite(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isFavorite = !task.isFavorite))
        }
    }

    fun archiveTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = "Archived", updatedAt = System.currentTimeMillis()))
        }
    }

    fun restoreTask(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = if (task.isCompleted) "Completed" else "Pending"
            repository.updateTask(task.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteHardwareTask(id: Int) {
        viewModelScope.launch {
            repository.deleteHardwareTask(id)
        }
    }

    fun reorderTasks(tasks: List<TaskEntity>) {
        viewModelScope.launch {
            repository.updateTasksOrder(tasks)
        }
    }

    // Authentication & Profile management
    fun registerUser(
        fullName: String,
        username: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        dob: String = "",
        gender: String = "",
        bio: String = "",
        address: String = ""
    ): Flow<Result<UserEntity>> = flow {
        // Validation
        if (fullName.trim().isEmpty() || username.trim().isEmpty() || email.trim().isEmpty() || phone.trim().isEmpty() || password.isEmpty()) {
            emit(Result.failure(Exception("Please fill all required fields")))
            return@flow
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emit(Result.failure(Exception("Invalid email address")))
            return@flow
        }
        if (!phone.all { it.isDigit() }) {
            emit(Result.failure(Exception("Phone number must contain only numbers")))
            return@flow
        }
        if (password.length < 8 || !password.any { it.isUpperCase() } || !password.any { it.isLowerCase() } || !password.any { it.isDigit() } || !password.any { !it.isLetterOrDigit() }) {
            emit(Result.failure(Exception("Password must contain at least 8 characters, with uppercase, lowercase, numbers, and special characters")))
            return@flow
        }
        if (password != confirmPassword) {
            emit(Result.failure(Exception("Password mismatch")))
            return@flow
        }

        // Unique check
        val existingEmail = repository.getUserByEmail(email)
        if (existingEmail != null) {
            emit(Result.failure(Exception("Email already exists")))
            return@flow
        }

        val existingUsername = repository.getUserByUsername(username)
        if (existingUsername != null) {
            emit(Result.failure(Exception("Username already exists")))
            return@flow
        }

        // Create User Entity
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(password, salt)
        val user = UserEntity(
            id = email.lowercase(),
            fullName = fullName,
            username = username,
            email = email,
            phone = phone,
            passwordHash = hash,
            salt = salt,
            dob = dob,
            gender = gender,
            bio = bio,
            address = address
        )

        repository.insertUser(user)
        emit(Result.success(user))
    }

    fun loginUser(
        emailOrUsername: String,
        password: String,
        rememberMe: Boolean
    ): Flow<Result<UserEntity>> = flow {
        if (emailOrUsername.trim().isEmpty() || password.isEmpty()) {
            emit(Result.failure(Exception("Please fill all required fields")))
            return@flow
        }

        // Fetch User either by Email or Username
        val user = repository.getUserByEmail(emailOrUsername.lowercase())
            ?: repository.getUserByUsername(emailOrUsername)

        if (user == null) {
            emit(Result.failure(Exception("User not found")))
            return@flow
        }

        val isMatch = SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)
        if (isMatch) {
            currentUserId.value = user.id
            currentUser.value = user

            // Save Remember Me Session
            if (rememberMe) {
                sharedPrefs.edit()
                    .putString("logged_in_user_id", user.id)
                    .putBoolean("remember_me", true)
                    .apply()
            } else {
                sharedPrefs.edit()
                    .putString("logged_in_user_id", user.id) // Session kept, but not remembered permanently across full wipe
                    .putBoolean("remember_me", false)
                    .apply()
            }

            emit(Result.success(user))
        } else {
            emit(Result.failure(Exception("Incorrect password")))
        }
    }

    fun logout() {
        currentUserId.value = "guest"
        currentUser.value = null
        sharedPrefs.edit()
            .remove("logged_in_user_id")
            .remove("remember_me")
            .apply()
    }

    fun updateProfile(
        fullName: String,
        username: String,
        email: String,
        phone: String,
        dob: String,
        gender: String,
        bio: String,
        address: String,
        profilePicUri: String
    ): Flow<Result<UserEntity>> = flow {
        if (fullName.trim().isEmpty()) {
            emit(Result.failure(Exception("Name cannot be empty")))
            return@flow
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emit(Result.failure(Exception("Invalid email address")))
            return@flow
        }
        if (!phone.all { it.isDigit() }) {
            emit(Result.failure(Exception("Phone number must contain only numbers")))
            return@flow
        }

        val current = currentUser.value
        if (current == null) {
            emit(Result.failure(Exception("No user is logged in")))
            return@flow
        }

        // If username or email changed, verify uniqueness against other accounts
        if (username != current.username) {
            val exist = repository.getUserByUsername(username)
            if (exist != null && exist.id != current.id) {
                emit(Result.failure(Exception("Username already taken")))
                return@flow
            }
        }
        if (email.lowercase() != current.email.lowercase()) {
            val exist = repository.getUserByEmail(email.lowercase())
            if (exist != null && exist.id != current.id) {
                emit(Result.failure(Exception("Email already taken")))
                return@flow
            }
        }

        // Update User Entity
        val updatedUser = current.copy(
            fullName = fullName,
            username = username,
            email = email,
            phone = phone,
            dob = dob,
            gender = gender,
            bio = bio,
            address = address,
            profilePicUri = profilePicUri
        )

        repository.updateUser(updatedUser)
        currentUser.value = updatedUser
        emit(Result.success(updatedUser))
    }

    private fun autoLogin() {
        val remember = sharedPrefs.getBoolean("remember_me", false)
        val savedUserId = sharedPrefs.getString("logged_in_user_id", null)
        if (remember && savedUserId != null) {
            viewModelScope.launch {
                val user = repository.getUserById(savedUserId)
                if (user != null) {
                    currentUserId.value = user.id
                    currentUser.value = user
                }
            }
        }
    }

    // Export & Import
    fun exportTasks(): String {
        return ImportExportHelper.exportTasksToJson(uiTasks.value)
    }

    fun importTasks(jsonStr: String): Result<String> {
        return try {
            val parsed = ImportExportHelper.importTasksFromJson(jsonStr)
            viewModelScope.launch {
                parsed.forEach { (task, subs) ->
                    // Set correct user ID on imported tasks
                    repository.insertTaskWithSubtasks(task.copy(userId = currentUserId.value), subs)
                }
            }
            Result.success("Successfully imported ${parsed.size} tasks!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class TaskViewModelFactory(
    private val repository: TaskRepository,
    private val sharedPrefs: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository, sharedPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class DashboardStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val overdueTasks: Int = 0,
    val highPriorityTasks: Int = 0,
    val pinnedTasks: Int = 0,
    val favoriteTasks: Int = 0,
    val todayTasks: Int = 0,
    val tomorrowTasks: Int = 0,
    val completionPercentage: Int = 0,
    val productivityScore: Int = 0,
    val weeklyCompletedDistribution: List<Int> = List(7) { 0 },
    val categoryTaskDistribution: Map<String, Int> = emptyMap()
)
