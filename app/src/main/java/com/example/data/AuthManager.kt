package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthManager {
    private const val TAG = "AuthManager"
    
    private var auth: FirebaseAuth? = null
    
    private val _currentUserState = MutableStateFlow<FirebaseUser?>(null)
    val currentUserState: StateFlow<FirebaseUser?> = _currentUserState.asStateFlow()

    fun initialize(context: Context) {
        if (auth != null) return
        
        try {
            // Retrieve values from BuildConfig securely
            val apiKey = if (BuildConfig.FIREBASE_API_KEY.isNotEmpty() && !BuildConfig.FIREBASE_API_KEY.startsWith("AIzaSyFake")) {
                BuildConfig.FIREBASE_API_KEY
            } else {
                "AIzaSyFakeApiKeyForInitializationOnlyAndPrototype"
            }
            
            val appId = if (BuildConfig.FIREBASE_APP_ID.isNotEmpty() && !BuildConfig.FIREBASE_APP_ID.contains("abc123xyz")) {
                BuildConfig.FIREBASE_APP_ID
            } else {
                "1:1234567890:android:abc123xyz"
            }
            
            val projectId = if (BuildConfig.FIREBASE_PROJECT_ID.isNotEmpty() && !BuildConfig.FIREBASE_PROJECT_ID.contains("personal-os-secure-sync")) {
                BuildConfig.FIREBASE_PROJECT_ID
            } else {
                "personal-os-secure-sync"
            }

            Log.d(TAG, "Initializing Firebase with Project ID: $projectId")

            val options = FirebaseOptions.Builder()
                .setApiKey(apiKey)
                .setApplicationId(appId)
                .setProjectId(projectId)
                .build()

            // Try to initialize Firebase
            FirebaseApp.initializeApp(context.applicationContext, options)
            val authInstance = FirebaseAuth.getInstance()
            auth = authInstance
            _currentUserState.value = authInstance.currentUser
            authInstance.addAuthStateListener { firebaseAuth ->
                _currentUserState.value = firebaseAuth.currentUser
                Log.d(TAG, "Auth state updated user ID: ${firebaseAuth.currentUser?.uid}")
            }
            Log.d(TAG, "Firebase initialized dynamically successfully!")
        } catch (e: Exception) {
            Log.e(TAG, "Dynamically initializing Firebase failed, trying default instance", e)
            try {
                // Try fallback to standard google-services.json context configuration if exists
                FirebaseApp.initializeApp(context.applicationContext)
                val authInstance = FirebaseAuth.getInstance()
                auth = authInstance
                _currentUserState.value = authInstance.currentUser
                authInstance.addAuthStateListener { firebaseAuth ->
                    _currentUserState.value = firebaseAuth.currentUser
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Failed both dynamic and standard initialization", ex)
            }
        }
    }

    fun getAuth(): FirebaseAuth {
        val currentAuth = auth
        if (currentAuth == null) {
            Log.w(TAG, "Auth was not initialized, returning standard dynamic fallback")
            return FirebaseAuth.getInstance()
        }
        return currentAuth
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth?.currentUser
    }
}
