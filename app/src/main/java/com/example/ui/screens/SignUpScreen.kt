package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TaskViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Bio & Address
    var bio by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    // Profile Pic Selection state (simulating custom paths/avatars)
    var profilePicUri by remember { mutableStateOf("avatar_1") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isSigningUp by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPicDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // 6 gorgeous gradient preset avatars for premium aesthetic + mock URI fields
    val presetAvatars = listOf(
        "avatar_1" to listOf(Color(0xFF4F5BA9), Color(0xFF8C95E0)),
        "avatar_2" to listOf(Color(0xFFEC4899), Color(0xFFF43F5E)),
        "avatar_3" to listOf(Color(0xFF10B981), Color(0xFF34D399)),
        "avatar_4" to listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)),
        "avatar_5" to listOf(Color(0xFF6366F1), Color(0xFF818CF8)),
        "avatar_6" to listOf(Color(0xFF14B8A6), Color(0xFF2DD4BF))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create Local Account",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Join Smart Task Manager to secure your own personal categories and tasks offline.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Circular Profile Picture Selection Preview
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = presetAvatars.find { it.first == profilePicUri }?.second
                                ?: listOf(Color.Gray, Color.DarkGray)
                        )
                    )
                    .clickable { showPicDialog = true }
                    .testTag("signup_avatar_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (fullName.trim().isNotEmpty()) fullName.take(2).uppercase() else "ST",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                // Small edit badge
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "EDIT",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = "Tap circle to change photo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { showPicDialog = true }
            )

            // Name Field
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    errorMessage = null
                },
                label = { Text("Full Name *") },
                placeholder = { Text("Sarah Jenkins") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_name_input"),
                singleLine = true
            )

            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    errorMessage = null
                },
                label = { Text("Username *") },
                placeholder = { Text("sarahj") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_username_input"),
                singleLine = true
            )

            // Email Address
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("Email Address *") },
                placeholder = { Text("sarah.jenkins@example.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_email_input"),
                singleLine = true
            )

            // Phone Number
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    errorMessage = null
                },
                label = { Text("Phone Number *") },
                placeholder = { Text("0123456789") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_phone_input"),
                singleLine = true
            )

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Password *") },
                placeholder = { Text("At least 8 chars (A-Z, a-z, 0-9, @)") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(icon, contentDescription = "Toggle password")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_password_input"),
                singleLine = true
            )

            // Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text("Confirm Password *") },
                placeholder = { Text("Repeat password") },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val icon = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(icon, contentDescription = "Toggle confirm password")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_confirm_password_input"),
                singleLine = true
            )

            // Optional demographic details fields
            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Date of Birth (Optional)") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = gender,
                onValueChange = { gender = it },
                label = { Text("Gender (Optional)") },
                placeholder = { Text("Female / Male / Non-binary") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio (Optional)") },
                placeholder = { Text("Freelance Developer & designer...") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address (Optional)") },
                placeholder = { Text("123, Central Avenue") },
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isSigningUp) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else {
                Button(
                    onClick = {
                        if (fullName.trim().isEmpty() || username.trim().isEmpty() || email.trim().isEmpty() || phone.trim().isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                            errorMessage = "Please fill all required fields."
                            return@Button
                        }
                        if (password != confirmPassword) {
                            errorMessage = "Password mismatch."
                            return@Button
                        }

                        isSigningUp = true
                        coroutineScope.launch {
                            delay(600) // Beautiful response animation delay
                            viewModel.registerUser(
                                fullName = fullName,
                                username = username,
                                email = email,
                                phone = phone,
                                password = password,
                                confirmPassword = confirmPassword,
                                dob = dob,
                                gender = gender,
                                bio = bio,
                                address = address
                            ).collect { result ->
                                if (result.isSuccess) {
                                    // Automatically login newly registered user
                                    viewModel.loginUser(email, password, rememberMe = true).collect { loginRes ->
                                        isSigningUp = false
                                        if (loginRes.isSuccess) {
                                            onNavigateToHome()
                                        } else {
                                            errorMessage = "Account created, but automatic login failed. Please try logging in manually."
                                        }
                                    }
                                } else {
                                    isSigningUp = false
                                    errorMessage = result.exceptionOrNull()?.message ?: "Sign up failed"
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("signup_submit_button")
                ) {
                    Text(
                        "Create Account",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account? ", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Log In",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clickable { onNavigateBack() }
                        .testTag("signup_login_navigation")
                )
            }
        }
    }

    // Pic Dialog (Change profile photo simulator)
    if (showPicDialog) {
        AlertDialog(
            onDismissRequest = { showPicDialog = false },
            title = { Text("Profile Picture") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Select a theme color gradient or run a simulated hardware acquisition:")

                    // Preset circular cards
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

                    // Simulated Camera & Gallery options
                    ListItem(
                        headlineContent = { Text("Simulate Camera Capture") },
                        leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = "Camera") },
                        modifier = Modifier.clickable {
                            profilePicUri = "avatar_1" // Instant Camera simulation selection
                            showPicDialog = false
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Simulate Gallery Picking") },
                        leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery") },
                        modifier = Modifier.clickable {
                            profilePicUri = "avatar_4" // Simulated custom picking
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
