package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(
    viewModel: TaskViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToAddTask: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToArchive: () -> Unit
) {
    var activeTab by remember { mutableStateOf("home") }

    Scaffold(
        topBar = {
            if (activeTab != "home") {
                TopAppBar(
                    title = {
                        Text(
                            text = when (activeTab) {
                                "tasks" -> "My Tasks"
                                "calendar" -> "Schedule Calendar"
                                else -> "My Profile"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        if (activeTab == "tasks") {
                            IconButton(onClick = onNavigateToArchive) {
                                Icon(Icons.Default.Archive, contentDescription = "Archive Vault")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = activeTab == "home",
                    onClick = { activeTab = "home" },
                    icon = { Icon(if (activeTab == "home") Icons.Default.Home else Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("nav_tab_home")
                )
                NavigationBarItem(
                    selected = activeTab == "tasks",
                    onClick = { activeTab = "tasks" },
                    icon = { Icon(if (activeTab == "tasks") Icons.Default.ListAlt else Icons.Outlined.ListAlt, contentDescription = "Tasks") },
                    label = { Text("Tasks") },
                    modifier = Modifier.testTag("nav_tab_tasks")
                )
                NavigationBarItem(
                    selected = activeTab == "calendar",
                    onClick = { activeTab = "calendar" },
                    icon = { Icon(if (activeTab == "calendar") Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth, contentDescription = "Calendar") },
                    label = { Text("Calendar") },
                    modifier = Modifier.testTag("nav_tab_calendar")
                )
                NavigationBarItem(
                    selected = activeTab == "profile",
                    onClick = { activeTab = "profile" },
                    icon = { Icon(if (activeTab == "profile") Icons.Default.AccountCircle else Icons.Outlined.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    modifier = Modifier.testTag("nav_tab_profile")
                )
            }
        },
        floatingActionButton = {
            // Show FAB only on Home and Tasks screens to keep UI beautiful and uncluttered
            if (activeTab == "home" || activeTab == "tasks") {
                FloatingActionButton(
                    onClick = onNavigateToAddTask,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_add_task")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Crossfade for smooth tab switching animations
            Crossfade(targetState = activeTab, label = "tab_fade") { tab ->
                when (tab) {
                    "home" -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToDetails = onNavigateToDetails,
                        onNavigateToAddTask = onNavigateToAddTask,
                        onNavigateToArchive = onNavigateToArchive,
                        onViewAllTasks = { activeTab = "tasks" }
                    )
                    "tasks" -> TaskListScreen(
                        viewModel = viewModel,
                        onNavigateToDetails = onNavigateToDetails,
                        onNavigateToEdit = onNavigateToEdit,
                        onNavigateToAddTask = onNavigateToAddTask
                    )
                    "calendar" -> CalendarScreen(
                        viewModel = viewModel,
                        onNavigateToDetails = onNavigateToDetails
                    )
                    "profile" -> ProfileScreen(
                        viewModel = viewModel,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToArchive = onNavigateToArchive
                    )
                }
            }
        }
    }
}
