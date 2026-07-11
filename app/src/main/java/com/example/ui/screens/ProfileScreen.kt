package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TaskViewModel
import com.example.ui.components.AnimatedBarChart
import com.example.ui.components.AnimatedPieChart
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: TaskViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToArchive: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val stats by viewModel.statisticsState.collectAsState()
    val activeUser by viewModel.currentUser.collectAsState()

    // Edit states
    var isEditing by remember { mutableStateOf(false) }

    // Forms
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var profilePicUri by remember { mutableStateOf("avatar_1") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPicDialog by remember { mutableStateOf(false) }

    // Synchronize form states when the active user loads
    LaunchedEffect(activeUser, isEditing) {
        if (!isEditing) {
            fullName = activeUser?.fullName ?: "Guest User"
            username = activeUser?.username ?: "guest"
            email = activeUser?.email ?: "guest@example.com"
            phone = activeUser?.phone ?: "0000000000"
            dob = activeUser?.dob ?: ""
            gender = activeUser?.gender ?: ""
            bio = activeUser?.bio ?: ""
            address = activeUser?.address ?: ""
            profilePicUri = activeUser?.profilePicUri ?: "avatar_1"
        }
    }

    val presetAvatars = listOf(
        "avatar_1" to listOf(Color(0xFF4F5BA9), Color(0xFF8C95E0)),
        "avatar_2" to listOf(Color(0xFFEC4899), Color(0xFFF43F5E)),
        "avatar_3" to listOf(Color(0xFF10B981), Color(0xFF34D399)),
        "avatar_4" to listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)),
        "avatar_5" to listOf(Color(0xFF6366F1), Color(0xFF818CF8)),
        "avatar_6" to listOf(Color(0xFF14B8A6), Color(0xFF2DD4BF))
    )

    val selectedAvatarColors = presetAvatars.find { it.first == profilePicUri }?.second
        ?: listOf(Color(0xFF6B7280), Color(0xFF9CA3AF))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_screen_column")
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stylized generic avatar with nice gradients matching selected preset
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(selectedAvatarColors))
                    .clickable(enabled = isEditing) { showPicDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (fullName.trim().isNotEmpty()) fullName.take(2).uppercase() else "ST",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Edit photo", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                if (!isEditing && activeUser != null) {
                    IconButton(onClick = { isEditing = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        HorizontalDivider()

        if (isEditing) {
            // Render beautiful edit form for Profile Screen
            Text(
                "Edit Profile Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Date of Birth") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = gender,
                onValueChange = { gender = it },
                label = { Text("Gender") },
                placeholder = { Text("Female / Male / Non-binary") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                placeholder = { Text("Describe yourself...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                placeholder = { Text("Where do you live?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isEditing = false
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.updateProfile(
                                fullName = fullName,
                                username = username,
                                email = email,
                                phone = phone,
                                dob = dob,
                                gender = gender,
                                bio = bio,
                                address = address,
                                profilePicUri = profilePicUri
                            ).collect { result ->
                                if (result.isSuccess) {
                                    isEditing = false
                                    errorMessage = null
                                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to update profile"
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save")
                }
            }

            HorizontalDivider()
        } else {
            // View-Only: render Bio or details if available
            if (bio.isNotEmpty() || address.isNotEmpty() || dob.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Profile Details", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        if (bio.isNotEmpty()) Text("Bio: $bio", style = MaterialTheme.typography.bodyMedium)
                        if (address.isNotEmpty()) Text("Address: $address", style = MaterialTheme.typography.bodyMedium)
                        if (dob.isNotEmpty()) Text("DOB: $dob", style = MaterialTheme.typography.bodyMedium)
                        if (gender.isNotEmpty()) Text("Gender: $gender", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Quick Overview Stats Cards
        Text(
            text = "Activity Analytics",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCompactCard(
                title = "Completion",
                value = "${stats.completionPercentage}%",
                modifier = Modifier.weight(1f)
            )
            StatsCompactCard(
                title = "Overdue",
                value = stats.overdueTasks.toString(),
                modifier = Modifier.weight(1f)
            )
            StatsCompactCard(
                title = "Productivity",
                value = "${stats.productivityScore}/100",
                modifier = Modifier.weight(1f)
            )
        }

        // Animated Bar Chart (Weekly completion distribution)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                AnimatedBarChart(weeklyData = stats.weeklyCompletedDistribution)
            }
        }

        // Animated Pie Chart (Category distribution)
        if (stats.categoryTaskDistribution.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Category Distribution",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    AnimatedPieChart(data = stats.categoryTaskDistribution)
                }
            }
        }

        // Navigation Links / Vault Access
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToArchive() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Archive",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "View Archived Vault",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Access deleted or archived tasks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(Icons.Default.ArrowForward, contentDescription = "Go")
            }
        }

        // About Application Card - Crediting Muhammad Amir Zubair
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "About Application",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text("App Version: 1.0.0", style = MaterialTheme.typography.bodyMedium)
                Text("Developer: Muhammad Amir Zubair", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text("Design System: Material You (M3)", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    // Custom Profile Pic Selection Overlay Simulator
    if (showPicDialog) {
        AlertDialog(
            onDismissRequest = { showPicDialog = false },
            title = { Text("Choose Avatar Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Select a professional color theme gradient for your card:")

                    // Presets Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        presetAvatars.take(3).forEach { (id, colors) ->
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(colors))
                                    .border(
                                        width = if (profilePicUri == id) 3.dp else 0.dp,
                                        color = if (profilePicUri == id) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        profilePicUri = id
                                        showPicDialog = false
                                    }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        presetAvatars.takeLast(3).forEach { (id, colors) ->
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(colors))
                                    .border(
                                        width = if (profilePicUri == id) 3.dp else 0.dp,
                                        color = if (profilePicUri == id) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        profilePicUri = id
                                        showPicDialog = false
                                    }
                            )
                        }
                    }

                    HorizontalDivider()

                    ListItem(
                        headlineContent = { Text("Simulate Device Gallery") },
                        leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery Picker") },
                        modifier = Modifier.clickable {
                            profilePicUri = "avatar_3"
                            showPicDialog = false
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Simulate Camera Capture") },
                        leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = "Camera Capture") },
                        modifier = Modifier.clickable {
                            profilePicUri = "avatar_5"
                            showPicDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatsCompactCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
        }
    }
}
