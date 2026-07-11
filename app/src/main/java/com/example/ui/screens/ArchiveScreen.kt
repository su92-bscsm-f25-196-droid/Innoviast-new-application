package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.TaskViewModel
import com.example.ui.components.CategoryChip
import com.example.ui.components.EmptyState
import com.example.ui.components.PriorityBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit
) {
    val archivedList by viewModel.archivedTasks.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archived Vault", fontWeight = FontWeight.Bold) },
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
        if (archivedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = "Archive Vault is Empty",
                    description = "Archived tasks are stored safely here to keep your main dashboard clutter-free."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .testTag("archive_list")
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(archivedList, key = { it.task.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.task.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                if (item.task.description.isNotEmpty()) {
                                    Text(item.task.description, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    CategoryChip(item.task.category)
                                    PriorityBadge(item.task.priority)
                                }
                            }

                            Row {
                                // Restore Button
                                IconButton(onClick = { viewModel.restoreTask(item.task) }) {
                                    Icon(Icons.Default.Unarchive, contentDescription = "Restore task", tint = MaterialTheme.colorScheme.primary)
                                }
                                // Delete Permanently
                                IconButton(onClick = { viewModel.deleteHardwareTask(item.task.id) }) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = "Delete permanently", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
