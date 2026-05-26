package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tracker_records")
data class TrackerRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recordId: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val recordType: String = "Daily Task", // "Daily Task" | "Weekly Goal" | "Monthly Goal" | "Project" | "Finance Review"
    val timeHorizon: String = "Daily", // "Daily" | "Weekly" | "Monthly" | "Project"
    val dayOfWeek: String = "", // Monday–Sunday
    val projectYear: String = "2026",
    val projectMonth: String = "May",
    val projectCategory: String = "Other", // Operations, Culture, Team Setup, Team Development, Financial, Personal, Health, Learning, Admin, Other
    val routineCategory: String = "Other", // Morning, Travel, Work, Family, Finance, Health, Review, Sleep, Other
    val routineActivity: String = "",
    val timeBlockStart: String = "",
    val timeBlockEnd: String = "",
    val weeklyGoal: String = "",
    val monthlyGoal: String = "",
    val toDoItem: String = "",
    val priority: String = "Medium", // High, Medium, Low
    val targetDate: String = "", // "yyyy-MM-dd"
    val status: String = "Not Started", // Not Started, In Progress, Blocked, Done, Skipped
    val createdDate: String = "", // "yyyy-MM-dd"
    val completedDate: String = "", // "yyyy-MM-dd" or ""
    val resultOutcome: String = "",
    val actionPlan: String = "",
    val purpose: String = "",
    val expenseAmount: Double = 0.0,
    val dailyExpenseUpdate: Boolean = false,
    val investmentReview: Boolean = false
)
