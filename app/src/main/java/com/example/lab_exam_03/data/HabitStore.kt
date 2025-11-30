package com.example.lab_exam_03.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

data class Habit(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val createdAt: Long
)

class HabitStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("habit_store", Context.MODE_PRIVATE)

    fun getAllHabits(): List<Habit> {
        val habits = mutableListOf<Habit>()
        val habitIds = prefs.getString(KEY_HABIT_IDS, "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
        
        for (id in habitIds) {
            val name = prefs.getString("habit_name_$id", "") ?: ""
            val category = prefs.getString("habit_category_$id", "") ?: ""
            val description = prefs.getString("habit_description_$id", "") ?: ""
            val createdAt = prefs.getLong("habit_created_$id", 0L)
            
            if (name.isNotEmpty()) {
                habits.add(Habit(id, name, category, description, createdAt))
            }
        }
        
        return habits
    }

    fun addHabit(habit: Habit) {
        val currentIds = prefs.getString(KEY_HABIT_IDS, "")?.split(",")?.filter { it.isNotEmpty() }?.toMutableList() ?: mutableListOf()
        currentIds.add(habit.id)
        
        prefs.edit()
            .putString(KEY_HABIT_IDS, currentIds.joinToString(","))
            .putString("habit_name_${habit.id}", habit.name)
            .putString("habit_category_${habit.id}", habit.category)
            .putString("habit_description_${habit.id}", habit.description)
            .putLong("habit_created_${habit.id}", habit.createdAt)
            .apply()
    }

    fun deleteHabit(habitId: String) {
        val currentIds = prefs.getString(KEY_HABIT_IDS, "")?.split(",")?.filter { it.isNotEmpty() && it != habitId } ?: emptyList()
        
        prefs.edit()
            .putString(KEY_HABIT_IDS, currentIds.joinToString(","))
            .remove("habit_name_$habitId")
            .remove("habit_category_$habitId")
            .remove("habit_description_$habitId")
            .remove("habit_created_$habitId")
            .apply()
        
        // Also remove from completed today
        val completedToday = getCompletedToday().filter { it != habitId }
        saveCompletedToday(completedToday)
    }

    fun getCompletedToday(): List<String> {
        val today = getCurrentDateKey()
        val completedString = prefs.getString("${KEY_COMPLETED}_$today", "") ?: ""
        return if (completedString.isEmpty()) emptyList() else completedString.split(",").filter { it.isNotEmpty() }
    }

    fun toggleHabitComplete(habitId: String) {
        val completed = getCompletedToday().toMutableList()
        if (completed.contains(habitId)) {
            completed.remove(habitId)
        } else {
            completed.add(habitId)
        }
        saveCompletedToday(completed)
    }

    private fun saveCompletedToday(completed: List<String>) {
        val today = getCurrentDateKey()
        prefs.edit().putString("${KEY_COMPLETED}_$today", completed.joinToString(",")).apply()
    }

    fun getStreakCount(): Int {
        // Simple streak calculation - count consecutive days with at least one completed habit
        var streak = 0
        val calendar = Calendar.getInstance()
        
        for (i in 0 until 30) { // Check last 30 days
            val dateKey = getDateKey(calendar.time)
            val completedThatDay = prefs.getString("${KEY_COMPLETED}_$dateKey", "") ?: ""
            val completed = if (completedThatDay.isEmpty()) emptyList() else completedThatDay.split(",").filter { it.isNotEmpty() }
            
            if (completed.isNotEmpty()) {
                streak++
            } else if (i > 0) { // Don't break on today if nothing completed yet
                break
            }
            
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }
        
        return streak
    }

    private fun getCurrentDateKey(): String {
        return getDateKey(Date())
    }

    private fun getDateKey(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
    }

    companion object {
        private const val KEY_HABIT_IDS = "habit_ids"
        private const val KEY_COMPLETED = "completed"
    }
}