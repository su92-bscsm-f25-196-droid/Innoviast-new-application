package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskWithSubtasks
import com.example.ui.TaskViewModel
import com.example.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: TaskViewModel,
    onNavigateToDetails: (Int) -> Unit
) {
    val tasks by viewModel.allTasks.collectAsState(initial = emptyList())

    // Tracks current month/year being viewed
    var calendarViewDate by remember { mutableStateOf(Calendar.getInstance()) }
    // Selected day (by default today)
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    val monthName = remember(calendarViewDate) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendarViewDate.time)
    }

    // Days in current calendar month view
    val daysInMonth = remember(calendarViewDate) {
        val cal = calendarViewDate.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed (0=Sun, 1=Mon, etc.)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        Pair(firstDayOfWeek, maxDays)
    }

    val firstDayOffset = daysInMonth.first
    val maxDays = daysInMonth.second

    // All active tasks in system (not archived)
    val activeTasks = remember(tasks) {
        tasks.filter { it.task.status != "Archived" }
    }

    // Filter tasks for the selected day
    val selectedDayTasks = remember(activeTasks, selectedDate) {
        activeTasks.filter { task ->
            val calTask = Calendar.getInstance().apply { timeInMillis = task.task.dueDate }
            calTask.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                    calTask.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("calendar_screen_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val nextCal = calendarViewDate.clone() as Calendar
                nextCal.add(Calendar.MONTH, -1)
                calendarViewDate = nextCal
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }

            Text(
                text = monthName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            IconButton(onClick = {
                val nextCal = calendarViewDate.clone() as Calendar
                nextCal.add(Calendar.MONTH, 1)
                calendarViewDate = nextCal
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }

        // Calendar Grid Header (Days of week)
        val weekDays = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Calendar Grid Days
        val totalCells = firstDayOffset + maxDays
        val rows = (totalCells + 6) / 7

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (r in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (c in 0..6) {
                        val cellIndex = r * 7 + c
                        val dayNumber = cellIndex - firstDayOffset + 1

                        if (dayNumber in 1..maxDays) {
                            val cellDate = (calendarViewDate.clone() as Calendar).apply {
                                set(Calendar.DAY_OF_MONTH, dayNumber)
                            }

                            val isCellSelected = cellDate.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                                    cellDate.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)

                            val isToday = cellDate.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
                                    cellDate.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

                            // Find tasks on this date
                            val tasksOnDate = activeTasks.filter { task ->
                                val calTask = Calendar.getInstance().apply { timeInMillis = task.task.dueDate }
                                calTask.get(Calendar.YEAR) == cellDate.get(Calendar.YEAR) &&
                                        calTask.get(Calendar.DAY_OF_YEAR) == cellDate.get(Calendar.DAY_OF_YEAR)
                            }

                            CalendarCell(
                                dayNumber = dayNumber,
                                isToday = isToday,
                                isSelected = isCellSelected,
                                tasksOnDate = tasksOnDate,
                                onClick = {
                                    selectedDate = cellDate
                                },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Blank filler cell
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // Selected Date Task List Header
        val selectedDateFormatted = remember(selectedDate) {
            SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(selectedDate.time)
        }

        Text(
            text = "Tasks for $selectedDateFormatted",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Selected Date Tasks List
        if (selectedDayTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks scheduled for this day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedDayTasks, key = { it.task.id }) { task ->
                    HomeTaskItem(
                        taskWithSubtasks = task,
                        onItemClick = { onNavigateToDetails(task.task.id) },
                        onCompleteToggle = { viewModel.toggleTaskCompletion(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarCell(
    dayNumber: Int,
    isToday: Boolean,
    isSelected: Boolean,
    tasksOnDate: List<TaskWithSubtasks>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasCompleted = tasksOnDate.isNotEmpty() && tasksOnDate.all { it.task.isCompleted }
    val hasPending = tasksOnDate.isNotEmpty() && tasksOnDate.any { !it.task.isCompleted }

    Column(
        modifier = modifier
            .aspectRatio(1.1f)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayNumber.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 15.sp
            ),
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                isToday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Small indicator dots for tasks
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasCompleted) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(Color(0xFF10B981), CircleShape) // Green dot for complete
                )
            }
            if (hasPending) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape) // Primary blue dot for pending
                )
            }
        }
    }
}
