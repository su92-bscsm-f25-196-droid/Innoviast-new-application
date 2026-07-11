package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import com.example.data.TaskEntity
import com.example.data.TaskWithSubtasks
import com.example.ui.TaskViewModel
import com.example.ui.components.CategoryChip
import com.example.ui.components.EmptyState
import com.example.ui.components.PriorityBadge
import com.example.utils.DateUtils

@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToAddTask: () -> Unit
) {
    val tasks by viewModel.uiTasks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedFilterCategory.collectAsState()
    val selectedPriority by viewModel.selectedFilterPriority.collectAsState()
    val selectedStatus by viewModel.selectedFilterStatus.collectAsState()
    val selectedDateRange by viewModel.selectedDateRange.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }

    val categories = listOf("Study", "Work", "Office", "Shopping", "Health", "Meeting", "Personal", "Event", "Others")
    val priorities = listOf("High", "Medium", "Low")
    val dateRanges = listOf("Due Today", "Due Tomorrow", "This Week", "Overdue")

    // Dynamic Tablet Support: check width in DP
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var selectedTaskForDetailOnTablet by remember { mutableStateOf<TaskWithSubtasks?>(null) }

    // If tablet, auto-select first task if none is selected
    LaunchedEffect(tasks) {
        if (isTablet && selectedTaskForDetailOnTablet == null && tasks.isNotEmpty()) {
            selectedTaskForDetailOnTablet = tasks.first()
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Master List Pane
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
        ) {
            // Smart Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(52.dp)
                    .testTag("search_bar"),
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

            // Horizontal Filters scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Toggle Button
                FilterChip(
                    selected = selectedStatus == "Pending",
                    onClick = { viewModel.selectedFilterStatus.value = "Pending" },
                    label = { Text("Pending") }
                )
                FilterChip(
                    selected = selectedStatus == "Completed",
                    onClick = { viewModel.selectedFilterStatus.value = "Completed" },
                    label = { Text("Completed") }
                )
                FilterChip(
                    selected = selectedStatus == "All",
                    onClick = { viewModel.selectedFilterStatus.value = "All" },
                    label = { Text("All") }
                )

                VerticalDivider(modifier = Modifier.height(24.dp))

                // Sort Dropdown Trigger
                AssistChip(
                    onClick = { showSortMenu = true },
                    label = { Text("Sort: $sortBy") },
                    leadingIcon = { Icon(Icons.Default.Sort, contentDescription = "Sort") }
                )

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    listOf("Due Date", "Newest", "Oldest", "Priority", "Alphabetical").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.sortBy.value = option
                                showSortMenu = false
                            }
                        )
                    }
                }

                VerticalDivider(modifier = Modifier.height(24.dp))

                // Date Ranges Filters
                dateRanges.forEach { range ->
                    FilterChip(
                        selected = selectedDateRange == range,
                        onClick = {
                            viewModel.selectedDateRange.value = if (selectedDateRange == range) null else range
                        },
                        label = { Text(range) }
                    )
                }

                VerticalDivider(modifier = Modifier.height(24.dp))

                // Category Filters
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            viewModel.selectedFilterCategory.value = if (selectedCategory == cat) null else cat
                        },
                        label = { Text(cat) }
                    )
                }

                VerticalDivider(modifier = Modifier.height(24.dp))

                // Priority Filters
                priorities.forEach { prio ->
                    FilterChip(
                        selected = selectedPriority == prio,
                        onClick = {
                            viewModel.selectedFilterPriority.value = if (selectedPriority == prio) null else prio
                        },
                        label = { Text("$prio Priority") }
                    )
                }
            }

            // Task List Core
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        title = if (searchQuery.isNotEmpty()) "No Matches Found" else "Your Checklist is Clear!",
                        description = if (searchQuery.isNotEmpty()) "We couldn't find any tasks matching \"$searchQuery\". Try refining your search query." else "Time to relax or add a new task to organize your schedules.",
                        buttonText = if (searchQuery.isNotEmpty()) "Clear Search" else "+ Create Task",
                        onButtonClick = {
                            if (searchQuery.isNotEmpty()) viewModel.searchQuery.value = "" else onNavigateToAddTask()
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("task_list"),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks, key = { it.task.id }) { task ->
                        InteractiveTaskCard(
                            taskWithSubtasks = task,
                            query = searchQuery,
                            isSelected = selectedTaskForDetailOnTablet?.task?.id == task.task.id,
                            onClick = {
                                if (isTablet) {
                                    selectedTaskForDetailOnTablet = task
                                } else {
                                    onNavigateToDetails(task.task.id)
                                }
                            },
                            onCompleteToggle = { viewModel.toggleTaskCompletion(task) },
                            onPinToggle = { viewModel.togglePin(task.task) },
                            onFavToggle = { viewModel.toggleFavorite(task.task) },
                            onArchive = { viewModel.archiveTask(task.task) },
                            onDelete = { viewModel.deleteHardwareTask(task.task.id) },
                            onEdit = { onNavigateToEdit(task.task.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        // Tablet Detail Pane (If screen size class is large)
        if (isTablet) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            ) {
                selectedTaskForDetailOnTablet?.let { item ->
                    TaskDetailPane(
                        item = item,
                        viewModel = viewModel,
                        onEdit = { onNavigateToEdit(item.task.id) },
                        onClose = { selectedTaskForDetailOnTablet = null }
                    )
                } ?: Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Select a task to view full details",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveTaskCard(
    taskWithSubtasks: TaskWithSubtasks,
    query: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onCompleteToggle: () -> Unit,
    onPinToggle: () -> Unit,
    onFavToggle: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val task = taskWithSubtasks.task
    val completedCount = taskWithSubtasks.subtasks.count { it.isCompleted }
    val totalSubs = taskWithSubtasks.subtasks.size

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                task.isCompleted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Elegant Custom Checkbox from Professional Polish Mockup
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
                        .clickable { onCompleteToggle() },
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

                Spacer(modifier = Modifier.width(12.dp))

                // Title and notes query highlight
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
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
                            text = getHighlightedText(task.title, query),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                            ),
                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (task.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = getHighlightedText(task.description, query),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Date Information
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

            // Subtask progress
            if (totalSubs > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.padding(start = 36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$completedCount/$totalSubs Completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { completedCount.toFloat() / totalSubs },
                        modifier = Modifier
                            .width(80.dp)
                            .height(4.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer labels & Quick Actions row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CategoryChip(task.category)
                    PriorityBadge(task.priority)
                }

                // Quick Action Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPinToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (task.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = if (task.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onFavToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (task.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (task.isFavorite) Color(0xFFEC4899) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onArchive,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = "Archive",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFBA1A1A),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Support highlighting matched text during search query matching
@Composable
fun getHighlightedText(text: String, query: String): androidx.compose.ui.text.AnnotatedString {
    if (query.isEmpty() || !text.contains(query, ignoreCase = true)) {
        return buildAnnotatedString { append(text) }
    }

    return buildAnnotatedString {
        var startIndex = 0
        while (startIndex < text.length) {
            val matchIndex = text.indexOf(query, startIndex, ignoreCase = true)
            if (matchIndex == -1) {
                append(text.substring(startIndex))
                break
            } else {
                append(text.substring(startIndex, matchIndex))
                withStyle(
                    style = SpanStyle(
                        background = Color(0xFFFEF08A), // bright highlighter yellow
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                ) {
                    append(text.substring(matchIndex, matchIndex + query.length))
                }
                startIndex = matchIndex + query.length
            }
        }
    }
}

@Composable
fun TaskDetailPane(
    item: TaskWithSubtasks,
    viewModel: TaskViewModel,
    onEdit: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Task Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close pane")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = item.task.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryChip(item.task.category)
            PriorityBadge(item.task.priority)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (item.task.description.isNotEmpty()) {
            Text("Description", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(item.task.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (item.task.notes.isNotEmpty()) {
            Text("Notes", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(item.task.notes, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "Due Date: ${DateUtils.formatLongDate(item.task.dueDate)} at ${DateUtils.formatTime(item.task.dueTime)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (item.task.recurrence != "None") {
            Text(
                text = "Recurrence: ${item.task.recurrence}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6366F1),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (item.subtasks.isNotEmpty()) {
            Text("Subtasks Checklist", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            item.subtasks.forEach { sub ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = sub.isCompleted,
                        onCheckedChange = { viewModel.toggleSubtaskCompletion(sub) }
                    )
                    Text(
                        text = sub.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else null
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onEdit,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit")
            }
            OutlinedButton(
                onClick = { viewModel.toggleTaskCompletion(item) },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (item.task.isCompleted) "Reopen" else "Complete")
            }
        }
    }
}
