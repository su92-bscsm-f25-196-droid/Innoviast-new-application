package com.example.utils

import com.example.data.SubtaskEntity
import com.example.data.TaskEntity
import com.example.data.TaskWithSubtasks
import org.json.JSONArray
import org.json.JSONObject

object ImportExportHelper {

    fun exportTasksToJson(tasks: List<TaskWithSubtasks>): String {
        val rootArray = JSONArray()
        for (item in tasks) {
            val taskObj = JSONObject().apply {
                put("title", item.task.title)
                put("description", item.task.description)
                put("category", item.task.category)
                put("priority", item.task.priority)
                put("dueDate", item.task.dueDate)
                put("dueTime", item.task.dueTime)
                put("reminder", item.task.reminder)
                put("status", item.task.status)
                put("isCompleted", item.task.isCompleted)
                put("isPinned", item.task.isPinned)
                put("isFavorite", item.task.isFavorite)
                put("recurrence", item.task.recurrence)
                put("notes", item.task.notes)
                put("sortOrder", item.task.sortOrder)
                put("createdAt", item.task.createdAt)
                put("updatedAt", item.task.updatedAt)

                // Subtasks
                val subtasksArray = JSONArray()
                for (sub in item.subtasks) {
                    val subObj = JSONObject().apply {
                        put("title", sub.title)
                        put("isCompleted", sub.isCompleted)
                    }
                    subtasksArray.put(subObj)
                }
                put("subtasks", subtasksArray)
            }
            rootArray.put(taskObj)
        }
        return rootArray.toString(4)
    }

    fun importTasksFromJson(jsonString: String): List<Pair<TaskEntity, List<SubtaskEntity>>> {
        val list = mutableListOf<Pair<TaskEntity, List<SubtaskEntity>>>()
        try {
            val rootArray = JSONArray(jsonString)
            for (i in 0 until rootArray.length()) {
                val taskObj = rootArray.getJSONObject(i)
                val title = taskObj.optString("title", "").trim()
                if (title.isEmpty()) continue // Required field validation

                val description = taskObj.optString("description", "")
                val category = taskObj.optString("category", "Others")
                val priority = taskObj.optString("priority", "Medium")
                val dueDate = taskObj.optLong("dueDate", 0L)
                val dueTime = taskObj.optString("dueTime", "")
                val reminder = taskObj.optString("reminder", "No Reminder")
                val status = taskObj.optString("status", "Pending")
                val isCompleted = taskObj.optBoolean("isCompleted", false)
                val isPinned = taskObj.optBoolean("isPinned", false)
                val isFavorite = taskObj.optBoolean("isFavorite", false)
                val recurrence = taskObj.optString("recurrence", "None")
                val notes = taskObj.optString("notes", "")
                val sortOrder = taskObj.optInt("sortOrder", 0)
                val createdAt = taskObj.optLong("createdAt", System.currentTimeMillis())
                val updatedAt = taskObj.optLong("updatedAt", System.currentTimeMillis())

                val task = TaskEntity(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    reminder = reminder,
                    status = status,
                    isCompleted = isCompleted,
                    isPinned = isPinned,
                    isFavorite = isFavorite,
                    recurrence = recurrence,
                    notes = notes,
                    sortOrder = sortOrder,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )

                val subtasksList = mutableListOf<SubtaskEntity>()
                if (taskObj.has("subtasks")) {
                    val subsArray = taskObj.getJSONArray("subtasks")
                    for (j in 0 until subsArray.length()) {
                        val subObj = subsArray.getJSONObject(j)
                        val subTitle = subObj.optString("title", "").trim()
                        if (subTitle.isNotEmpty()) {
                            subtasksList.add(
                                SubtaskEntity(
                                    taskId = 0, // Assigned during insert
                                    title = subTitle,
                                    isCompleted = subObj.optBoolean("isCompleted", false)
                                )
                            )
                        }
                    }
                }
                list.add(Pair(task, subtasksList))
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid JSON format or required fields missing: ${e.localizedMessage}")
        }
        return list
    }
}
