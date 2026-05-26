package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AuthManager
import com.example.data.TrackerRecord
import com.example.data.TrackerRecordRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TrackerViewModel(
    application: Application,
    private val repository: TrackerRecordRepository
) : AndroidViewModel(application) {

    // Simple date operations in yyyy-MM-dd format
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Current date formatted string
    fun getTodayString(): String = dateFormat.format(Date())

    // Tracks current logged in user ID to filter records reactively
    private val _userIdState = MutableStateFlow("anonymous_or_local")
    val userIdState: StateFlow<String> = _userIdState.asStateFlow()

    // Base records observable filtered by authenticated user ID
    @OptIn(ExperimentalCoroutinesApi::class)
    val records: StateFlow<List<TrackerRecord>> = _userIdState
        .flatMapLatest { userId ->
            repository.getRecordsForUser(userId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Collect AuthState which dynamically updates user ID trigger and seeds if empty
        viewModelScope.launch {
            AuthManager.currentUserState.collect { user ->
                val id = user?.uid ?: "anonymous_or_local"
                _userIdState.value = id
                val list = repository.getRecordsForUser(id).first()
                if (list.isEmpty()) {
                    seedTemplateRecordsForUser(id)
                }
            }
        }
    }

    private suspend fun seedTemplateRecords() {
        seedTemplateRecordsForUser("anonymous_or_local")
    }

    private suspend fun seedTemplateRecordsForUser(userId: String) {
        val today = getTodayString()
        
        val cal = Calendar.getInstance()
        
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = dateFormat.format(cal.time)
        
        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, -2)
        val twoDaysAgo = dateFormat.format(cal.time)

        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, -3)
        val threeDaysAgo = dateFormat.format(cal.time)

        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, -5)
        val fiveDaysAgoStr = dateFormat.format(cal.time)
 
        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, 2)
        val twoDaysLaterStr = dateFormat.format(cal.time)

        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, 7)
        val nextWeekStr = dateFormat.format(cal.time)

        // --- TODAY ACTIVE ROUTINES ---
        // 1. Morning Alignment (Active)
        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Daily Task",
                timeHorizon = "Daily",
                dayOfWeek = getCurrentDayOfWeek(),
                toDoItem = "Perform 5AM SAVERS Morning Victory Hour: contemplation, alignment cards, and posture work",
                priority = "High",
                status = "In Progress",
                routineCategory = "Morning",
                createdDate = today,
                targetDate = today,
                purpose = "Anchor conscious orientation and establish peak daily decision-making clarity"
            )
        )

        // 2. High-Value Deep Work Block (Done)
        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Daily Task",
                timeHorizon = "Daily",
                dayOfWeek = getCurrentDayOfWeek(),
                toDoItem = "90/90/1 Deep Focus: Refactor system sync engine with fallback resilience and offline schemas",
                priority = "High",
                status = "Done",
                routineCategory = "Work",
                createdDate = today,
                targetDate = today,
                completedDate = today,
                purpose = "Achieve bulletproof data parity during intermittent connectivity blocks",
                resultOutcome = "Parity architecture established; handled Sqlite constraints beautifully."
            )
        )

        // 3. Movement Practice (In Progress)
        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Daily Task",
                timeHorizon = "Daily",
                dayOfWeek = getCurrentDayOfWeek(),
                toDoItem = "Movement conditioning workout: focus on badminton reflex sprints, flexibility, and hydration",
                priority = "Medium",
                status = "In Progress",
                routineCategory = "Health",
                createdDate = today,
                targetDate = today,
                purpose = "Maintain cardiovascular base and optimize overall neurological response velocity"
            )
        )

        // 4. Nightly Shutdown Ritual (Not Started)
        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Daily Task",
                timeHorizon = "Daily",
                dayOfWeek = getCurrentDayOfWeek(),
                toDoItem = "Nightly Shutdown: Sync accounts, complete reflective review, and read sub-conscious goal cards",
                priority = "Medium",
                status = "Not Started",
                routineCategory = "Sleep",
                createdDate = today,
                targetDate = today,
                purpose = "Disengage completely from active logic loops to protect restorative deep sleep cycles"
            )
        )

        // --- HISTORICAL COMPLETED ITEMS (For active stats representation / Last 7 Days count) ---
        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Daily Task",
                timeHorizon = "Daily",
                dayOfWeek = "Yesterday",
                toDoItem = "Conducted weekly family budget check-in and reconciled manual accounting logs",
                priority = "Medium",
                status = "Done",
                routineCategory = "Finance",
                createdDate = yesterday,
                targetDate = yesterday,
                completedDate = yesterday,
                purpose = "Confirm asset allocation parameters and confirm surplus cash-flow indicators"
            )
        )

        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Daily Task",
                timeHorizon = "Daily",
                toDoItem = "Executed 45-minute architectural study on localized database design paradigms",
                priority = "Low",
                status = "Done",
                routineCategory = "Review",
                createdDate = twoDaysAgo,
                targetDate = twoDaysAgo,
                completedDate = twoDaysAgo,
                purpose = "Drive continuous professional systems-engineering enhancement"
            )
        )

        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Daily Task",
                timeHorizon = "Daily",
                toDoItem = "Clean and optimize local office workspace layout to reduce visual noise",
                priority = "Low",
                status = "Done",
                routineCategory = "Other",
                createdDate = threeDaysAgo,
                targetDate = threeDaysAgo,
                completedDate = threeDaysAgo,
                purpose = "Decompress visual workspace to support deep focus states"
            )
        )


        // --- WEEKLY GOAL SLOTS (Slots 1 to 5) ---
        // Slot 1: Active
        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Weekly Goal",
                timeHorizon = "Weekly",
                toDoItem = "Establish 100% stable database offline fallback and local serialization tests",
                priority = "High",
                status = "In Progress",
                createdDate = today,
                targetDate = nextWeekStr,
                purpose = "Ensure complete stability for non-technical deployment readiness"
            )
        )

        // Slot 2: Active
        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Weekly Goal",
                timeHorizon = "Weekly",
                toDoItem = "Perform mid-month Groww & Zerodha investment portfolio calibration and security audit",
                priority = "Medium",
                status = "In Progress",
                createdDate = today,
                targetDate = nextWeekStr,
                purpose = "Align asset allocations of high-yield indices against quarterly compound projections"
            )
        )

        // Slot 3: Done (Realistically completed this week)
        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Weekly Goal",
                timeHorizon = "Weekly",
                toDoItem = "Configure zero-latency AXIS branding vector paths and adaptive launcher rendering matrices",
                priority = "Medium",
                status = "Done",
                createdDate = fiveDaysAgoStr,
                targetDate = today,
                completedDate = today,
                purpose = "Establish premium minimalist visual alignment across all device viewport ratios",
                resultOutcome = "Launcher foreground resources verified on multi-device emulators without distortion."
            )
        )

        // Slot 4: Not Started
        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Weekly Goal",
                timeHorizon = "Weekly",
                toDoItem = "Draft Version 1 deployment manifesto outlining system boundaries and offline parity limits",
                priority = "Low",
                status = "Not Started",
                createdDate = today,
                targetDate = nextWeekStr,
                purpose = "Provide operational transparency and lower cognitive barriers for first-time operators"
            )
        )


        // --- MONTHLY STRATEGIC GOALS (Slots 1 to 5) ---
        // Slot 1: Done
        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Monthly Goal",
                timeHorizon = "Monthly",
                toDoItem = "Transition database persistence model to fully structured SQLite Room framework",
                priority = "High",
                status = "Done",
                createdDate = fiveDaysAgoStr,
                targetDate = today,
                completedDate = today,
                purpose = "Secure core transaction safety and prevent state corruption permanently",
                resultOutcome = "Database migration to compiled schema completed and tested with zero data-loss exceptions."
            )
        )

        // Slot 2: Active
        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Monthly Goal",
                timeHorizon = "Monthly",
                toDoItem = "Execute 12 high-intensity cardiovascular badminton routines and recovery sprints",
                priority = "Medium",
                status = "In Progress",
                createdDate = today,
                targetDate = today,
                purpose = "Strengthen respiratory efficiency and expand peak metabolic threshold windows"
            )
        )

        // Slot 3: Active
        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Monthly Goal",
                timeHorizon = "Monthly",
                toDoItem = "Verify fully integrated, end-to-end encrypted identity sync on Android with Google/Apple backbones",
                priority = "High",
                status = "In Progress",
                createdDate = today,
                targetDate = today,
                purpose = "Preserve raw user privacy while enabling secure multi-tenant synchronizations"
            )
        )


        // --- RAPID PLANNING METHOD (RPM) INITIATIVES (For Project Tab) ---
        // 1. Tech Infrastructure (Operations)
        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Project",
                timeHorizon = "Project",
                projectCategory = "Operations",
                routineActivity = "System Nexus Sync Engine",
                toDoItem = "Develop System Nexus Sync Engine: offline preservation architecture with auto-commit",
                priority = "High",
                status = "In Progress",
                createdDate = fiveDaysAgoStr,
                targetDate = twoDaysLaterStr,
                purpose = "Achieve continuous system uptime without a persistent active network requirement",
                actionPlan = "1. Author local StateFlow transaction checkpoints\n2. Program automatic background conflict detection queues\n3. Implement localized offline SQLite ledger buffers"
            )
        )

        // 2. Personal Health & Wellness (Personal)
        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Project",
                timeHorizon = "Project",
                projectCategory = "Personal",
                routineActivity = "Peak Physical Alignment",
                toDoItem = "Optimize Peak Physical Alignment: morning water therapy, movement, and badminton conditioning",
                priority = "Medium",
                status = "In Progress",
                createdDate = fiveDaysAgoStr,
                targetDate = nextWeekStr,
                purpose = "Generate maximum neurological capacity to support sustained cognitive execution during deep blocks",
                actionPlan = "1. Maintain 500ml pure water consumption upon immediate 5AM rise\n2. Execute structured anaerobic acceleration sets on-court twice weekly\n3. Restrict solid nutrition windows after 7:30 PM to optimize digestive recovery"
            )
        )

        // 3. Wealth Management Ledger System (Financial)
        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Project",
                timeHorizon = "Project",
                projectCategory = "Financial",
                routineActivity = "Core Assets Reconciler",
                toDoItem = "Implement Core Assets Reconciler: automate manual spreadsheet imports and growth projections",
                priority = "High",
                status = "Blocked",
                createdDate = fiveDaysAgoStr,
                targetDate = nextWeekStr,
                purpose = "Streamline tracking of asset holdings and ensure absolute precision in balance sheets",
                actionPlan = "1. Refine offline investment check indicators\n2. Setup single-click Groww statement parsers\n3. Program local JSON data payload export schemas"
            )
        )


        // --- FINANCE AUDIT RECORD ---
        repository.insertRecord(
            TrackerRecord(
                userId = userId,
                recordType = "Finance Review",
                timeHorizon = "Weekly",
                toDoItem = "Execute Weekly Strategic Treasury Audit and Ledger Verification",
                priority = "Medium",
                status = "In Progress",
                createdDate = today,
                targetDate = today,
                expenseAmount = 1450.00,
                dailyExpenseUpdate = true,
                investmentReview = false,
                purpose = "Check actual expenditures against weekly allocation constraints to prevent account drift"
            )
        )
    }
 
    // Insert record
    fun addRecord(record: TrackerRecord) {
        viewModelScope.launch {
            val userId = _userIdState.value
            val withDates = record.copy(
                userId = userId,
                createdDate = if (record.createdDate.isEmpty()) getTodayString() else record.createdDate,
                completedDate = if (record.status == "Done" && record.completedDate.isEmpty()) getTodayString() else record.completedDate
            )
            repository.insertRecord(withDates)
        }
    }

    // Update Record
    fun updateRecord(record: TrackerRecord) {
        viewModelScope.launch {
            var updated = record
            if (record.status == "Done") {
                if (record.completedDate.isEmpty()) {
                    updated = record.copy(completedDate = getTodayString())
                }
            } else {
                updated = record.copy(completedDate = "")
            }
            repository.updateRecord(updated)
        }
    }

    // Quick Update: Toggle Checkbox logic or advance status
    fun toggleRecordCompleted(record: TrackerRecord) {
        viewModelScope.launch {
            val newStatus = if (record.status == "Done") "Not Started" else "Done"
            val completedDate = if (newStatus == "Done") getTodayString() else ""
            repository.updateRecord(record.copy(status = newStatus, completedDate = completedDate))
        }
    }

    fun toggleDailyExpenseChecked(record: TrackerRecord) {
        viewModelScope.launch {
            val nextState = !record.dailyExpenseUpdate
            // Auto status logic: if finance review complete -> Done
            val nextStatus = if (nextState && record.investmentReview) "Done" else "In Progress"
            val completedDate = if (nextStatus == "Done") getTodayString() else ""
            repository.updateRecord(record.copy(
                dailyExpenseUpdate = nextState,
                status = nextStatus,
                completedDate = completedDate
            ))
        }
    }

    fun toggleInvestmentReviewChecked(record: TrackerRecord) {
        viewModelScope.launch {
            val nextState = !record.investmentReview
            // Auto status logic: if finance review complete -> Done
            val nextStatus = if (record.dailyExpenseUpdate && nextState) "Done" else "In Progress"
            val completedDate = if (nextStatus == "Done") getTodayString() else ""
            repository.updateRecord(record.copy(
                investmentReview = nextState,
                status = nextStatus,
                completedDate = completedDate
            ))
        }
    }

    fun deleteRecord(record: TrackerRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            // Drop-in reset helper
            repository.deleteAllRecords()
            seedTemplateRecords()
        }
    }

    // Spreadsheet formulas translated perfectly into JVM Logic
    fun calculateAgingDays(createdDateStr: String, status: String): Int? {
        if (createdDateStr.isEmpty() || status == "Done") return null
        return try {
            val created = dateFormat.parse(createdDateStr) ?: return null
            val today = dateFormat.parse(getTodayString()) ?: return null
            val diffMs = today.time - created.time
            // Limit minimum to 0
            maxOf(0, (diffMs / (1000 * 60 * 60 * 24)).toInt())
        } catch (e: Exception) {
            null
        }
    }

    fun calculateDaysUntilDeadline(targetDateStr: String): Int? {
        if (targetDateStr.isEmpty()) return null
        return try {
            val target = dateFormat.parse(targetDateStr) ?: return null
            val today = dateFormat.parse(getTodayString()) ?: return null
            val diffMs = target.time - today.time
            (diffMs / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            null
        }
    }

    fun detectIsOverdue(targetDateStr: String, status: String): Boolean {
        if (status == "Done" || targetDateStr.isEmpty()) return false
        return try {
            val target = dateFormat.parse(targetDateStr) ?: return false
            val today = dateFormat.parse(getTodayString()) ?: return false
            today.after(target)
        } catch (e: Exception) {
            false
        }
    }

    fun getCompletionPercentage(status: String): Int {
        return when (status) {
            "Done" -> 100
            "In Progress" -> 50
            "Blocked" -> 25
            "Not Started" -> 0
            else -> 0
        }
    }

    fun getFinanceReviewCheckString(dailyExpense: Boolean, investment: Boolean): String {
        return if (dailyExpense && investment) "OK" else "Review due"
    }

    // Derived StateFlows for live KPI calculation
    val openTasksCount: StateFlow<Int> = records
        .combinedCount { record ->
            record.status != "Done" && record.status != "Skipped"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val overdueTasksCount: StateFlow<Int> = records
        .combinedCount { record ->
            detectIsOverdue(record.targetDate, record.status)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedLast7DaysCount: StateFlow<Int> = records
        .combinedCount { record ->
            record.status == "Done" && isWithinLast7Days(record.completedDate)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val financeChecksThisMonthCount: StateFlow<Int> = records
        .combinedCount { record ->
            record.recordType == "Finance Review" &&
            record.dailyExpenseUpdate &&
            record.investmentReview &&
            isWithinCurrentMonth(record.createdDate)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val monthlyExpenseTotal: StateFlow<Double> = records
        .combineSum { record ->
            if (isWithinCurrentMonth(record.createdDate)) record.expenseAmount else 0.0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayExpenseTotal: StateFlow<Double> = records
        .combineSum { record ->
            if (record.createdDate == getTodayString()) record.expenseAmount else 0.0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Fluent generic flow helpers
    private fun Flow<List<TrackerRecord>>.combinedCount(predicate: (TrackerRecord) -> Boolean): Flow<Int> {
        return map { list -> list.count(predicate) }
    }

    private fun Flow<List<TrackerRecord>>.combineSum(mapper: (TrackerRecord) -> Double): Flow<Double> {
        return map { list -> list.sumOf(mapper) }
    }

    // Date range helpers
    private fun isWithinLast7Days(dateStr: String): Boolean {
        if (dateStr.isEmpty()) return false
        return try {
            val date = dateFormat.parse(dateStr) ?: return false
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -6)
            // Trim hours
            val limitDate = cal.time
            !date.before(limitDate)
        } catch (e: Exception) {
            false
        }
    }

    private fun isWithinCurrentMonth(dateStr: String): Boolean {
        if (dateStr.isEmpty()) return false
        return try {
            val date = dateFormat.parse(dateStr) ?: return false
            val recordCal = Calendar.getInstance().apply { time = date }
            val nowCal = Calendar.getInstance()
            recordCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            recordCal.get(Calendar.MONTH) == nowCal.get(Calendar.MONTH)
        } catch (e: Exception) {
            false
        }
    }

    private fun getCurrentDayOfWeek(): String {
        return SimpleDateFormat("EEEE", Locale.US).format(Date())
    }

    // Onboarding completed preference
    private val _onboardingCompleted = MutableStateFlow(
        getApplication<Application>()
            .getSharedPreferences("personal_os_prefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("onboarding_completed", false)
    )
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    fun completeOnboarding() {
        getApplication<Application>()
            .getSharedPreferences("personal_os_prefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_completed", true)
            .apply()
        _onboardingCompleted.value = true
    }

    fun resetOnboarding() {
        getApplication<Application>()
            .getSharedPreferences("personal_os_prefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_completed", false)
            .apply()
        _onboardingCompleted.value = false
    }
}

// Simple Factory for creating our ViewModel with proper injection
class TrackerViewModelFactory(
    private val application: Application,
    private val repository: TrackerRecordRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrackerViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
