package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.SubtaskEntity
import com.example.ui.TaskViewModel
import com.example.utils.DateUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    viewModel: TaskViewModel,
    taskId: Int? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isEditMode = taskId != null

    // Fetch existing task if in Edit Mode
    val tasks by viewModel.allTasks.collectAsState(initial = emptyList())
    val existingTask = remember(tasks, taskId) {
        tasks.find { it.task.id == taskId }
    }

    // Input States
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Personal") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var dueDate by remember { mutableStateOf(0L) }
    var dueTime by remember { mutableStateOf("") }
    var reminder by remember { mutableStateOf("No Reminder") }
    var notes by remember { mutableStateOf("") }
    var recurrence by remember { mutableStateOf("None") }

    // Subtask States
    var subtasksList = remember { mutableStateListOf<String>() }
    var newSubtaskText by remember { mutableStateOf("") }

    // Validation Errors
    var titleError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    // Dropdown Expanded states
    var categoryExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var reminderExpanded by remember { mutableStateOf(false) }
    var recurrenceExpanded by remember { mutableStateOf(false) }

    // Load existing task data if editing
    LaunchedEffect(existingTask) {
        if (isEditMode && existingTask != null) {
            val task = existingTask.task
            title = task.title
            description = task.description
            selectedCategory = task.category
            selectedPriority = task.priority
            dueDate = task.dueDate
            dueTime = task.dueTime
            reminder = task.reminder
            notes = task.notes
            recurrence = task.recurrence

            subtasksList.clear()
            existingTask.subtasks.forEach {
                subtasksList.add(it.title)
            }
        }
    }

    val categories = listOf(
        "Personal",
        "Family",
        "Office",
        "Study",
        "Team",
        "Meeting",
        "Event",
        "Shopping",
        "Health",
        "Travel",
        "Business",
        "Bills",
        "Appointment",
        "Custom"
    )
    val priorities = listOf("Low", "Medium", "High")
    val reminders = listOf("No Reminder", "15 Minutes Before", "30 Minutes Before", "1 Hour Before", "1 Day Before")
    val recurrences = listOf("None", "Daily", "Weekdays", "Weekly", "Monthly", "Yearly")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Task" else "Create Task", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.trim().isNotEmpty()) titleError = null
                },
                label = { Text("Task Title *") },
                placeholder = { Text("What needs to be done?") },
                isError = titleError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input"),
                singleLine = true
            )
            titleError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Add more details about this task...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            // Category & Priority Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Selector Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            Icon(
                                imageVector = if (categoryExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Category dropdown"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    // Transparent overlay to catch click
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { categoryExpanded = true }
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.45f)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Priority Selector Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedPriority,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = {
                            Icon(
                                imageVector = if (priorityExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Priority dropdown"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { priorityExpanded = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { priorityExpanded = true }
                    )
                    DropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.45f)
                    ) {
                        priorities.forEach { prio ->
                            DropdownMenuItem(
                                text = { Text(prio) },
                                onClick = {
                                    selectedPriority = prio
                                    priorityExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Date & Time pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Due Date field (clickable)
                Box(modifier = Modifier.weight(1f)) {
                    val formattedDate = if (dueDate == 0L) "No date selected" else DateUtils.formatLongDate(dueDate)
                    OutlinedTextField(
                        value = formattedDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Due Date *") },
                        isError = dateError != null,
                        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select Date") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = if (dueDate == 0L) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = if (dateError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                val calendar = Calendar.getInstance()
                                if (dueDate != 0L) calendar.timeInMillis = dueDate
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val selectedCal = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, year)
                                            set(Calendar.MONTH, month)
                                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }
                                        dueDate = selectedCal.timeInMillis
                                        dateError = null
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                    )
                }

                // Due Time field (clickable)
                Box(modifier = Modifier.weight(1f)) {
                    val formattedTime = if (dueTime.isEmpty()) "All Day" else DateUtils.formatTime(dueTime)
                    OutlinedTextField(
                        value = formattedTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Due Time") },
                        trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = "Select Time") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                val calendar = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        dueTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            }
                    )
                }
            }
            dateError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Recurrence and Reminder Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Recurrence selector
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = recurrence,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Recurrence") },
                        trailingIcon = {
                            Icon(
                                imageVector = if (recurrenceExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Recurrence dropdown"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { recurrenceExpanded = true }
                    )
                    DropdownMenu(
                        expanded = recurrenceExpanded,
                        onDismissRequest = { recurrenceExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.45f)
                    ) {
                        recurrences.forEach { rec ->
                            DropdownMenuItem(
                                text = { Text(rec) },
                                onClick = {
                                    recurrence = rec
                                    recurrenceExpanded = false
                                }
                            )
                        }
                    }
                }

                // Reminder Selector (UI selection only)
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = reminder,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Reminder") },
                        trailingIcon = {
                            Icon(
                                imageVector = if (reminderExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Reminder dropdown"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { reminderExpanded = true }
                    )
                    DropdownMenu(
                        expanded = reminderExpanded,
                        onDismissRequest = { reminderExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.45f)
                    ) {
                        reminders.forEach { rem ->
                            DropdownMenuItem(
                                text = { Text(rem) },
                                onClick = {
                                    reminder = rem
                                    reminderExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Rich Multiline Notes Editor
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Detailed Notes") },
                placeholder = {
                    Text("• Bullet points or numbered lists...\n• Add details here.")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )

            // Subtasks / Checklist Creator
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Checklist Subtasks",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtask items already added
                    subtasksList.forEachIndexed { index, sub ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Check, contentDescription = "Subtask", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(sub, style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(onClick = { subtasksList.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove subtask", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Add new subtask row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSubtaskText,
                            onValueChange = { newSubtaskText = it },
                            placeholder = { Text("Add subtask title") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newSubtaskText.trim().isNotEmpty()) {
                                    subtasksList.add(newSubtaskText.trim())
                                    newSubtaskText = ""
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add subtask")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons (Save / Cancel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        // Validate Fields
                        var isValid = true
                        if (title.trim().isEmpty()) {
                            titleError = "Title is required."
                            isValid = false
                        }
                        if (dueDate == 0L) {
                            dateError = "Please select due date."
                            isValid = false
                        }

                        if (isValid) {
                            if (isEditMode && existingTask != null) {
                                // Create new list of subtask entities
                                val subtaskEntities = subtasksList.map {
                                    // Check if subtask existed in database to preserve completion state
                                    val match = existingTask.subtasks.find { sub -> sub.title == it }
                                    SubtaskEntity(
                                        id = match?.id ?: 0,
                                        taskId = existingTask.task.id,
                                        title = it,
                                        isCompleted = match?.isCompleted ?: false
                                    )
                                }
                                viewModel.updateTask(
                                    id = existingTask.task.id,
                                    title = title.trim(),
                                    description = description.trim(),
                                    category = selectedCategory,
                                    priority = selectedPriority,
                                    dueDate = dueDate,
                                    dueTime = dueTime,
                                    reminder = reminder,
                                    notes = notes.trim(),
                                    subtasks = subtaskEntities,
                                    recurrence = recurrence
                                )
                            } else {
                                viewModel.createTask(
                                    title = title.trim(),
                                    description = description.trim(),
                                    category = selectedCategory,
                                    priority = selectedPriority,
                                    dueDate = dueDate,
                                    dueTime = dueTime,
                                    reminder = reminder,
                                    notes = notes.trim(),
                                    subtasks = subtasksList.toList(),
                                    recurrence = recurrence
                                )
                            }
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_task_button")
                ) {
                    Text(if (isEditMode) "Save Changes" else "Create Task")
                }
            }
        }
    }
}
