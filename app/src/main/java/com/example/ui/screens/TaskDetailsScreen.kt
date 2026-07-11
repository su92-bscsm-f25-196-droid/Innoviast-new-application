package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TaskViewModel
import com.example.ui.components.CategoryChip
import com.example.ui.components.PriorityBadge
import com.example.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    viewModel: TaskViewModel,
    taskId: Int,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val tasks by viewModel.allTasks.collectAsState(initial = emptyList())
    val taskWithSubtasks = remember(tasks, taskId) {
        tasks.find { it.task.id == taskId }
    }

    if (taskWithSubtasks == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val task = taskWithSubtasks.task
    val subtasks = taskWithSubtasks.subtasks
    val completedCount = subtasks.count { it.isCompleted }
    val totalSubs = subtasks.size

    val formatDateTime = remember { SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(task.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Task")
                    }
                    IconButton(onClick = {
                        viewModel.deleteHardwareTask(task.id)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = Color(0xFFEF4444))
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
            // Task Title & Pin/Favorite indicator
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryChip(task.category)
                            Spacer(modifier = Modifier.width(8.dp))
                            PriorityBadge(task.priority)
                        }

                        Row {
                            IconButton(onClick = { viewModel.togglePin(task) }) {
                                Icon(
                                    imageVector = if (task.isPinned) Icons.Filled.PushPin else Icons.Filled.PushPin,
                                    contentDescription = "Pin",
                                    tint = if (task.isPinned) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)
                                )
                            }
                            IconButton(onClick = { viewModel.toggleFavorite(task) }) {
                                Icon(
                                    imageVector = if (task.isFavorite) Icons.Filled.Favorite else Icons.Filled.Favorite,
                                    contentDescription = "Favorite",
                                    tint = if (task.isFavorite) Color(0xFFEC4899) else Color.Gray.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                        ),
                        color = if (task.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )

                    if (task.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Scheduling Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = "Date", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Due Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(DateUtils.formatLongDate(task.dueDate), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Time", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Due Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (task.dueTime.isNotEmpty()) DateUtils.formatTime(task.dueTime) else "All Day", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recurrence", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Recurrence Schedule", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(task.recurrence, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = "Reminder", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Reminder Alarm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(task.reminder, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Detailed Notes Section
            if (task.notes.isNotEmpty()) {
                Text(
                    text = "Detailed Notes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        lineHeight = 22.sp
                    )
                }
            }

            // Subtasks / Checklist
            if (subtasks.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Checklist Subtasks ($completedCount/$totalSubs)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    LinearProgressIndicator(
                        progress = { completedCount.toFloat() / totalSubs },
                        modifier = Modifier
                            .width(100.dp)
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        subtasks.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleSubtaskCompletion(sub) }
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (sub.isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = "Toggle Complete",
                                    tint = if (sub.isCompleted) Color(0xFF10B981) else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = sub.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else null
                                    ),
                                    color = if (sub.isCompleted) Color.Gray else Color.Unspecified
                                )
                            }
                        }
                    }
                }
            }

            // Metadata info
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Created: ${formatDateTime.format(Date(task.createdAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Last Updated: ${formatDateTime.format(Date(task.updatedAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Completion Action Bar at bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.archiveTask(task)
                        onNavigateBack()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Archive, contentDescription = "Archive")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Archive")
                }

                Button(
                    onClick = {
                        viewModel.toggleTaskCompletion(taskWithSubtasks)
                    },
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.isCompleted) Color(0xFF6B7280) else Color(0xFF10B981)
                    )
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.Undo else Icons.Default.Check,
                        contentDescription = "Complete"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (task.isCompleted) "Reopen Task" else "Mark Complete")
                }
            }
        }
    }
}
