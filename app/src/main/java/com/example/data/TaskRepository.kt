package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val userDao: UserDao
) {

    // Retrieve active and archived tasks isolated by userId
    fun getAllTasksFlow(userId: String): Flow<List<TaskWithSubtasks>> = taskDao.getAllTasksFlow(userId)
    fun getArchivedTasksFlow(userId: String): Flow<List<TaskWithSubtasks>> = taskDao.getArchivedTasksFlow(userId)

    suspend fun getTaskById(id: Int): TaskWithSubtasks? {
        return taskDao.getTaskById(id)
    }

    suspend fun insertTaskWithSubtasks(task: TaskEntity, subtasks: List<SubtaskEntity>) {
        taskDao.insertTaskWithSubtasks(task, subtasks)
    }

    suspend fun updateTaskWithSubtasks(task: TaskEntity, subtasks: List<SubtaskEntity>) {
        taskDao.updateTaskWithSubtasks(task, subtasks)
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun deleteHardwareTask(id: Int) {
        taskDao.deleteTaskById(id)
    }

    suspend fun insertSubtask(subtask: SubtaskEntity) {
        taskDao.insertSubtask(subtask)
    }

    suspend fun updateSubtask(subtask: SubtaskEntity) {
        taskDao.updateSubtask(subtask)
    }

    suspend fun deleteSubtask(subtask: SubtaskEntity) {
        taskDao.deleteSubtask(subtask)
    }

    // Support updating list order (Drag & Drop sorting order)
    suspend fun updateTasksOrder(tasks: List<TaskEntity>) {
        tasks.forEachIndexed { index, task ->
            taskDao.updateTask(task.copy(sortOrder = index))
        }
    }

    // User Operations
    suspend fun getUserById(id: String): UserEntity? {
        return userDao.getUserById(id)
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }

    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }
}
