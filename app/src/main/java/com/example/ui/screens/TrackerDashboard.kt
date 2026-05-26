package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.scale
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AuthManager
import com.example.data.TrackerRecord
import java.util.Locale
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import com.example.ui.TrackerViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerDashboardScreen(
    viewModel: TrackerViewModel,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val records by viewModel.records.collectAsStateWithLifecycle()

    val openTasks by viewModel.openTasksCount.collectAsStateWithLifecycle()
    val overdueTasks by viewModel.overdueTasksCount.collectAsStateWithLifecycle()
    val completedLast7Days by viewModel.completedLast7DaysCount.collectAsStateWithLifecycle()
    val financeChecksThisMonth by viewModel.financeChecksThisMonthCount.collectAsStateWithLifecycle()
    val monthlyExpenseTotal by viewModel.monthlyExpenseTotal.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Home, 1: Tasks, 2: Finance, 3: Projects
    var adminModeEnabled by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val todayStr = viewModel.getTodayString()

    val currentHour = remember<Int> { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = remember<String> {
        when (currentHour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    if (showSettingsDialog) {
        val user = AuthManager.getCurrentUser()
        val isAnonymous = user == null
        val accountEmail = user?.email ?: "local_offline_sandbox@ospersonal.com"
        val accountUid = user?.uid ?: "SANDBOX_MOCK_UID_102938"

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings Core",
                        tint = AccentColor
                    )
                    Text(
                        text = "Systems Configuration",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = PrimaryText
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // SECURE PROFILE CARD
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF1F5F9), // Slate 100
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)) // Slate 300
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SECURE IDENTITY KEY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentColor,
                                    letterSpacing = 1.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isAnonymous) Color(0xFFFEF3C7) else Color(0xFFDCFCE7),
                                    border = BorderStroke(1.dp, if (isAnonymous) Color(0xFFF59E0B) else Color(0xFF22C55E))
                                ) {
                                    Text(
                                        text = if (isAnonymous) "Offline Sandbox" else "Cloud Synced",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAnonymous) Color(0xFFB45309) else Color(0xFF15803D),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = accountEmail,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )

                            Text(
                                text = "UID: $accountUid",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = SecondaryText
                            )
                        }
                    }

                    // SWITCH ACTIONS OR FEATURES
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "PREFERENCES & TOOLING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText,
                            letterSpacing = 0.5.sp
                        )

                        // 1. Admin mode toggle Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { adminModeEnabled = !adminModeEnabled }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = "Admin Grid",
                                    tint = SecondaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Admin Spreadsheet Mode",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryText
                                    )
                                    Text(
                                        text = "Activate advanced raw spreadsheet auditing columns",
                                        fontSize = 10.sp,
                                        color = SecondaryText
                                    )
                                }
                            }
                            Switch(
                                checked = adminModeEnabled,
                                onCheckedChange = { adminModeEnabled = it },
                                modifier = Modifier.scale(0.8f) // Scale it slightly to match dense UI
                            )
                        }

                        // 2. Tutorial Reset Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.resetOnboarding()
                                    Toast.makeText(context, "Guided Axis Tour enabled! Switch to Today tab to view.", Toast.LENGTH_LONG).show()
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = "Show Tutorial",
                                    tint = SecondaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Guided Axis Tour",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryText
                                    )
                                    Text(
                                        text = "Restore first-launch guided tours and center alignment tips",
                                        fontSize = 10.sp,
                                        color = SecondaryText
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Reset Tour",
                                tint = SecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // SECURITY NOTICE FOR CORES
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "System Log",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Unified accounts are linked to Apple Keychain, iCloud, and Google Cloud credentials automatically.",
                            fontSize = 10.sp,
                            color = Color(0xFF1D4ED8),
                            lineHeight = 14.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSettingsDialog = false
                        // Process sign out flow
                        AuthManager.getAuth().signOut()
                        onSignOut()
                        Toast.makeText(context, "Logged out of AXIS Operating Center", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Log Out",
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Log Out", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "$greeting 👋",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AxisCompassLogo(
                                sizeDp = 18,
                                animateNeedle = false
                            )
                            Text(
                                text = "AXIS • LIFE ALIGNMENT BASE",
                                fontSize = 13.sp,
                                color = SecondaryText,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                },
                actions = {
                    // Styled row of 4 action icons matching the screenshot layout with enlarged targets
                    Row(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Action 1: Search
                        IconButton(
                            onClick = { Toast.makeText(context, "Search feature coming soon!", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.size(44.dp).minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = SecondaryText,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Action 2: Save File / Reload Templates
                        IconButton(
                            onClick = {
                                viewModel.resetDatabase()
                                Toast.makeText(context, "Loaded fresh templates & synchronized layout", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(44.dp).minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Reload Templates",
                                tint = SecondaryText,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Action 3: NightMode / Moon Moon Toggle
                        IconButton(
                            onClick = { Toast.makeText(context, "Night Mode cozy preset active", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.size(44.dp).minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = "Toggle Night Mode",
                                tint = SecondaryText,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Action 4: Settings / Account Profile & Systems Configuration
                        IconButton(
                            onClick = {
                                showSettingsDialog = true
                            },
                            modifier = Modifier
                                .testTag("admin_mode_toggle")
                                .size(44.dp)
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Toggle Settings",
                                tint = if (adminModeEnabled || showSettingsDialog) AccentColor else SecondaryText,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackground,
                    titleContentColor = PrimaryText
                )
            )
        },
        bottomBar = {
            CustomBottomNavBar(
                activeTab = activeTab,
                onTabSelected = { activeTab = it }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SecondarySurface,
                contentColor = AccentColor,
                modifier = Modifier
                    .size(56.dp)
                    .padding(bottom = 4.dp)
                    .testTag("add_record_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Item",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .background(AppBackground)
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                },
                label = "ScreenSwitchAnimation"
            ) { targetTab ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    when (targetTab) {
                        0 -> TodayTabView(
                            viewModel = viewModel,
                            records = records,
                            adminMode = adminModeEnabled,
                            onModifyRecord = { viewModel.updateRecord(it) },
                            onNextTab = { activeTab = 1 }
                        )
                        1 -> WeeklyTabView(
                            viewModel = viewModel,
                            records = records,
                            adminMode = adminModeEnabled,
                            onAddRecord = { showAddDialog = true }
                        )
                        2 -> MonthlyTabView(
                            viewModel = viewModel,
                            records = records,
                            adminMode = adminModeEnabled,
                            onAddRecord = { showAddDialog = true }
                        )
                        3 -> RPMTabView(
                            viewModel = viewModel,
                            records = records,
                            adminMode = adminModeEnabled,
                            onAddRecord = { showAddDialog = true }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddRecordDialog(
                onDismiss = { showAddDialog = false },
                onSave = { record ->
                    viewModel.addRecord(record)
                    showAddDialog = false
                    Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun TodayTabView(
    viewModel: TrackerViewModel,
    records: List<TrackerRecord>,
    adminMode: Boolean,
    onModifyRecord: (TrackerRecord) -> Unit,
    onNextTab: () -> Unit
) {
    val today = viewModel.getTodayString()
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("personal_os_prefs", android.content.Context.MODE_PRIVATE) }
    
    // SAVERS Morning Routine checks (Silence, Affirmations, Visualization, Exercise, Read, Scribe)
    val saversList = listOf(
        Pair("Silence (S)", "Breathing, quiet contemplation and grounding exercise"),
        Pair("Affirmations (A)", "\"Everything is working in my favor. Good is coming to me every day.\""),
        Pair("Visualization (V)", "Hydrate with water therapy. Focus on physical, mental, and wealth fitness."),
        Pair("Exercise (E)", "Badminton workout or active physical play"),
        Pair("Reading (R)", "Read and study your Goal Card to align sub-conscious focus"),
        Pair("Scribing (S)", "Morning scripting & Pre-Performance Blueprint")
    )
    
    val saversChecked = remember(today) {
        val map = mutableStateMapOf<Int, Boolean>()
        for (i in 0..5) {
            map[i] = prefs.getBoolean("savers_${today}_$i", false)
        }
        map
    }
    val completedCount = saversChecked.values.count { it }
    
    // High Priority item focus
    val top3Focus = remember(records) {
        records.filter { it.recordType == "Daily Task" && it.priority == "High" && it.status != "Done" }.take(3)
    }

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

    val onboardingCompleted by viewModel.onboardingCompleted.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Simple, Spacious Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Alignment Center",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AccentColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, AccentColor.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = "Active Layer 1",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
            Text(
                text = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US).format(Date()),
                fontSize = 15.sp,
                color = SecondaryText,
                fontWeight = FontWeight.Medium
            )
        }

        // --- Interactive First-time Onboarding ---
        if (!onboardingCompleted) {
            var onboardingStep by remember { mutableStateOf(1) }
            
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AccentColor.copy(alpha = 0.05f),
                border = BorderStroke(1.5.dp, AccentColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AxisCompassLogo(
                                sizeDp = 22,
                                animateNeedle = false
                            )
                            Text(
                                text = "GUIDED TOUR • $onboardingStep OF 3",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentColor,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        Text(
                            text = "Skip Tour",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.completeOnboarding() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = when (onboardingStep) {
                            1 -> "The Daily Axis"
                            2 -> "Operational Stability"
                            else -> "Intentional Direction"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )

                    Text(
                        text = when (onboardingStep) {
                            1 -> "Axis is your central point of daily alignment. Dedicating time block 5:00 AM to 6:00 AM for the critical SAVERS routine (Silence, Affirmations, Visualization, Exercise, Reading, Scribing) anchors your day with structured, intentional stability before active execution begins."
                            2 -> "Clear away the noise of endless lists by defining and centering your Top 3 High-Priority Tasks. Concentrated focus keeps you resiliently grounded, preventing cognitive drift."
                            else -> "A true axis balances daily routines with long-term horizons. Use the primary navigation tabs (Today, Weekly, Monthly, RPM) to seamlessly coordinate weekly persistence lists, monthly strategic milestones, and Mass Action Plans."
                        },
                        fontSize = 14.sp,
                        color = SecondaryText,
                        lineHeight = 20.sp
                    )

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Indicators
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (1..3).forEach { step ->
                                Box(
                                    modifier = Modifier
                                        .size(if (onboardingStep == step) 16.dp else 8.dp, 8.dp)
                                        .clip(CircleShape)
                                        .background(if (onboardingStep == step) AccentColor else BorderColor)
                                )
                            }
                        }

                        // Buttons with generous touch targets (48dp+)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (onboardingStep > 1) {
                                OutlinedButton(
                                    onClick = { onboardingStep-- },
                                    modifier = Modifier.height(44.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp)
                                ) {
                                    Text("Back", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    if (onboardingStep < 3) {
                                        onboardingStep++
                                    } else {
                                        viewModel.completeOnboarding()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                                modifier = Modifier.height(44.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = if (onboardingStep == 3) "Let's Begin" else "Next",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Combined Harmony Progress Card ---
        val todayHighPriorityTasks = remember(records) {
            records.filter { it.recordType == "Daily Task" && it.priority == "High" }
        }
        val completedHighPriorityCount = todayHighPriorityTasks.count { it.status == "Done" }
        val totalHighPriorityCount = todayHighPriorityTasks.size
        
        val totalChecks = 6 + totalHighPriorityCount
        val completedChecks = completedCount + completedHighPriorityCount
        val alignmentPercent = (completedChecks.toFloat() / totalChecks * 100).toInt()

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MainSurface,
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = "Harmony",
                            tint = AccentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Daily Alignment Status",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }
                    Text(
                        text = "$alignmentPercent% complete",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (alignmentPercent == 100) StatusDone else AccentColor
                    )
                }
                
                // ProgressBar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = SecondarySurface
                ) {
                    val progressWidthPercent = completedChecks.toFloat() / totalChecks
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(if (progressWidthPercent > 0) progressWidthPercent else 0.0001f)
                                .background(if (alignmentPercent == 100) StatusDone else AccentColor)
                        )
                        if (progressWidthPercent < 1f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f - progressWidthPercent)
                                    .background(Color.Transparent)
                            )
                        }
                    }
                }
                
                Text(
                    text = when {
                        alignmentPercent == 100 -> "✨ Perfect Harmony! All morning rituals and high priority tasks are fully executed."
                        alignmentPercent >= 70 -> "🌿 Beautiful progress today. You have established a solid baseline of mindfulness."
                        alignmentPercent >= 40 -> "⚡ Steady pace. Complete your active deep focus blocks to unlock high-value results."
                        else -> "🌅 A quiet morning start. Tap any morning ritual or priority task below to begin."
                    },
                    fontSize = 13.sp,
                    color = SecondaryText,
                    lineHeight = 18.sp
                )
            }
        }

        // SAVERS Checklist Widget
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MainSurface,
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SAVERS Morning Victory Hour",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Gratitude + 5AM Victory hour SAVERS + Health",
                            fontSize = 13.sp,
                            color = SecondaryText
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (completedCount == 6) StatusDone.copy(alpha = 0.15f) else SecondarySurface,
                        border = BorderStroke(1.dp, if (completedCount == 6) StatusDone else BorderColor)
                    ) {
                        Text(
                            text = "$completedCount / 6 Done",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (completedCount == 6) StatusDone else AccentColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                HorizontalDivider(color = BorderColor)

                saversList.forEachIndexed { index, saver ->
                    val isChecked = saversChecked[index] ?: false
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                val nextChecked = !isChecked
                                saversChecked[index] = nextChecked
                                prefs.edit().putBoolean("savers_${today}_$index", nextChecked).apply()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = "Toggle Complete",
                            tint = if (isChecked) StatusDone else SecondaryText,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = saver.first,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isChecked) SecondaryText else PrimaryText,
                                style = if (isChecked) TextStyle(textDecoration = TextDecoration.LineThrough) else TextStyle.Default
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = saver.second,
                                fontSize = 13.sp,
                                color = SecondaryText,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Calm victory state reinforcement trigger
        if (completedCount == 6) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = StatusDone.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, StatusDone.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(StatusDone.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Victory Hour achieved",
                            tint = StatusDone,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Morning Victory Complete! 🧘‍♂️",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Calm clarity achieved. Your morning victory is sealed. You are prepared for deep work focus.",
                            fontSize = 13.sp,
                            color = SecondaryText,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Top 3 Focus Daily targets
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "📌 Focus Top 3 High Value Daily",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SecondaryText,
                letterSpacing = 0.8.sp
            )

            if (top3Focus.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SecondarySurface,
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Mind What Matters the Most",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Text(
                            text = "Define your high-value actions to prevent getting bogged down. System defaults:\n1. Gratitude & 5AM Rise\n2. S-A-V-E-R-S Execution\n3. World-class Deep Work Block (90/90/1 rule)\n\nTap the bottom-right + button to create a custom High Priority Task!",
                            fontSize = 14.sp,
                            color = SecondaryText,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    top3Focus.forEach { record ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MainSurface,
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.toggleRecordCompleted(record) },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = if (record.status == "Done") Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = "Toggle Complete",
                                        tint = if (record.status == "Done") StatusDone else AccentColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.toDoItem,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (record.status == "Done") SecondaryText else PrimaryText,
                                        style = if (record.status == "Done") TextStyle(textDecoration = TextDecoration.LineThrough) else TextStyle.Default
                                    )
                                    if (record.purpose.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Purpose: ${record.purpose}",
                                            fontSize = 13.sp,
                                            color = SecondaryText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Chronometer timeline schedule
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "⚡ Daily Time Block Chronometer",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SecondaryText,
                letterSpacing = 0.8.sp
            )

            val blocks = listOf(
                Triple("5.00AM - 5.15AM", "Optimal Rise & Water Therapy", "Hydrate immediately to fuel mitochondria. Visualize physical, mental and spiritual fitness"),
                Triple("5.15AM - 5.50AM", "SAVERS Victory Hour", "Focus S-A-V-E-R-S in quiet Silence with deep reflection"),
                Triple("5.50AM - 8.30AM", "Movement & Workout", "Badminton tournament workouts, core exercises, and devotions"),
                Triple("9.00AM - 9.30AM", "Traffic University", "Prepare for office. Listen to the Strangest Secret. No News. No Feeds"),
                Triple("9.30AM - 11.00AM", "Deep Focus (90/90/1 Rule)", "Execute on the single highest strategic priority elite cycle"),
                Triple("11.00AM - 1.00PM", "World-class Work (60/10 Method)", "TBTF Protocol & your personal virtual Park study"),
                Triple("4.00PM - 6.30PM", "5PM Next Day Planner & Sync", "Planning official priorities. Adminstrivia constraints and hydration"),
                Triple("7.30PM - 9.30PM", "Family Meal & Nature Walk", "No digital screens. Joyful nature walk, connect with kids and companion"),
                Triple("9.30PM - 10.00PM", "Nightly Sleep Rituals", "Plan vs actual verification, read Goal Card, listen to Strangest Secret")
            )

            val activeIndex = when (currentHour) {
                5 -> if (Calendar.getInstance().get(Calendar.MINUTE) < 15) 0 else 1
                in 6..8 -> 2
                9 -> 3
                10 -> 4
                in 11..13 -> 5
                in 14..18 -> 6
                19, 20 -> 7
                else -> 8
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MainSurface,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    blocks.forEachIndexed { index, block ->
                        val isActive = index == activeIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.width(110.dp)) {
                                Text(
                                    text = block.first,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) AccentColor else SecondaryText
                                )
                                if (isActive) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = AccentColor.copy(alpha = 0.12f),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE NOW",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (isActive) 16.dp else 11.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) AccentColor else BorderColor)
                                )
                                if (index < blocks.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(40.dp)
                                            .background(BorderColor)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = block.second,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) AccentColor else PrimaryText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = block.third,
                                    fontSize = 13.sp,
                                    color = SecondaryText,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Nightly Shutdown & Operational Reminders
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "🌙 Daily Operational Reminders & Shutdown",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SecondaryText,
                letterSpacing = 0.8.sp
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MainSurface,
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Nightly Shutdown Checklist",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Seal cognitive circles & prepare tomorrow's launchpad",
                                fontSize = 13.sp,
                                color = SecondaryText
                            )
                        }

                        val shutdownList = listOf(
                            Pair("Ledger Reconciliation", "Verify daily transactions vs budget limits"),
                            Pair("Launchpad Alignment", "Pre-commit to tomorrow's Top 3 Deep Work blocks"),
                            Pair("Digital Screen Blackout", "Power down all corporate servers, screens & terminals"),
                            Pair("Subconscious Seed", "Review long-range sub-conscious goals card")
                        )

                        val shutdownChecked = remember(today) {
                            val map = mutableStateMapOf<Int, Boolean>()
                            for (i in 0..3) {
                                map[i] = prefs.getBoolean("shutdown_${today}_$i", false)
                            }
                            map
                        }
                        val completedShutdownCount = shutdownChecked.values.count { it }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (completedShutdownCount == 4) StatusDone.copy(alpha = 0.15f) else SecondarySurface,
                            border = BorderStroke(1.dp, if (completedShutdownCount == 4) StatusDone else BorderColor)
                        ) {
                            Text(
                                text = "$completedShutdownCount / 4 Done",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (completedShutdownCount == 4) StatusDone else AccentColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = BorderColor)

                    val shutdownList = listOf(
                        Pair("Ledger Reconciliation", "Verify daily transactions vs budget limits"),
                        Pair("Launchpad Alignment", "Pre-commit to tomorrow's Top 3 Deep Work blocks"),
                        Pair("Digital Screen Blackout", "Power down all corporate servers, screens & terminals"),
                        Pair("Subconscious Seed", "Review long-range sub-conscious goals card")
                    )

                    val shutdownChecked = remember(today) {
                        val map = mutableStateMapOf<Int, Boolean>()
                        for (i in 0..3) {
                            map[i] = prefs.getBoolean("shutdown_${today}_$i", false)
                        }
                        map
                    }

                    shutdownList.forEachIndexed { index, item ->
                        val isChecked = shutdownChecked[index] ?: false
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val nextChecked = !isChecked
                                    shutdownChecked[index] = nextChecked
                                    prefs.edit().putBoolean("shutdown_${today}_$index", nextChecked).apply()
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = "Toggle Complete",
                                tint = if (isChecked) StatusDone else SecondaryText,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.first,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChecked) SecondaryText else PrimaryText,
                                    style = if (isChecked) TextStyle(textDecoration = TextDecoration.LineThrough) else TextStyle.Default
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.second,
                                    fontSize = 13.sp,
                                    color = SecondaryText,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = BorderColor)

                    // Daily Reflection Prompts
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "💡 ALIGNMENT REFLECTION PROMPTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentColor,
                            letterSpacing = 1.sp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SecondarySurface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Calibration Reminder:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText
                                    )
                                    Text(
                                        text = "Did your high-priority block today solve a strategic bottleneck, or did you default to reactive administrative fire-fighting?",
                                        fontSize = 13.sp,
                                        color = SecondaryText,
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SecondarySurface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Transition Prompt:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText
                                    )
                                    Text(
                                        text = "Where did you observe cognitive drift or impatience today? Recalibrate to centerness before entering family conversation spaces.",
                                        fontSize = 13.sp,
                                        color = SecondaryText,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyTabView(
    viewModel: TrackerViewModel,
    records: List<TrackerRecord>,
    adminMode: Boolean,
    onAddRecord: () -> Unit
) {
    val weeklyGoals = remember(records) {
        records.filter { it.recordType == "Weekly Goal" }
    }

    var showSlotEditDialog by remember { mutableStateOf(false) }
    var editingSlotNum by remember { mutableStateOf(1) }
    var editingGoalText by remember { mutableStateOf("") }

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // Spacious Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Weekly Perspective",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Text(
                text = "Layer 2: Goals rhythm, Carry-forward & Planning buckets",
                fontSize = 14.sp,
                color = SecondaryText,
                fontWeight = FontWeight.Medium
            )
        }

        // Weekly Executive Reflection & Calibration Summary Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MainSurface,
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Summarize,
                            contentDescription = "Weekly Summary",
                            tint = AccentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Weekly Executive Review",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StatusDone.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, StatusDone.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "84% COMPLETED",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StatusDone,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

                // Multi-parameter review dashboard summaries
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(SecondarySurface, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "INTEGRAL FOCUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText
                        )
                        Text(
                            text = "28.5 hrs Deep Work",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryText
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(SecondarySurface, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "ALIGNMENT PATH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryText
                        )
                        Text(
                            text = "Stable & Structured",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentColor
                        )
                    }
                }

                // Bulleted Lived-In Operational Progress Notes
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "🏆 WEEKLY WINS & COMPENSATING MILESTONES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusDone,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "• Refactored system sync engine database persistence layer on SQLite schemas, mitigating data-loss risk.\n• Completed Zero-latency AXIS branding vector paths integration across compact/expanded adaptive screens.\n• Completed active physical badminton workouts and successfully maintained consistent 5:00 AM rise routine.",
                        fontSize = 13.sp,
                        color = SecondaryText,
                        lineHeight = 18.sp
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "⚠️ OPERATIONAL BOTTLENECK & CORRECTIVE SCHEDULING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE11D48), // Rose 600
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "• Slow CSV import parsers causing lateral UI thread delays during budget spreadsheet audit. Corrective Action: Scheduled an asynchronous coroutine stream background task for tomorrow morning's focus block.",
                        fontSize = 13.sp,
                        color = SecondaryText,
                        lineHeight = 18.sp
                    )
                }

                HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

                // Weekly Alignment Prompts
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💡 WEEKLY PERSPECTIVE REFLECTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentColor,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Did my week's priority outcomes direct and compound the monthly strategic layer, or did I drift into convenient lateral work? Identify the single biggest system paradigm tweak needed for upcoming Monday launchpad.",
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = SecondaryText,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Goal Slots Panel
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MainSurface),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Weekly Goals (Slot 1 to 5)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )

                for (slotIndex in 1..5) {
                    val matchingGoal = weeklyGoals.getOrNull(slotIndex - 1)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (matchingGoal != null) Color.Transparent else SecondarySurface.copy(alpha = 0.4f))
                            .clickable {
                                editingSlotNum = slotIndex
                                editingGoalText = matchingGoal?.toDoItem ?: ""
                                showSlotEditDialog = true
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (matchingGoal?.status == "Done") StatusDone.copy(alpha = 0.15f) else AccentColor.copy(alpha = 0.1f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                  Text(
                                      slotIndex.toString(),
                                      fontSize = 14.sp,
                                      fontWeight = FontWeight.ExtraBold,
                                      color = if (matchingGoal?.status == "Done") StatusDone else AccentColor
                                  )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            if (matchingGoal != null) {
                                Text(
                                    matchingGoal.toDoItem,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (matchingGoal.status == "Done") SecondaryText else PrimaryText,
                                    style = if (matchingGoal.status == "Done") TextStyle(textDecoration = TextDecoration.LineThrough) else TextStyle.Default
                                )
                            } else {
                                Text(
                                    "Slot #$slotIndex: Tap to set a weekly goal",
                                    fontSize = 14.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = AccentColor.copy(alpha = 0.8f)
                                )
                            }
                        }

                        if (matchingGoal != null) {
                            IconButton(
                                onClick = { viewModel.toggleRecordCompleted(matchingGoal) },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = if (matchingGoal.status == "Done") Icons.Default.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                    contentDescription = "Complete goal",
                                    tint = if (matchingGoal.status == "Done") StatusDone else SecondaryText,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daily Buckets
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 Monday to Sunday Planning Buckets",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SecondaryText,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Add Activity +",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentColor,
                    modifier = Modifier
                        .clickable { onAddRecord() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            daysOfWeek.forEach { dayName ->
                val dayRecords = records.filter { it.dayOfWeek.equals(dayName, ignoreCase = true) }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MainSurface,
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dayName.uppercase(Locale.US),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentColor,
                                letterSpacing = 0.5.sp
                            )
                            if (dayRecords.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SecondarySurface
                                ) {
                                    Text(
                                        text = "${dayRecords.size} items",
                                        fontSize = 12.sp,
                                        color = SecondaryText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (dayRecords.isEmpty()) {
                            Text(
                                text = when (dayName) {
                                    "Monday" -> "• kids hours 7:00-7:30 AM"
                                    "Sunday" -> "• Weekly Portfolio Review (Groww/Zerodha/Zebpay check)"
                                    else -> "No tasks bucketed yet. Click Add Activity above to set."
                                },
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic,
                                color = SecondaryText,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            dayRecords.forEach { rec ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.toggleRecordCompleted(rec) },
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (rec.status == "Done") Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                            contentDescription = "Quick Complete",
                                            tint = if (rec.status == "Done") StatusDone else SecondaryText,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Text(
                                        text = rec.toDoItem,
                                        fontSize = 15.sp,
                                        color = if (rec.status == "Done") SecondaryText else PrimaryText,
                                        style = if (rec.status == "Done") TextStyle(textDecoration = TextDecoration.LineThrough) else TextStyle.Default,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSlotEditDialog) {
        val matchingGoal = weeklyGoals.getOrNull(editingSlotNum - 1)
        AlertDialog(
            onDismissRequest = { showSlotEditDialog = false },
            title = { Text("Edit Weekly Goal #$editingSlotNum") },
            text = {
                OutlinedTextField(
                    value = editingGoalText,
                    onValueChange = { editingGoalText = it },
                    label = { Text("Goal Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editingGoalText.isNotEmpty()) {
                            if (matchingGoal != null) {
                                viewModel.updateRecord(matchingGoal.copy(toDoItem = editingGoalText))
                            } else {
                                viewModel.addRecord(
                                    TrackerRecord(
                                        recordType = "Weekly Goal",
                                        timeHorizon = "Weekly",
                                        toDoItem = editingGoalText,
                                        priority = "Medium",
                                        status = "Not Started",
                                        purpose = "Weekly priority goal"
                                    )
                                )
                            }
                        } else if (matchingGoal != null) {
                            viewModel.deleteRecord(matchingGoal)
                        }
                        showSlotEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSlotEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MonthlyTabView(
    viewModel: TrackerViewModel,
    records: List<TrackerRecord>,
    adminMode: Boolean,
    onAddRecord: () -> Unit
) {
    val monthlyGoals = remember(records) {
        records.filter { it.recordType == "Monthly Goal" }
    }

    var showSlotEditDialog by remember { mutableStateOf(false) }
    var editingSlotNum by remember { mutableStateOf(1) }
    var editingGoalText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Monthly Strategic Layer",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Text(
                text = "Layer 3: Corporate and long-range monthly planning",
                fontSize = 14.sp,
                color = SecondaryText,
                fontWeight = FontWeight.Medium
            )
        }

        // Strategic Monthly Goals
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MainSurface),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Monthly goals (Slot 1 to 5)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )

                for (slotIndex in 1..5) {
                    val matchingGoal = monthlyGoals.getOrNull(slotIndex - 1)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (matchingGoal != null) Color.Transparent else SecondarySurface.copy(alpha = 0.4f))
                            .clickable {
                                editingSlotNum = slotIndex
                                editingGoalText = matchingGoal?.toDoItem ?: ""
                                showSlotEditDialog = true
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (matchingGoal?.status == "Done") StatusDone.copy(alpha = 0.15f) else AccentColor.copy(alpha = 0.1f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    slotIndex.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (matchingGoal?.status == "Done") StatusDone else AccentColor
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            if (matchingGoal != null) {
                                Text(
                                    matchingGoal.toDoItem,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (matchingGoal.status == "Done") SecondaryText else PrimaryText,
                                    style = if (matchingGoal.status == "Done") TextStyle(textDecoration = TextDecoration.LineThrough) else TextStyle.Default
                                )
                            } else {
                                Text(
                                    "Slot #$slotIndex: Tap to set a monthly goal",
                                    fontSize = 14.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = AccentColor.copy(alpha = 0.8f)
                                )
                            }
                        }

                        if (matchingGoal != null) {
                            IconButton(
                                onClick = { viewModel.toggleRecordCompleted(matchingGoal) },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = if (matchingGoal.status == "Done") Icons.Default.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                    contentDescription = "Complete goal",
                                    tint = if (matchingGoal.status == "Done") StatusDone else SecondaryText,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Important Dates
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "📌 Important Calendar Dates",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SecondaryText,
                letterSpacing = 0.8.sp
            )

            val milestones = listOf(
                Pair("May 5", "Optimize monthly outline & SAVERS alignment"),
                Pair("May 10", "Kids school fee & family portfolio review"),
                Pair("May 15", "Active project mid-way results review"),
                Pair("May 28", "Comprehensive ledger & asset spreadsheet compile"),
                Pair("May 30", "Next month RPM initiative sequence planning")
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MainSurface,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    milestones.forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SecondarySurface,
                                border = BorderStroke(1.dp, BorderColor),
                                modifier = Modifier.width(75.dp)
                            ) {
                                Text(
                                    text = m.first,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = m.second,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSlotEditDialog) {
        val matchingGoal = monthlyGoals.getOrNull(editingSlotNum - 1)
        AlertDialog(
            onDismissRequest = { showSlotEditDialog = false },
            title = { Text("Edit Monthly Goal #$editingSlotNum") },
            text = {
                OutlinedTextField(
                    value = editingGoalText,
                    onValueChange = { editingGoalText = it },
                    label = { Text("Goal Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editingGoalText.isNotEmpty()) {
                            if (matchingGoal != null) {
                                viewModel.updateRecord(matchingGoal.copy(toDoItem = editingGoalText))
                            } else {
                                viewModel.addRecord(
                                    TrackerRecord(
                                        recordType = "Monthly Goal",
                                        timeHorizon = "Monthly",
                                        toDoItem = editingGoalText,
                                        priority = "Medium",
                                        status = "Not Started",
                                        purpose = "Monthly strategic goal"
                                    )
                                )
                            }
                        } else if (matchingGoal != null) {
                            viewModel.deleteRecord(matchingGoal)
                        }
                        showSlotEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSlotEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RPMTabView(
    viewModel: TrackerViewModel,
    records: List<TrackerRecord>,
    adminMode: Boolean,
    onAddRecord: () -> Unit
) {
    val rpmInitiatives = remember(records) {
        records.filter { it.recordType == "Project" }
    }
    var editingRecordForRpm by remember { mutableStateOf<TrackerRecord?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "RPM Strategic MAP",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Text(
                text = "Results Area • Purpose Tracker • Massive Action Plans",
                fontSize = 14.sp,
                color = AccentColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        // Informative Core Vibe
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SecondarySurface,
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = AccentColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Tony Robbins' RPM Alignment: Designate target RESULTS, clarify the emotional PURPOSE (Why), and execute key activities via Massive Action Plan (MAP).",
                    fontSize = 13.sp,
                    color = SecondaryText,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Initiative controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📋 Transformation Initiatives",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SecondaryText,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "Create Initiative +",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AccentColor,
                modifier = Modifier
                    .clickable { onAddRecord() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        if (rpmInitiatives.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No initiatives generated yet. Tap Create Initiative + to build one!",
                    fontSize = 15.sp,
                    color = SecondaryText,
                    fontStyle = FontStyle.Italic
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                rpmInitiatives.forEach { record ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MainSurface,
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = record.projectCategory.uppercase(Locale.US),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentColor,
                                    letterSpacing = 0.5.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (record.status == "Done") StatusDone.copy(alpha = 0.15f) else SecondarySurface,
                                    border = BorderStroke(1.dp, if (record.status == "Done") StatusDone else BorderColor)
                                ) {
                                    Text(
                                        text = record.status,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (record.status == "Done") StatusDone else PrimaryText,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Result
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "1. TARGET RESULT STATEMENT",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryText
                                )
                                Text(
                                    text = record.toDoItem,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryText
                                )
                            }

                            // Purpose
                            if (record.purpose.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = AccentColor.copy(alpha = 0.05f),
                                    border = BorderStroke(1.dp, AccentColor.copy(alpha = 0.1f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "2. DRIVING PURPOSE (WHY)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentColor,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = record.purpose,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            fontStyle = FontStyle.Italic,
                                            color = PrimaryText
                                        )
                                    }
                                }
                            }

                            // Action MAP Plan steps
                            if (record.actionPlan.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "3. MASSIVE ACTION PLAN (MAP) CHUNKS",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SecondaryText
                                    )
                                    record.actionPlan.split("\n").forEach { step ->
                                        if (step.trim().isNotEmpty()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = AccentColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = step,
                                                    fontSize = 15.sp,
                                                    color = PrimaryText,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (record.targetDate.isNotEmpty()) {
                                    Text(
                                        text = "Target Date: ${record.targetDate}",
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = SecondaryText
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = { editingRecordForRpm = record },
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit initiative",
                                            tint = AccentColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteRecord(record) },
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete initiative",
                                            tint = StatusOverdue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.toggleRecordCompleted(record) },
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (record.status == "Done") Icons.Default.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                            contentDescription = "Complete project",
                                            tint = if (record.status == "Done") StatusDone else SecondaryText,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
         }
     }

     if (editingRecordForRpm != null) {
         EditRecordDialog(
             record = editingRecordForRpm!!,
             onDismiss = { editingRecordForRpm = null },
             onSave = { updated ->
                 viewModel.updateRecord(updated)
                 editingRecordForRpm = null
             }
         )
     }
 }

// ==========================================
// 2. MASTER TRACKER VIEW DEFINITION
// ==========================================
@Composable
fun MasterTrackerTabView(
    records: List<TrackerRecord>,
    viewModel: TrackerViewModel,
    adminMode: Boolean,
    onUpdateRecord: (TrackerRecord) -> Unit,
    onDeleteRecord: (TrackerRecord) -> Unit,
    onToggleComplete: (TrackerRecord) -> Unit,
    initialFilter: String = "All"
) {
    // Elegant tab-synchronized default filters
    val defaultSelected = remember(initialFilter) {
        when (initialFilter) {
            "Daily Task" -> "All Tasks"
            else -> initialFilter
        }
    }
    var selectedFilter by remember(defaultSelected) { mutableStateOf(defaultSelected) }

    val categories = remember(initialFilter) {
        when (initialFilter) {
            "Daily Task" -> listOf("All Tasks", "Daily Task", "Goal")
            "Finance Review" -> listOf("Finance Review")
            "Project" -> listOf("Project")
            else -> listOf("All", "Daily Task", "Project", "Finance Review", "Goal")
        }
    }

    val filteredRecords = remember(records, selectedFilter) {
        when (selectedFilter) {
            "All Tasks" -> records.filter { it.recordType == "Daily Task" || it.recordType == "Weekly Goal" || it.recordType == "Monthly Goal" }
            "Daily Task" -> records.filter { it.recordType == "Daily Task" }
            "Goal" -> records.filter { it.recordType == "Weekly Goal" || it.recordType == "Monthly Goal" }
            "Finance Review" -> records.filter { it.recordType == "Finance Review" }
            "Project" -> records.filter { it.recordType == "Project" }
            "All" -> records
            else -> records.filter { it.recordType == selectedFilter }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Fast Categories Filter
        if (categories.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) AccentColor else SecondarySurface)
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else PrimaryText
                        )
                    }
                }
            }
        }

        // Counter label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredRecords.size} Records listed",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryText
            )
            
            if (adminMode) {
                Text(
                    text = "Raw Columns Enabled",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentColor
                )
            }
        }

        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = "Empty Records",
                        tint = SecondaryText,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No records here",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Use the '+ Add Item' floating button to add dynamic entries.",
                        fontSize = 14.sp,
                        color = SecondaryText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredRecords, key = { it.id }) { record ->
                    TrackerRecordRow(
                        record = record,
                        viewModel = viewModel,
                        adminMode = adminMode,
                        onUpdate = onUpdateRecord,
                        onDelete = onDeleteRecord,
                        onToggleDone = { onToggleComplete(record) }
                    )
                }
            }
        }
    }
}

// ==========================================
// INDIVIDUAL ROW COMPONENT
// ==========================================
@Composable
fun TrackerRecordRow(
    record: TrackerRecord,
    viewModel: TrackerViewModel,
    adminMode: Boolean,
    onUpdate: (TrackerRecord) -> Unit,
    onDelete: (TrackerRecord) -> Unit,
    onToggleDone: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var editDialogVisible by remember { mutableStateOf(false) }

    // Pre-calculate custom spreadsheet equivalent properties
    val isDone = record.status == "Done"
    val isOverdue = viewModel.detectIsOverdue(record.targetDate, record.status)
    val daysToDeadline = viewModel.calculateDaysUntilDeadline(record.targetDate)
    val agingDays = viewModel.calculateAgingDays(record.createdDate, record.status)
    val completionPercent = viewModel.getCompletionPercentage(record.status)

    val rowColor = if (isDone) MainSurface.copy(alpha = 0.6f) else MainSurface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("tracker_row_${record.id}"),
        shape = RoundedCornerShape(16.dp),
        color = rowColor,
        border = BorderStroke(
            1.dp,
            if (isOverdue) StatusOverdue.copy(alpha = 0.8f) else BorderColor.copy(alpha = 0.6f)
        ),
        shadowElevation = 2.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Checkbox/Finish, Meta icons, Titles, Priority badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Large Checkbox / Circle Toggle - Big Target for Seniors
                IconButton(
                    onClick = { onToggleDone() },
                    modifier = Modifier
                        .size(44.dp)
                        .minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = "Toggle Complete",
                        tint = if (isDone) StatusDone else AccentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Type Badge & Priority Label
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SecondarySurface,
                            modifier = Modifier.height(24.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = record.recordType,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentColor
                                )
                            }
                        }

                        // Priority Label
                        PriorityBadge(priority = record.priority)

                        // Overdue Flag
                        if (isOverdue) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = StatusOverdue.copy(alpha = 0.15f),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "OVERDUE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusOverdue
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Title
                    val titleText = when (record.recordType) {
                        "Daily Task" -> record.toDoItem
                        "Weekly Goal" -> record.weeklyGoal
                        "Monthly Goal" -> record.monthlyGoal
                        "Project" -> record.routineActivity
                        "Finance Review" -> "${record.toDoItem} - Expense Update"
                        else -> record.toDoItem
                    }

                    Text(
                        text = titleText.ifEmpty { "Unlabeled entry" },
                        fontSize = 18.sp, // Large, high-legibility title font for seniors
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDone) SecondaryText else PrimaryText,
                        maxLines = if (expanded) 10 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Expand Icon Indicator
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier.size(28.dp) // Larger expand icon
                )
            }

            // Quick Status & Target Indicators (Non-Expanded View)
            if (!expanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status Text with highly recognizable Emojis
                        val statusLabel = when (record.status) {
                            "Done" -> "✅ Finished"
                            "In Progress" -> "⚡ Working"
                            "Blocked" -> "🛑 Stuck"
                            "Not Started" -> "💤 Queue"
                            "Skipped" -> "⏭️ Skipped"
                            else -> record.status
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(11.dp) // Larger indicator
                                    .clip(CircleShape)
                                    .background(getColorForStatus(record.status))
                            )
                            Text(
                                text = statusLabel,
                                fontSize = 15.sp, // Large and highly legible status labels
                                fontWeight = FontWeight.Bold,
                                color = getColorForStatus(record.status)
                            )
                        }

                        // Target Date if exists
                        if (record.targetDate.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Target",
                                    tint = SecondaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = record.targetDate,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOverdue) StatusOverdue else SecondaryText
                                )
                            }
                        }

                        // Expense Indicator if exists
                        if (record.expenseAmount > 0) {
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", record.expenseAmount)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = StatusBlocked
                            )
                        }
                    }

                    // Progress Pill Indicator
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentColor.copy(alpha = 0.1f),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = "$completionPercent% Done",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Expanded Area detailing actions, hidden parameters
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = BorderColor)

                    // Core Details Block
                    if (record.purpose.isNotEmpty()) {
                        DetailBlock(label = "Purpose / Intent", text = record.purpose)
                    }

                    if (record.actionPlan.isNotEmpty() && record.recordType == "Project") {
                        DetailBlock(label = "Planned Execution Steps", text = record.actionPlan)
                    }

                    if (record.resultOutcome.isNotEmpty() && isDone) {
                        DetailBlock(label = "Final Outcome / Result", text = record.resultOutcome)
                    }

                    // Fields mapping for Projects
                    if (record.recordType == "Project") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                DetailBlock(label = "Project Category", text = record.projectCategory)
                            }
                            if (record.routineCategory.isNotEmpty() && record.routineCategory != "Other") {
                                Box(modifier = Modifier.weight(1f)) {
                                    DetailBlock(label = "Routine Schedule", text = record.routineCategory)
                                }
                            }
                        }
                    }

                    // Fields mapping for Finance Review Checks
                    if (record.recordType == "Finance Review") {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SecondarySurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Habit Integrity Checkpoints",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Daily Expense Logging check
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { viewModel.toggleDailyExpenseChecked(record) }
                                            .padding(8.dp)
                                    ) {
                                        Checkbox(
                                            checked = record.dailyExpenseUpdate,
                                            onCheckedChange = { viewModel.toggleDailyExpenseChecked(record) },
                                            colors = CheckboxDefaults.colors(checkedColor = StatusDone)
                                        )
                                        Text("Daily Expense Logged", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    }

                                    // Investor Review check
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { viewModel.toggleInvestmentReviewChecked(record) }
                                            .padding(8.dp)
                                    ) {
                                        Checkbox(
                                            checked = record.investmentReview,
                                            onCheckedChange = { viewModel.toggleInvestmentReviewChecked(record) },
                                            colors = CheckboxDefaults.colors(checkedColor = StatusDone)
                                        )
                                        Text("Investment Reviewed", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                                
                                val reviewCheck = viewModel.getFinanceReviewCheckString(record.dailyExpenseUpdate, record.investmentReview)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = if (reviewCheck == "OK") Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (reviewCheck == "OK") StatusDone else StatusOverdue
                                    )
                                    Text(
                                        text = "Google Sheets Log Integrity: $reviewCheck",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (reviewCheck == "OK") StatusDone else StatusOverdue
                                    )
                                }
                            }
                        }
                    }

                    // META DATA / FORMULA ENGINES BLOCK (ADMIN MODE ONLY)
                    if (adminMode) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentColor.copy(alpha = 0.05f),
                            border = BorderStroke(1.dp, AccentColor.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Engineering, contentDescription = null, tint = AccentColor, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Formula Engine (Sheet Equivalent Debug)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))

                                RawParamRow(label = "record_id", value = record.recordId)
                                RawParamRow(label = "record_type", value = record.recordType)
                                RawParamRow(label = "time_horizon", value = record.timeHorizon)
                                if (record.dayOfWeek.isNotEmpty()) RawParamRow(label = "day_of_week", value = record.dayOfWeek)
                                RawParamRow(label = "created_date (T)", value = record.createdDate)
                                if (isDone) RawParamRow(label = "completed_date (T)", value = record.completedDate)
                                
                                RawParamRow(
                                    label = "aging_days (\$H\$)",
                                    value = agingDays?.toString() ?: "N/A (Done or Empty)"
                                )
                                RawParamRow(
                                    label = "days_to_target (\$H\$)",
                                    value = daysToDeadline?.toString() ?: "N/A"
                                )
                                RawParamRow(
                                    label = "completion_percentage (\$Q\$)",
                                    value = "$completionPercent%"
                                )
                                RawParamRow(
                                    label = "overdue_flag (\$P\$)",
                                    value = if (isOverdue) "Overdue" else "OK"
                                )
                            }
                        }
                    }

                    // Action Footers: Edit, Delete, Toggle Complete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { editDialogVisible = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .background(SecondarySurface, RoundedCornerShape(8.dp))
                                .minimumInteractiveComponentSize()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Item", tint = AccentColor, modifier = Modifier.size(18.dp))
                                Text("Edit Detail", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentColor)
                            }
                        }

                        IconButton(
                            onClick = { onDelete(record) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .background(StatusOverdue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .minimumInteractiveComponentSize()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Item", tint = StatusOverdue, modifier = Modifier.size(18.dp))
                                Text("Delete", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusOverdue)
                            }
                        }
                    }
                }
            }
        }
    }

    if (editDialogVisible) {
        EditRecordDialog(
            record = record,
            onDismiss = { editDialogVisible = false },
            onSave = { updated ->
                onUpdate(updated)
                editDialogVisible = false
            }
        )
    }
}

// Subordinate composables
@Composable
fun DetailBlock(label: String, text: String) {
    Column {
        Text(text = label.uppercase(Locale.US), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SecondaryText, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = text, fontSize = 15.sp, color = PrimaryText, lineHeight = 19.sp)
    }
}

@Composable
fun RawParamRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = SecondaryText)
        Text(text = value, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = PrimaryText)
    }
}

@Composable
fun PriorityBadge(priority: String) {
    val (bgColor, textColor, label) = when (priority) {
        "High" -> Triple(PriorityHigh.copy(alpha = 0.2f), PriorityHigh, "🚨 Urgent")
        "Medium" -> Triple(PriorityMedium.copy(alpha = 0.2f), PriorityMedium, "⚠️ Medium")
        "Low" -> Triple(PriorityLow.copy(alpha = 0.2f), PriorityLow, "ℹ️ Normal")
        else -> Triple(SecondarySurface, PrimaryText, priority)
    }
    Surface(
        shape = RoundedCornerShape(8.dp), // More rounded, modern pill shape
        color = bgColor,
        modifier = Modifier.height(24.dp) // Taller tag
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp, // Larger tag text for senior eyes
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }
    }
}

fun getColorForStatus(status: String): Color {
    return when (status) {
        "Done" -> StatusDone
        "In Progress" -> StatusInProgress
        "Blocked" -> StatusBlocked
        "Not Started" -> StatusNotStarted
        "Skipped" -> SecondaryText
        else -> StatusNotStarted
    }
}

// ==========================================
// 3. DIALOGS (ADD & EDIT DETAILS)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordDialog(
    onDismiss: () -> Unit,
    onSave: (TrackerRecord) -> Unit
) {
    var chosenTypeIndex by remember { mutableStateOf(0) }
    val types = listOf("Task", "Project", "Goal", "Finance")

    var toDoText by remember { mutableStateOf("") }
    var priorityText by remember { mutableStateOf("Medium") }
    var targetDateText by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("Not Started") }
    var purposeText by remember { mutableStateOf("") }

    // Projects fields
    var projectCategory by remember { mutableStateOf("Personal") }
    var activityText by remember { mutableStateOf("") }
    var actionPlanText by remember { mutableStateOf("") }

    // Finance fields
    var expenseAmount by remember { mutableStateOf("") }
    var dailyChecked by remember { mutableStateOf(false) }
    var investmentChecked by remember { mutableStateOf(false) }

    // Goal fields
    var goalType by remember { mutableStateOf("Weekly") } // "Weekly" or "Monthly"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MainSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Item",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )

                // High Contrast Selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    types.forEachIndexed { idx, title ->
                        val isSel = chosenTypeIndex == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) AccentColor else SecondarySurface)
                                .clickable { chosenTypeIndex = idx }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else PrimaryText
                            )
                        }
                    }
                }

                HorizontalDivider(color = BorderColor)

                // DYNAMIC FORMS BASED ON USER FEEDBACK
                when (chosenTypeIndex) {
                    0 -> { // Task (Daily Task)
                        OutlinedTextField(
                            value = toDoText,
                            onValueChange = { toDoText = it },
                            label = { Text("What needs to be done?", fontSize = 15.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("add_item_title"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentColor)
                        )

                        Text("Priority Level", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("High", "Medium", "Low").forEach { p ->
                                val selected = priorityText == p
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selected) AccentColor else SecondarySurface)
                                        .clickable { priorityText = p }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        p,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else PrimaryText
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = targetDateText,
                            onValueChange = { targetDateText = it },
                            label = { Text("Due Date (yyyy-MM-dd) • Optional", fontSize = 15.sp) },
                            placeholder = { Text("e.g. 2026-05-30") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // safe for dates
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentColor)
                        )

                        OutlinedTextField(
                            value = purposeText,
                            onValueChange = { purposeText = it },
                            label = { Text("Purpose / Motivation", fontSize = 15.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentColor)
                        )
                    }
                    1 -> { // Project (Project)
                        OutlinedTextField(
                            value = activityText,
                            onValueChange = { activityText = it },
                            label = { Text("Objective / Project Name", fontSize = 15.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentColor)
                        )

                        Text("Category Classification", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Operations", "Culture", "Team Setup", "Financial", "Personal", "Health", "Learning").forEach { cat ->
                                val selected = projectCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (selected) AccentColor else SecondarySurface)
                                        .clickable { projectCategory = cat }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        cat,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else PrimaryText
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = actionPlanText,
                            onValueChange = { actionPlanText = it },
                            label = { Text("Execution Steps (Plan) • Multi-line", fontSize = 15.sp) },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentColor)
                        )

                        OutlinedTextField(
                            value = purposeText,
                            onValueChange = { purposeText = it },
                            label = { Text("Project Key Purpose", fontSize = 15.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentColor)
                        )
                    }
                    2 -> { // Goal (Weekly or Monthly)
                        Text("Horizon Type", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Weekly", "Monthly").forEach { gt ->
                                val selected = goalType == gt
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selected) AccentColor else SecondarySurface)
                                        .clickable { goalType = gt }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "$gt Goal",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else PrimaryText
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = toDoText,
                            onValueChange = { toDoText = it },
                            label = { Text("Describe the Target Goal", fontSize = 15.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentColor)
                        )

                        OutlinedTextField(
                            value = targetDateText,
                            onValueChange = { targetDateText = it },
                            label = { Text("Target Deadline (yyyy-MM-dd)", fontSize = 15.sp) },
                            placeholder = { Text("e.g. 2026-05-31") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentColor)
                        )
                    }
                    3 -> { // Finance (Finance Review)
                        OutlinedTextField(
                            value = toDoText,
                            onValueChange = { toDoText = it },
                            label = { Text("Finance/Ledger Item Name", fontSize = 15.sp) },
                            placeholder = { Text("e.g. Weekly Server costs Review") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentColor)
                        )

                        OutlinedTextField(
                            value = expenseAmount,
                            onValueChange = { expenseAmount = it },
                            label = { Text("Logged Amount (₹)", fontSize = 15.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentColor)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { dailyChecked = !dailyChecked }
                                .padding(vertical = 6.dp)
                        ) {
                            Checkbox(checked = dailyChecked, onCheckedChange = { dailyChecked = it })
                            Text("Daily Expense Checked ✅", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { investmentChecked = !investmentChecked }
                                .padding(vertical = 6.dp)
                        ) {
                            Checkbox(checked = investmentChecked, onCheckedChange = { investmentChecked = it })
                            Text("Investment Check Logged 📈", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Submittals
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", color = SecondaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val emptyDefault = if (toDoText.isEmpty()) "Quick Item" else toDoText
                            val record = when (chosenTypeIndex) {
                                0 -> TrackerRecord(
                                    recordType = "Daily Task",
                                    timeHorizon = "Daily",
                                    toDoItem = emptyDefault,
                                    priority = priorityText,
                                    targetDate = targetDateText,
                                    status = statusText,
                                    purpose = purposeText
                                )
                                1 -> TrackerRecord(
                                    recordType = "Project",
                                    timeHorizon = "Project",
                                    projectCategory = projectCategory,
                                    routineActivity = if (activityText.isEmpty()) "Sub Routine" else activityText,
                                    toDoItem = if (activityText.isEmpty()) "Sub Routine" else activityText,
                                    actionPlan = actionPlanText,
                                    purpose = purposeText,
                                    status = "Not Started"
                                )
                                2 -> {
                                    val isWeekly = goalType == "Weekly"
                                    TrackerRecord(
                                        recordType = if (isWeekly) "Weekly Goal" else "Monthly Goal",
                                        timeHorizon = if (isWeekly) "Weekly" else "Monthly",
                                        weeklyGoal = if (isWeekly) toDoText else "",
                                        monthlyGoal = if (!isWeekly) toDoText else "",
                                        toDoItem = toDoText,
                                        targetDate = targetDateText,
                                        status = "Not Started"
                                    )
                                }
                                3 -> TrackerRecord(
                                    recordType = "Finance Review",
                                    timeHorizon = "Monthly",
                                    toDoItem = if (toDoText.isEmpty()) "Monthly Expense Check" else toDoText,
                                    expenseAmount = expenseAmount.toDoubleOrNull() ?: 0.0,
                                    dailyExpenseUpdate = dailyChecked,
                                    investmentReview = investmentChecked,
                                    status = if (dailyChecked && investmentChecked) "Done" else "In Progress"
                                )
                                else -> TrackerRecord()
                            }
                            onSave(record)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_record_button")
                    ) {
                        Text("Save Item", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecordDialog(
    record: TrackerRecord,
    onDismiss: () -> Unit,
    onSave: (TrackerRecord) -> Unit
) {
    var toDoText by remember { mutableStateOf(record.toDoItem) }
    var weeklyGoalText by remember { mutableStateOf(record.weeklyGoal) }
    var monthlyGoalText by remember { mutableStateOf(record.monthlyGoal) }
    var routineActivityText by remember { mutableStateOf(record.routineActivity) }
    
    var priorityText by remember { mutableStateOf(record.priority) }
    var targetDateText by remember { mutableStateOf(record.targetDate) }
    var statusText by remember { mutableStateOf(record.status) }
    var purposeText by remember { mutableStateOf(record.purpose) }
    var actionPlanText by remember { mutableStateOf(record.actionPlan) }
    var expenseAmountText by remember { mutableStateOf(record.expenseAmount.toString()) }
    var dailyChecked by remember { mutableStateOf(record.dailyExpenseUpdate) }
    var investmentChecked by remember { mutableStateOf(record.investmentReview) }
    var resultOutcomeText by remember { mutableStateOf(record.resultOutcome) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MainSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Detail • ${record.recordType}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentColor
                )

                HorizontalDivider(color = BorderColor)

                // Editable Name based on Record Type
                when (record.recordType) {
                    "Daily Task" -> {
                        OutlinedTextField(
                            value = toDoText,
                            onValueChange = { toDoText = it },
                            label = { Text("Task Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "Weekly Goal" -> {
                        OutlinedTextField(
                            value = weeklyGoalText,
                            onValueChange = { weeklyGoalText = it },
                            label = { Text("Weekly Goal Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "Monthly Goal" -> {
                        OutlinedTextField(
                            value = monthlyGoalText,
                            onValueChange = { monthlyGoalText = it },
                            label = { Text("Monthly Goal Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "Project" -> {
                        OutlinedTextField(
                            value = routineActivityText,
                            onValueChange = { routineActivityText = it },
                            label = { Text("Project Routine Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = actionPlanText,
                            onValueChange = { actionPlanText = it },
                            label = { Text("Action Steps") },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            maxLines = 3
                        )
                    }
                    "Finance Review" -> {
                        OutlinedTextField(
                            value = toDoText,
                            onValueChange = { toDoText = it },
                            label = { Text("Review Identifier") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = expenseAmountText,
                            onValueChange = { expenseAmountText = it },
                            label = { Text("Expense Amount ($)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                // Common fields: Priority, Status, Target Date, Outcome
                Text("Item Status", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Not Started", "In Progress", "Blocked", "Done", "Skipped").forEach { s ->
                        val selected = statusText == s
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) AccentColor else SecondarySurface)
                                .clickable { statusText = s }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                s,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else PrimaryText
                            )
                        }
                    }
                }

                Text("Priority Level", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("High", "Medium", "Low").forEach { p ->
                        val selected = priorityText == p
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) AccentColor else SecondarySurface)
                                .clickable { priorityText = p }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                p,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else PrimaryText
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = targetDateText,
                    onValueChange = { targetDateText = it },
                    label = { Text("Target Due Date (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = purposeText,
                    onValueChange = { purposeText = it },
                    label = { Text("Key Purpose / Backstory Goal") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (statusText == "Done") {
                    OutlinedTextField(
                        value = resultOutcomeText,
                        onValueChange = { resultOutcomeText = it },
                        label = { Text("Result / Final Reflection Outcome") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Submittals
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", color = SecondaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val expAmt = expenseAmountText.toDoubleOrNull() ?: record.expenseAmount
                            val updated = record.copy(
                                toDoItem = when (record.recordType) {
                                    "Daily Task" -> toDoText
                                    "Weekly Goal" -> weeklyGoalText
                                    "Monthly Goal" -> monthlyGoalText
                                    "Project" -> routineActivityText
                                    "Finance Review" -> toDoText
                                    else -> toDoText
                                },
                                weeklyGoal = weeklyGoalText,
                                monthlyGoal = monthlyGoalText,
                                routineActivity = routineActivityText,
                                priority = priorityText,
                                targetDate = targetDateText,
                                status = statusText,
                                purpose = purposeText,
                                actionPlan = actionPlanText,
                                expenseAmount = expAmt,
                                resultOutcome = resultOutcomeText
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Save Changes", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. GOOGLE SHEETS & QUICK START GUIDE TAB VIEW
// ==========================================
@Composable
fun GuideTabView() {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "GOOGLE SHEETS & GUIDE",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SecondaryText,
            letterSpacing = 1.sp
        )

        // Section: Live Quick Start Guide for Uncle
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MainSurface,
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Welcome — Daily Tracker Quick Start Guide",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PrimaryText
                )
                HorizontalDivider(color = BorderColor)
                
                QuickGuideItem(
                    stepNumber = "1",
                    title = "Add Something New",
                    description = "Click the '+' floating action button in the bottom-right corner. Choose either Task, Expense, Project, or Goal. Enter basic clear text and tap Save."
                )
                
                QuickGuideItem(
                    stepNumber = "2",
                    title = "Update Your Day",
                    description = "When a scheduled activity or checklist is finished, change its status to 'Done' or simply tap the visual checkbox ✅ right from your dashboard. It takes under 3 seconds!"
                )
                
                QuickGuideItem(
                    stepNumber = "3",
                    title = "Scan Your Operational Integrity",
                    description = "Review 'Open Tasks' to see pending workloads. Watch the 'Overdue' count inside red markers to secure zero-backlog days. Tick off 'Finance log updates' each week."
                )
            }
        }

        // Section: Excel Copy-Paste Formulas Setup Guide
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MainSurface,
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Google Sheets / Excel Sync Formula Engine",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AccentColor
                )
                Text(
                    text = "If you want to sync this system into a Google Sheet spreadsheet, copy-paste these exact automated formulas into corresponding cells. Tap any formula card to copy it directly to your clipboard.",
                    fontSize = 14.sp,
                    color = SecondaryText,
                    lineHeight = 18.sp
                )

                HorizontalDivider(color = BorderColor)

                // Formula Card 1: Overdue Detection
                FormulaCopyCard(
                    formulaName = "Overdue Detection",
                    formulaText = "=IF(AND(\$Q2<>\"Done\",\$P2<>\"\",TODAY()>\$P2),\"Overdue\",\"\")",
                    description = "Analyzes target date (\$P) and status (\$Q) to auto-flag delayed items.",
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(it))
                        Toast.makeText(context, "Copied Overdue Formula", Toast.LENGTH_SHORT).show()
                    }
                )

                // Formula Card 2: Aging Calculation
                FormulaCopyCard(
                    formulaName = "Aging Calculation",
                    formulaText = "=IF(OR(\$R2=\"\", \$Q2=\"Done\"), \"\", DAYS(TODAY(), \$R2))",
                    description = "Calculates days accumulated since creation date (\$R) for unresolved items.",
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(it))
                        Toast.makeText(context, "Copied Aging Formula", Toast.LENGTH_SHORT).show()
                    }
                )

                // Formula Card 3: Days Until Deadline
                FormulaCopyCard(
                    formulaName = "Days Until Deadline",
                    formulaText = "=IF(\$P2=\"\", \"\", DAYS(\$P2, TODAY()))",
                    description = "Dynamically counts remaining hours/days left for deadlines.",
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(it))
                        Toast.makeText(context, "Copied Deadline Formula", Toast.LENGTH_SHORT).show()
                    }
                )

                // Formula Card 4: Completion percentage
                FormulaCopyCard(
                    formulaName = "Completion Percentage",
                    formulaText = "=IFS(\$Q2=\"Done\",100,\$Q2=\"In Progress\",50,\$Q2=\"Blocked\",25,\$Q2=\"Not Started\",0,TRUE,0)",
                    description = "Converts statuses into clean numeric progress indices for progress tracking.",
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(it))
                        Toast.makeText(context, "Copied Completion Formula", Toast.LENGTH_SHORT).show()
                    }
                )

                // Formula Card 5: Finance logging review
                FormulaCopyCard(
                    formulaName = "Finance Review Integrity Check",
                    formulaText = "=IF(AND(\$X2=TRUE,\$Y2=TRUE),\"OK\",\"Review due\")",
                    description = "Tracks whether daily expense (\$X) and weekly investments (\$Y) have both been logged.",
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(it))
                        Toast.makeText(context, "Copied Finance integrity Formula", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun QuickGuideItem(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(SecondarySurface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = AccentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
            Text(text = description, fontSize = 14.sp, color = SecondaryText, lineHeight = 18.sp)
        }
    }
}

@Composable
fun FormulaCopyCard(
    formulaName: String,
    formulaText: String,
    description: String,
    onCopy: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCopy(formulaText) },
        shape = RoundedCornerShape(8.dp),
        color = SecondarySurface,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formulaName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentColor
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = SecondaryText, modifier = Modifier.size(14.dp))
                    Text("TAP TO COPY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SecondaryText)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                color = MainSurface,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Text(
                    text = formulaText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = PrimaryText,
                    modifier = Modifier.padding(10.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = description, fontSize = 13.sp, color = SecondaryText)
        }
    }
}

// Simplified Bottom Navigation Bar matching modern executive aesthetics
@Composable
fun CustomBottomNavBar(
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 12.dp,
        tonalElevation = 6.dp,
        color = MainSurface
    ) {
        Column {
            HorizontalDivider(color = BorderColor, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BottomNavItem(
                    label = "Today",
                    icon = Icons.Outlined.WbSunny,
                    selectedIcon = Icons.Default.WbSunny,
                    isSelected = activeTab == 0,
                    onClick = { onTabSelected(0) },
                    modifier = Modifier.weight(1f)
                )

                BottomNavItem(
                    label = "Weekly",
                    icon = Icons.Outlined.DateRange,
                    selectedIcon = Icons.Default.DateRange,
                    isSelected = activeTab == 1,
                    onClick = { onTabSelected(1) },
                    modifier = Modifier.weight(1f)
                )

                BottomNavItem(
                    label = "Monthly",
                    icon = Icons.Outlined.Event,
                    selectedIcon = Icons.Default.Event,
                    isSelected = activeTab == 2,
                    onClick = { onTabSelected(2) },
                    modifier = Modifier.weight(1f)
                )

                BottomNavItem(
                    label = "RPM",
                    icon = Icons.Outlined.Layers,
                    selectedIcon = Icons.Default.Layers,
                    isSelected = activeTab == 3,
                    onClick = { onTabSelected(3) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    label: String,
    icon: ImageVector,
    selectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) AccentColor.copy(alpha = 0.1f) else Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) selectedIcon else icon,
                contentDescription = label,
                tint = if (isSelected) AccentColor else SecondaryText,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) AccentColor else SecondaryText
        )
    }
}

@Composable
fun KPICard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MainSurface,
        border = BorderStroke(1.dp, BorderColor),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(Locale.US),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SecondaryText,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = SecondaryText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun DottedLine(color: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier.height(1.dp)) {
        val pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
            pathEffect = pathEffect,
            strokeWidth = 2f
        )
    }
}

