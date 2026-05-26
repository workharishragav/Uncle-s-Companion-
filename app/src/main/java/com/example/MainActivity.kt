package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.AppDatabase
import com.example.data.AuthManager
import com.example.data.TrackerRecordRepository
import com.example.ui.TrackerViewModel
import com.example.ui.TrackerViewModelFactory
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.TrackerDashboardScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize authentication framework
        AuthManager.initialize(applicationContext)
        
        // Initialize local Room Database and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TrackerRecordRepository(database.trackerRecordDao())
        
        // Setup ViewModel using Factory custom desugaring
        val viewModel: TrackerViewModel by viewModels {
            TrackerViewModelFactory(application, repository)
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var showSplash by remember { mutableStateOf(true) }
                val currentUser by AuthManager.currentUserState.collectAsState()
                var bypassLogin by remember { mutableStateOf(false) }

                if (showSplash) {
                    com.example.ui.screens.AxisSplashScreen(
                        onSplashFinished = {
                            showSplash = false
                        }
                    )
                } else {
                    if (currentUser != null || bypassLogin) {
                        TrackerDashboardScreen(
                            viewModel = viewModel,
                            onSignOut = {
                                bypassLogin = false
                            }
                        )
                    } else {
                        LoginScreen(
                            onLoginSuccess = {
                                bypassLogin = true
                            }
                        )
                    }
                }
            }
        }
    }
}
