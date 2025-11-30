package com.example.lab_exam_03.data

import android.content.Context
import android.content.SharedPreferences

class WaterStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("water_store", Context.MODE_PRIVATE)

    fun getGlassesConsumed(): Int = prefs.getInt(KEY_GLASSES, 0)

    fun setGlassesConsumed(value: Int) {
        prefs.edit().putInt(KEY_GLASSES, value.coerceIn(0, MAX_GLASSES)).apply()
    }

    fun increment() {
        setGlassesConsumed(getGlassesConsumed() + 1)
    }

    fun reset() {
        setGlassesConsumed(0)
    }

    fun getGoalGlasses(): Int = prefs.getInt(KEY_GOAL, DEFAULT_GOAL)

    fun setGoalGlasses(value: Int) {
        prefs.edit().putInt(KEY_GOAL, value.coerceAtLeast(1)).apply()
    }

    // Weekly tracking: store 7 integers keyed by weekStart + dayIndex
    fun addToToday(amount: Int) {
        setGlassesConsumed(getGlassesConsumed() + amount)
        val (weekKey, dayIndex) = currentWeekKeyAndDay()
        val key = weeklyKey(weekKey, dayIndex)
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, (current + amount).coerceAtLeast(0)).apply()
        
        // Also store for progress tracking
        val dateKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        setWaterForDate(dateKey, getGlassesConsumed())
    }

    fun setToday(total: Int) {
        val delta = total - getGlassesConsumed()
        setGlassesConsumed(total)
        val (weekKey, dayIndex) = currentWeekKeyAndDay()
        val key = weeklyKey(weekKey, dayIndex)
        prefs.edit().putInt(key, total.coerceAtLeast(0)).apply()
        
        // Also store for progress tracking
        val dateKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        setWaterForDate(dateKey, total)
    }

    fun getWeekData(): List<Int> {
        ensureWeek()
        val (weekKey, _) = currentWeekKeyAndDay()
        return (0 until 7).map { day -> prefs.getInt(weeklyKey(weekKey, day), 0) }
    }

    fun ensureWeek() {
        val (currentWeekKey, _) = currentWeekKeyAndDay()
        val storedWeek = prefs.getString(KEY_WEEK_KEY, null)
        if (storedWeek != currentWeekKey) {
            // New week: clear previous 7-day values and reset today's total
            storedWeek?.let { old ->
                for (i in 0 until 7) prefs.edit().remove(weeklyKey(old, i)).apply()
            }
            prefs.edit().putString(KEY_WEEK_KEY, currentWeekKey).apply()
            setGlassesConsumed(0)
        }
    }

    private fun currentWeekKeyAndDay(): Pair<String, Int> {
        val cal = java.util.Calendar.getInstance()
        cal.firstDayOfWeek = java.util.Calendar.MONDAY
        // Compute Monday of this week
        val now = cal.time
        val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
        val delta = ((dayOfWeek + 5) % 7) // 0 = Monday, 6 = Sunday
        cal.add(java.util.Calendar.DAY_OF_MONTH, -delta)
        val monday = cal.time
        val weekKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(monday)

        // Day index 0..6
        cal.time = now
        val dayIndex = (dayOfWeek + 5) % 7
        return Pair(weekKey, dayIndex)
    }

    private fun weeklyKey(weekKey: String, dayIndex: Int): String = "week_${weekKey}_$dayIndex"

    fun getWaterForDate(dateKey: String): Int {
        return prefs.getInt("water_$dateKey", 0)
    }

    fun setWaterForDate(dateKey: String, amount: Int) {
        prefs.edit().putInt("water_$dateKey", amount.coerceAtLeast(0)).apply()
    }

    companion object {
        private const val KEY_GLASSES = "glasses"
        private const val KEY_GOAL = "goal"
        private const val KEY_WEEK_KEY = "weekKey"
        private const val DEFAULT_GOAL = 8
        private const val MAX_GLASSES = 20
    }
}



