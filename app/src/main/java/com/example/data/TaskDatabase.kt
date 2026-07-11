package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // Email or username (lowercased)
    val fullName: String,
    val username: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val salt: String,
    val bio: String = "",
    val address: String = "",
    val profilePicUri: String = "",
    val dob: String = "",
    val gender: String = ""
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "guest", // Multi-user isolation key
    val title: String,
    val description: String,
    val category: String, // Study, Work, Office, Shopping, Health, Meeting, Personal, Event, Others
    val priority: String, // Low, Medium, High
    val dueDate: Long, // timestamp
    val dueTime: String, // "HH:mm" or empty
    val reminder: String, // No Reminder, 15 Minutes Before, etc.
    val status: String, // Pending, Completed, Archived
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val recurrence: String = "None", // None, Daily, Weekdays, Weekly, Monthly, Yearly
    val notes: String = "",
    val sortOrder: Int = 0
)

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    val title: String,
    val isCompleted: Boolean = false
)

data class TaskWithSubtasks(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<SubtaskEntity>
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface TaskDao {
    @Transaction
    @Query("SELECT * FROM tasks WHERE userId = :userId AND status != 'Archived' ORDER BY isPinned DESC, sortOrder ASC, createdAt DESC")
    fun getAllTasksFlow(userId: String): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE userId = :userId AND status = 'Archived' ORDER BY updatedAt DESC")
    fun getArchivedTasksFlow(userId: String): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): TaskWithSubtasks?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)

    // Subtask Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long

    @Update
    suspend fun updateSubtask(subtask: SubtaskEntity)

    @Delete
    suspend fun deleteSubtask(subtask: SubtaskEntity)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasksByTaskId(taskId: Int)

    @Transaction
    suspend fun insertTaskWithSubtasks(task: TaskEntity, subtasks: List<SubtaskEntity>) {
        val taskId = insertTask(task).toInt()
        subtasks.forEach {
            insertSubtask(it.copy(taskId = taskId))
        }
    }

    @Transaction
    suspend fun updateTaskWithSubtasks(task: TaskEntity, subtasks: List<SubtaskEntity>) {
        updateTask(task)
        deleteSubtasksByTaskId(task.id)
        subtasks.forEach {
            insertSubtask(it.copy(taskId = task.id))
        }
    }
}

@Database(entities = [TaskEntity::class, SubtaskEntity::class, UserEntity::class], version = 2, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao
}
