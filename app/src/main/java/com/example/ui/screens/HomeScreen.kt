package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskWithSubtasks
import com.example.ui.TaskViewModel
import com.example.ui.components.CategoryChip
import com.example.ui.components.EmptyState
import com.example.ui.components.PriorityBadge
import com.example.utils.DateUtils
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    viewModel: TaskViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToAddTask: () -> Unit,
    onNavigateToArchive: () -> Unit,
    onViewAllTasks: (() -> Unit)? = null
) {
    val tasks by viewModel.uiTasks.collectAsState()
    val stats by viewModel.statisticsState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Determine Greeting based on time
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    // Filter tasks based on search query (if any) and separate today/upcoming
    val filteredTasks = remember(tasks, searchQuery) {
        if (searchQuery.isBlank()) {
            tasks
        } else {
            tasks.filter {
                it.task.title.contains(searchQuery, ignoreCase = true) ||
                        it.task.description.contains(searchQuery, ignoreCase = true) ||
                        it.task.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val todayTasks = filteredTasks.filter { DateUtils.isToday(it.task.dueDate) }
    val upcomingTasks = filteredTasks.filter { !DateUtils.isToday(it.task.dueDate) && it.task.dueDate > System.currentTimeMillis() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header & Avatar (Exactly as styled in Professional Polish)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greeting.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sarah Jenkins",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                // Avatar circle with white border and linear gradient
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF4F5BA9), Color(0xFF8C95E0))
                            )
                        )
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SJ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }

        // Inline Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("home_search_bar"),
                placeholder = {
                    Text(
                        "Search tasks, notes, or tags...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Search")
                        }
                    }
                },
                singleLine = true,
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Stats Cards Grid matching mockup style and color layout
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Tasks: DDE1FF primaryContainer, border C5C6D0 outline, text 001452
                    DashboardCard(
                        title = "Total Tasks",
                        value = stats.totalTasks.toString(),
                        icon = Icons.Default.Assignment,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    // Overdue Tasks: white surface, border C5C6D0 outline, text BA1A1A
                    DashboardCard(
                        title = "Overdue",
                        value = stats.overdueTasks.toString(),
                        icon = Icons.Default.Error,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Completed Tasks: Surface, text Green
                    DashboardCard(
                        title = "Completed",
                        value = stats.completedTasks.toString(),
                        icon = Icons.Default.CheckCircle,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    // Pending Tasks: Surface, text Primary Indigo
                    DashboardCard(
                        title = "Pending",
                        value = stats.pendingTasks.toString(),
                        icon = Icons.Default.HourglassEmpty,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Productivity Progress Card (beautiful Indigo container with border)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Productivity Level",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Daily Score: ${stats.productivityScore}/100",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { stats.completionPercentage.toFloat() / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${stats.completionPercentage}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Today's Focus Title & VIEW ALL
        if (todayTasks.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Today's Focus",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (onViewAllTasks != null) {
                        Text(
                            text = "VIEW ALL",
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onViewAllTasks() }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }

            items(todayTasks, key = { it.task.id }) { task ->
                HomeTaskItem(
                    taskWithSubtasks = task,
                    onItemClick = { onNavigateToDetails(task.task.id) },
                    onCompleteToggle = { viewModel.toggleTaskCompletion(task) }
                )
            }
        }

        // Upcoming Tasks Title
        if (upcomingTasks.isNotEmpty()) {
            item {
                Text(
                    text = "Upcoming Tasks",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
            }

            items(upcomingTasks.take(5), key = { it.task.id }) { task ->
                HomeTaskItem(
                    taskWithSubtasks = task,
                    onItemClick = { onNavigateToDetails(task.task.id) },
                    onCompleteToggle = { viewModel.toggleTaskCompletion(task) }
                )
            }
        }

        // If completely empty
        if (filteredTasks.isEmpty()) {
            item {
                EmptyState(
                    title = "All Clean & Organized!",
                    description = if (searchQuery.isNotEmpty()) "No tasks matched \"$searchQuery\"." else "No tasks registered yet. Create a task now to jumpstart your daily productivity score!",
                    buttonText = if (searchQuery.isNotEmpty()) "Clear Search" else "+ Create First Task",
                    onButtonClick = {
                        if (searchQuery.isNotEmpty()) viewModel.searchQuery.value = "" else onNavigateToAddTask()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                )
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = contentColor
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(contentColor.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun HomeTaskItem(
    taskWithSubtasks: TaskWithSubtasks,
    onItemClick: () -> Unit,
    onCompleteToggle: () -> Unit
) {
    val task = taskWithSubtasks.task
    val completedCount = taskWithSubtasks.subtasks.count { it.isCompleted }
    val totalSubs = taskWithSubtasks.subtasks.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant Custom Checkbox exactly from the Professional Polish Mockup
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (task.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .border(
                        2.dp,
                        if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onCompleteToggle() }
                    .testTag("checkbox_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryChip(task.category)
                    PriorityBadge(task.priority)

                    if (task.recurrence != "None") {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recurring",
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (totalSubs > 0) {
                        Text(
                            text = "• $completedCount/$totalSubs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateUtils.formatShortDate(task.dueDate),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (DateUtils.isOverdue(task.dueDate, task.isCompleted)) Color(0xFFBA1A1A) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (task.dueTime.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = DateUtils.formatTime(task.dueTime),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
