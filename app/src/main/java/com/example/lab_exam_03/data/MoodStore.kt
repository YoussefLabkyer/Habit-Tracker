package com.example.lab_exam_03.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

data class MoodEntry(
    val id: String,
    val emoji: String,
    val mood: String,
    val note: String,
    val date: String,
    val time: String,
    val timestamp: Long
)

class MoodStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mood_store", Context.MODE_PRIVATE)

    fun getAllMoods(): List<MoodEntry> {
        val moods = mutableListOf<MoodEntry>()
        val moodIds = prefs.getString(KEY_MOOD_IDS, "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
        
        for (id in moodIds) {
            val emoji = prefs.getString("mood_emoji_$id", "") ?: ""
            val mood = prefs.getString("mood_name_$id", "") ?: ""
            val note = prefs.getString("mood_note_$id", "") ?: ""
            val date = prefs.getString("mood_date_$id", "") ?: ""
            val time = prefs.getString("mood_time_$id", "") ?: ""
            val timestamp = prefs.getLong("mood_timestamp_$id", 0L)
            
            if (emoji.isNotEmpty()) {
                moods.add(MoodEntry(id, emoji, mood, note, date, time, timestamp))
            }
        }
        
        // Sort by timestamp descending (newest first)
        return moods.sortedByDescending { it.timestamp }
    }

    fun addMood(moodEntry: MoodEntry) {
        val currentIds = prefs.getString(KEY_MOOD_IDS, "")?.split(",")?.filter { it.isNotEmpty() }?.toMutableList() ?: mutableListOf()
        currentIds.add(0, moodEntry.id) // Add to beginning for newest first
        
        prefs.edit()
            .putString(KEY_MOOD_IDS, currentIds.joinToString(","))
            .putString("mood_emoji_${moodEntry.id}", moodEntry.emoji)
            .putString("mood_name_${moodEntry.id}", moodEntry.mood)
            .putString("mood_note_${moodEntry.id}", moodEntry.note)
            .putString("mood_date_${moodEntry.id}", moodEntry.date)
            .putString("mood_time_${moodEntry.id}", moodEntry.time)
            .putLong("mood_timestamp_${moodEntry.id}", moodEntry.timestamp)
            .apply()
    }

    fun deleteMood(moodId: String) {
        val currentIds = prefs.getString(KEY_MOOD_IDS, "")?.split(",")?.filter { it.isNotEmpty() && it != moodId } ?: emptyList()
        
        prefs.edit()
            .putString(KEY_MOOD_IDS, currentIds.joinToString(","))
            .remove("mood_emoji_$moodId")
            .remove("mood_name_$moodId")
            .remove("mood_note_$moodId")
            .remove("mood_date_$moodId")
            .remove("mood_time_$moodId")
            .remove("mood_timestamp_$moodId")
            .apply()
    }

    fun getThisWeekCount(): Int {
        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        return getAllMoods().count { it.timestamp > oneWeekAgo }
    }

    fun getMostCommonMood(): String {
        val moods = getAllMoods()
        if (moods.isEmpty()) return "-"
        
        val moodCounts = mutableMapOf<String, Int>()
        for (mood in moods) {
            moodCounts[mood.emoji] = (moodCounts[mood.emoji] ?: 0) + 1
        }
        
        return moodCounts.maxByOrNull { it.value }?.key ?: "-"
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
        private const val KEY_MOOD_IDS = "mood_ids"
    }
}

