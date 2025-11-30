package com.example.lab_exam_03.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.lab_exam_03.R
import com.example.lab_exam_03.data.WaterStore
import com.example.lab_exam_03.data.MoodStore
import com.example.lab_exam_03.data.SleepStore
import java.text.SimpleDateFormat
import java.util.*

class ProgressFragment : Fragment() {

    private var waterStore: WaterStore? = null
    private var moodStore: MoodStore? = null
    private var sleepStore: SleepStore? = null

    private var selectedPeriod = 14 // Default to 14 days

    // UI Elements
    private var btn7Days: Button? = null
    private var btn14Days: Button? = null
    private var btn30Days: Button? = null

    // Stats TextViews
    private var tvAvgWater: TextView? = null
    private var tvAvgMood: TextView? = null
    private var tvAvgSleep: TextView? = null
    private var tvTotalEntries: TextView? = null

    // Water Chart Info
    private var tvWaterTotal: TextView? = null
    private var tvWaterGoalAchievement: TextView? = null
    private var tvWaterBestDay: TextView? = null

    // Mood Chart Info
    private var tvMoodAverage: TextView? = null
    private var tvMoodBestDay: TextView? = null
    private var tvMoodTrend: TextView? = null

    // Sleep Chart Info
    private var tvSleepAverage: TextView? = null
    private var tvSleepTotal: TextView? = null
    private var tvSleepBestNight: TextView? = null

    // Chart Views
    private var waterChartView: ChartView? = null
    private var moodChartView: ChartView? = null
    private var sleepChartView: ChartView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progress, container, false)

        // Initialize stores
        waterStore = WaterStore(requireContext())
        moodStore = MoodStore(requireContext())
        sleepStore = SleepStore(requireContext())

        // Initialize UI elements
        initializeViews(view)
        setupTimeButtons()
        setupChartViews(view)
        updateButtonStates() // Set initial button state

        return view
    }

    override fun onResume() {
        super.onResume()
        updateAllData()
    }

    private fun initializeViews(view: View) {
        // Time buttons
        btn7Days = view.findViewById(R.id.btn7Days)
        btn14Days = view.findViewById(R.id.btn14Days)
        btn30Days = view.findViewById(R.id.btn30Days)

        // Stats
        tvAvgWater = view.findViewById(R.id.tvAvgWater)
        tvAvgMood = view.findViewById(R.id.tvAvgMood)
        tvAvgSleep = view.findViewById(R.id.tvAvgSleep)
        tvTotalEntries = view.findViewById(R.id.tvTotalEntries)

        // Water info
        tvWaterTotal = view.findViewById(R.id.tvWaterTotal)
        tvWaterGoalAchievement = view.findViewById(R.id.tvWaterGoalAchievement)
        tvWaterBestDay = view.findViewById(R.id.tvWaterBestDay)

        // Mood info
        tvMoodAverage = view.findViewById(R.id.tvMoodAverage)
        tvMoodBestDay = view.findViewById(R.id.tvMoodBestDay)
        tvMoodTrend = view.findViewById(R.id.tvMoodTrend)

        // Sleep info
        tvSleepAverage = view.findViewById(R.id.tvSleepAverage)
        tvSleepTotal = view.findViewById(R.id.tvSleepTotal)
        tvSleepBestNight = view.findViewById(R.id.tvSleepBestNight)
    }

    private fun setupTimeButtons() {
        btn7Days?.setOnClickListener {
            selectPeriod(7)
            updateButtonStates()
            updateAllData()
        }
        btn14Days?.setOnClickListener {
            selectPeriod(14)
            updateButtonStates()
            updateAllData()
        }
        btn30Days?.setOnClickListener {
            selectPeriod(30)
            updateButtonStates()
            updateAllData()
        }
    }

    private fun selectPeriod(days: Int) {
        selectedPeriod = days
    }

    private fun updateButtonStates() {
        // Reset all buttons
        btn7Days?.apply {
            setBackgroundResource(R.drawable.bg_time_button)
            setTextColor(Color.BLACK)
        }
        btn14Days?.apply {
            setBackgroundResource(R.drawable.bg_time_button)
            setTextColor(Color.BLACK)
        }
        btn30Days?.apply {
            setBackgroundResource(R.drawable.bg_time_button)
            setTextColor(Color.BLACK)
        }

        // Highlight selected button
        val selectedButton = when(selectedPeriod) {
            7 -> btn7Days
            14 -> btn14Days
            30 -> btn30Days
            else -> btn14Days
        }

        selectedButton?.apply {
            setBackgroundColor(Color.parseColor("#22c55e"))
            setTextColor(Color.WHITE)
        }
    }

    private fun setupChartViews(view: View) {
        waterChartView = ChartView(requireContext()).apply {
            chartType = ChartType.LINE
            primaryColor = Color.parseColor("#22c55e")
            goalColor = Color.parseColor("#86efac")
        }

        moodChartView = ChartView(requireContext()).apply {
            chartType = ChartType.AREA
            primaryColor = Color.parseColor("#22c55e")
        }

        sleepChartView = ChartView(requireContext()).apply {
            chartType = ChartType.BAR
            primaryColor = Color.parseColor("#22c55e")
            goalColor = Color.parseColor("#86efac")
        }

        // Replace placeholder views
        val waterPlaceholder = view.findViewById<View>(R.id.waterChartView)
        val moodPlaceholder = view.findViewById<View>(R.id.moodChartView)
        val sleepPlaceholder = view.findViewById<View>(R.id.sleepChartView)

        replaceView(waterPlaceholder, waterChartView!!)
        replaceView(moodPlaceholder, moodChartView!!)
        replaceView(sleepPlaceholder, sleepChartView!!)
    }

    private fun replaceView(oldView: View, newView: View) {
        val parent = oldView.parent as ViewGroup
        val index = parent.indexOfChild(oldView)
        val layoutParams = oldView.layoutParams
        parent.removeView(oldView)
        parent.addView(newView, index, layoutParams)
    }

    private fun updateAllData() {
        updateStats()
        updateWaterChart()
        updateMoodChart()
        updateSleepChart()
    }

    private fun updateStats() {
        val waterData = getWaterDataForPeriod()
        val moodData = getMoodDataForPeriod()
        val sleepData = getSleepDataForPeriod()

        // Calculate averages
        val avgWater = if (waterData.isNotEmpty()) waterData.average() else 0.0
        val avgMood = if (moodData.isNotEmpty()) moodData.average() else 0.0
        val avgSleep = if (sleepData.isNotEmpty()) sleepData.average() else 0.0

        // Update UI
        tvAvgWater?.text = String.format("%.1f", avgWater)
        tvAvgMood?.text = String.format("%.1f", avgMood)
        tvAvgSleep?.text = String.format("%.1f", avgSleep)

        // Total entries (days with any data)
        val totalDays = maxOf(waterData.size, moodData.size, sleepData.size)
        tvTotalEntries?.text = totalDays.toString()
    }

    private fun updateWaterChart() {
        val data = getWaterDataForPeriod()
        val goal = waterStore?.getGoalGlasses() ?: 8

        // Update chart (force Double list)
        waterChartView?.setData(data.map { it.toDouble() }, goal.toDouble())

        // Update water info
        val total = data.sum().toInt()
        val goalAchievement = if (data.isNotEmpty()) {
            ((data.count { it >= goal } / data.size.toDouble()) * 100).toInt()
        } else 0
        val bestDay = data.maxOrNull()?.toInt() ?: 0

        tvWaterTotal?.text = "$total glasses"
        tvWaterGoalAchievement?.text = "$goalAchievement%"
        tvWaterBestDay?.text = "$bestDay glasses"
    }

    private fun updateMoodChart() {
        val data = getMoodDataForPeriod()

        // Update chart (force Double list)
        moodChartView?.setData(data.map { it.toDouble() }, 5.0) // Max mood is 5

        // Update mood info
        val average = if (data.isNotEmpty()) data.average() else 0.0
        val bestDay = data.maxOrNull() ?: 0.0
        val trend = if (data.size >= 2) {
            val recent = data.takeLast(3).average()
            val older = data.take(3).average()
            if (recent > older) "↑ Improving" else if (recent < older) "↓ Declining" else "→ Stable"
        } else "→ Stable"

        tvMoodAverage?.text = String.format("%.1f/5", average)
        tvMoodBestDay?.text = String.format("%.1f/5", bestDay)
        tvMoodTrend?.text = trend

        // Set trend color
        val trendColor = when {
            trend.startsWith("↑") -> Color.parseColor("#22c55e")
            trend.startsWith("↓") -> Color.parseColor("#ef4444")
            else -> Color.parseColor("#666666")
        }
        tvMoodTrend?.setTextColor(trendColor)
    }

    private fun updateSleepChart() {
        val data = getSleepDataForPeriod()
        val goal = sleepStore?.getDailyGoal() ?: 8.0

        // Update chart (force Double list)
        sleepChartView?.setData(data.map { it.toDouble() }, goal.toDouble())

        // Update sleep info
        val average = if (data.isNotEmpty()) data.average() else 0.0
        val total = data.sum()
        val bestNight = data.maxOrNull() ?: 0.0

        tvSleepAverage?.text = String.format("%.1f hrs", average)
        tvSleepTotal?.text = String.format("%.0f hrs", total)
        tvSleepBestNight?.text = String.format("%.1f hrs", bestNight)
    }

    private fun getWaterDataForPeriod(): List<Double> {
        val data = mutableListOf<Double>()
        val calendar = Calendar.getInstance()

        for (i in selectedPeriod - 1 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)

            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val consumption = waterStore?.getWaterForDate(dateKey) ?: 0
            data.add(consumption.toDouble())
        }

        return data
    }

    private fun getMoodDataForPeriod(): List<Double> {
        val data = mutableListOf<Double>()
        val calendar = Calendar.getInstance()
        val moodEntries = moodStore?.getAllMoods() ?: emptyList()

        for (i in selectedPeriod - 1 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)

            val dateStr = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(calendar.time)

            // Find mood entries for this date and calculate average
            val dayEntries = moodEntries.filter { it.date == dateStr }
            val avgMood = if (dayEntries.isNotEmpty()) {
                dayEntries.map { getMoodScore(it.mood) }.average()
            } else 0.0

            data.add(avgMood)
        }

        return data
    }

    private fun getSleepDataForPeriod(): List<Double> {
        val data = mutableListOf<Double>()
        val calendar = Calendar.getInstance()
        val sleepEntries = sleepStore?.getAllSleepEntries() ?: emptyList()

        for (i in selectedPeriod - 1 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)

            val dateStr = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(calendar.time)

            // Find sleep entry for this date
            val dayEntry = sleepEntries.find { it.date == dateStr }
            val sleepHours = dayEntry?.hours?.toDouble() ?: 0.0

            data.add(sleepHours)
        }

        return data
    }

    private fun getMoodScore(mood: String): Double {
        return when(mood) {
            "Happy" -> 5.0
            "Confident" -> 4.5
            "Excited" -> 4.5
            "Loved" -> 4.0
            "Peaceful" -> 4.0
            "Thoughtful" -> 3.0
            "Tired" -> 2.5
            "Anxious" -> 2.0
            "Sad" -> 1.5
            "Angry" -> 1.0
            else -> 3.0
        }
    }
}
