package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TaskViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    var notificationEnabled by remember { mutableStateOf(true) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
            // Appearance Header
            Text(
                "Preferences",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // Dark Mode Switch Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = "Dark Mode")
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Dark Theme", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            Text("Enable dark slate color scheme", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onThemeToggle,
                        modifier = Modifier.testTag("dark_theme_switch")
                    )
                }
            }

            // Notification Switch Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Enable Alarms", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            Text("Enable task reminder placeholder state", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it }
                    )
                }
            }

            HorizontalDivider()

            // Backup & Restore Header
            Text(
                "Data Backup & Restore",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // Export Tasks Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backup, contentDescription = "Backup", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Backup Database", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            Text("Export all tasks & subtasks checklist into a JSON string copied to clipboard.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Button(
                        onClick = {
                            val json = viewModel.exportTasks()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Smart Task Manager Backup", json)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Tasks exported & copied to clipboard successfully!", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export JSON to Clipboard")
                    }
                }
            }

            // Restore Tasks Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backup, contentDescription = "Restore", tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Restore Database", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            Text("Paste backup JSON contents to restore your tasks and merge them securely.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Import JSON Backup")
                    }
                }
            }

            HorizontalDivider()

            // Account Security / Actions
            Text(
                "Account",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Logout, contentDescription = "Log Out", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Log Out", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error))
                            Text("Securely exit current local user session", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Log Out Action", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            HorizontalDivider()

            // About Card - crediting Muhammad Amir Zubair
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Developer Information", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("Developer: Muhammad Amir Zubair", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Text("About: Smart Task Manager is a modern task management application built with performance and security at its core.", style = MaterialTheme.typography.bodyMedium)
                    Text("Release Version: v1.0.0 (Production Ready)", style = MaterialTheme.typography.bodyMedium)
                    Text("Privacy Policy: All database actions happen completely offline. No tracking telemetry or data exports leave this Android container.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Import Dialog
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text("Import Database JSON") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Paste your backup JSON text below. Duplicate tasks will be merged securely.", style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(
                            value = importJsonText,
                            onValueChange = { importJsonText = it },
                            placeholder = { Text("Paste JSON here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            minLines = 4
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (importJsonText.trim().isNotEmpty()) {
                                val result = viewModel.importTasks(importJsonText)
                                if (result.isSuccess) {
                                    Toast.makeText(context, result.getOrNull(), Toast.LENGTH_LONG).show()
                                    showImportDialog = false
                                    importJsonText = ""
                                } else {
                                    Toast.makeText(context, "Failed to restore: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    ) {
                        Text("Restore")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
