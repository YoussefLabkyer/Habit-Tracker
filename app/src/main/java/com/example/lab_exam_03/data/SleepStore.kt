package com.example.lab_exam_03.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

data class SleepEntry(
    val id: String,
    val hours: Float,
    val goal: Float,
    val percentage: Int,
    val date: String,
    val time: String,
    val timestamp: Long
)

class SleepStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sleep_store", Context.MODE_PRIVATE)

    fun getAllSleepEntries(): List<SleepEntry> {
        val entries = mutableListOf<SleepEntry>()
        val entryIds = prefs.getString(KEY_ENTRY_IDS, "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
        
        for (id in entryIds) {
            val hours = prefs.getFloat("sleep_hours_$id", 0f)
            val goal = prefs.getFloat("sleep_goal_$id", 8f)
            val percentage = prefs.getInt("sleep_percentage_$id", 0)
            val date = prefs.getString("sleep_date_$id", "") ?: ""
            val time = prefs.getString("sleep_time_$id", "") ?: ""
            val timestamp = prefs.getLong("sleep_timestamp_$id", 0L)
            
            if (hours > 0) {
                entries.add(SleepEntry(id, hours, goal, percentage, date, time, timestamp))
            }
        }
        
        return entries.sortedByDescending { it.timestamp }
    }

    fun addSleepEntry(entry: SleepEntry) {
        val currentIds = prefs.getString(KEY_ENTRY_IDS, "")?.split(",")?.filter { it.isNotEmpty() }?.toMutableList() ?: mutableListOf()
        currentIds.add(0, entry.id)
        
        prefs.edit()
            .putString(KEY_ENTRY_IDS, currentIds.joinToString(","))
            .putFloat("sleep_hours_${entry.id}", entry.hours)
            .putFloat("sleep_goal_${entry.id}", entry.goal)
            .putInt("sleep_percentage_${entry.id}", entry.percentage)
            .putString("sleep_date_${entry.id}", entry.date)
            .putString("sleep_time_${entry.id}", entry.time)
            .putLong("sleep_timestamp_${entry.id}", entry.timestamp)
            .apply()
    }

    fun deleteSleepEntry(entryId: String) {
        val currentIds = prefs.getString(KEY_ENTRY_IDS, "")?.split(",")?.filter { it.isNotEmpty() && it != entryId } ?: emptyList()
        
        prefs.edit()
            .putString(KEY_ENTRY_IDS, currentIds.joinToString(","))
            .remove("sleep_hours_$entryId")
            .remove("sleep_goal_$entryId")
            .remove("sleep_percentage_$entryId")
            .remove("sleep_date_$entryId")
            .remove("sleep_time_$entryId")
            .remove("sleep_timestamp_$entryId")
            .apply()
    }

    fun getTodaySleep(): Float {
        val today = getCurrentDateString()
        val entries = getAllSleepEntries()
        return entries.find { it.date == today }?.hours ?: 0f
    }

    fun getAverageSleep(): Float {
        val entries = getAllSleepEntries()
        return if (entries.isNotEmpty()) {
            entries.map { it.hours }.average().toFloat()
        } else 0f
    }

    fun getGoalStreak(): Int {
        val entries = getAllSleepEntries()
        var streak = 0
        
        for (entry in entries) {
            if (entry.hours >= entry.goal) {
                streak++
            } else {
                break
            }
        }
        
        return streak
    }

    fun getDailyGoal(): Float {
        return prefs.getFloat(KEY_DAILY_GOAL, 8f)
    }

    fun setDailyGoal(goal: Float) {
        prefs.edit().putFloat(KEY_DAILY_GOAL, goal).apply()
    }

    fun getCurrentDateString(): String {
        val calendar = Calendar.getInstance()
        return SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US).format(calendar.time)
    }

    fun getCurrentTimeString(): String {
        val calendar = Calendar.getInstance()
        return SimpleDateFormat("hh:mm a", Locale.US).format(calendar.time)
    }

    companion object {
        private const val KEY_ENTRY_IDS = "sleep_entry_ids"
        private const val KEY_DAILY_GOAL = "daily_goal"
    }
}

