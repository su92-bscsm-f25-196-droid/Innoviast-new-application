package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.data.TaskDatabase
import com.example.data.TaskRepository
import com.example.ui.TaskViewModel
import com.example.ui.TaskViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load persisted preferences & settings
        val sharedPrefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        // Initialize SQLite Room Database
        val database = Room.databaseBuilder(
            applicationContext,
            TaskDatabase::class.java,
            "smart_task_manager.db"
        ).fallbackToDestructiveMigration().build()

        val repository = TaskRepository(database.taskDao(), database.userDao())
        val factory = TaskViewModelFactory(repository, sharedPrefs)
        viewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        setContent {
            // Track theme state reactively
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember {
                mutableStateOf(sharedPrefs.getBoolean("dark_theme", systemDark))
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "splash") {
                        // Splash Screen
                        composable("splash") {
                            SplashScreen(
                                onNavigateToMain = {
                                    // Session route determination based on logged-in state
                                    val isLoggedIn = viewModel.currentUser.value != null
                                    val startRoute = if (isLoggedIn) "main" else "welcome"
                                    navController.navigate(startRoute) {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Welcome Screen introducing Smart Task Manager
                        composable("welcome") {
                            WelcomeScreen(
                                onNavigateToLogin = { navController.navigate("login") },
                                onNavigateToSignUp = { navController.navigate("signup") }
                            )
                        }

                        // Secure Login Screen
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onNavigateToHome = {
                                    navController.navigate("main") {
                                        popUpTo("welcome") { inclusive = true }
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToSignUp = { navController.navigate("signup") }
                            )
                        }

                        // Secure Signup Screen
                        composable("signup") {
                            SignUpScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToHome = {
                                    navController.navigate("main") {
                                        popUpTo("welcome") { inclusive = true }
                                        popUpTo("signup") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Bottom navigation main container
                        composable("main") {
                            MainContainer(
                                viewModel = viewModel,
                                onNavigateToDetails = { id ->
                                    navController.navigate("task_detail/$id")
                                },
                                onNavigateToEdit = { id ->
                                    navController.navigate("add_edit_task?taskId=$id")
                                },
                                onNavigateToAddTask = {
                                    navController.navigate("add_edit_task")
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                },
                                onNavigateToArchive = {
                                    navController.navigate("archive")
                                }
                            )
                        }

                        // Create / Edit Task Screen (Optional taskId)
                        composable(
                            route = "add_edit_task?taskId={taskId}",
                            arguments = listOf(
                                navArgument("taskId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val taskIdString = backStackEntry.arguments?.getString("taskId")
                            val taskId = taskIdString?.toIntOrNull()
                            AddEditTaskScreen(
                                viewModel = viewModel,
                                taskId = taskId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Task Details View Screen
                        composable(
                            route = "task_detail/{taskId}",
                            arguments = listOf(
                                navArgument("taskId") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getInt("taskId") ?: 0
                            TaskDetailsScreen(
                                viewModel = viewModel,
                                taskId = taskId,
                                onNavigateToEdit = { id ->
                                    navController.navigate("add_edit_task?taskId=$id")
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Settings Panel Screen
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                isDarkTheme = isDarkTheme,
                                onThemeToggle = { enabled ->
                                    isDarkTheme = enabled
                                    sharedPrefs.edit().putBoolean("dark_theme", enabled).apply()
                                },
                                onNavigateBack = { navController.popBackStack() },
                                onLogout = {
                                    viewModel.logout()
                                    navController.navigate("welcome") {
                                        popUpTo("main") { inclusive = true }
                                        popUpTo("settings") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Archived Vault Screen
                        composable("archive") {
                            ArchiveScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
