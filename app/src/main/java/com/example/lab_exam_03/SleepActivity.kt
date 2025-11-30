package com.example.lab_exam_03

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.lab_exam_03.data.SleepEntry
import com.example.lab_exam_03.data.SleepStore

class SleepActivity : AppCompatActivity() {

    private lateinit var sleepStore: SleepStore
    private lateinit var tvPercentage: TextView
    private lateinit var tvCurrentSleep: TextView
    private lateinit var tvGoalDisplay: TextView
    private lateinit var etSleepHours: EditText
    private lateinit var etSleepGoal: EditText
    private lateinit var btnLogSleep: Button
    private lateinit var tvAvgSleep: TextView
    private lateinit var tvTotalEntries: TextView
    private lateinit var tvGoalStreak: TextView
    private lateinit var historyList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep)

        sleepStore = SleepStore(this)
        initViews()
        setupLogButton()
        refreshUI()
    }

    private fun initViews() {
        tvPercentage = findViewById(R.id.tvPercentage)
        tvCurrentSleep = findViewById(R.id.tvCurrentSleep)
        tvGoalDisplay = findViewById(R.id.tvGoalDisplay)
        etSleepHours = findViewById(R.id.etSleepHours)
        etSleepGoal = findViewById(R.id.etSleepGoal)
        btnLogSleep = findViewById(R.id.btnLogSleep)
        tvAvgSleep = findViewById(R.id.tvAvgSleep)
        tvTotalEntries = findViewById(R.id.tvTotalEntries)
        tvGoalStreak = findViewById(R.id.tvGoalStreak)
        historyList = findViewById(R.id.historyList)

        // Set initial goal value
        etSleepGoal.setText(sleepStore.getDailyGoal().toString())
    }

    private fun setupLogButton() {
        btnLogSleep.setOnClickListener {
            val hoursText = etSleepHours.text.toString().trim()
            val goalText = etSleepGoal.text.toString().trim()

            if (hoursText.isEmpty()) {
                Toast.makeText(this, "Please enter sleep hours", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hours = hoursText.toFloatOrNull()
            if (hours == null || hours <= 0) {
                Toast.makeText(this, "Please enter valid sleep hours", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val goal = if (goalText.isNotEmpty()) {
                goalText.toFloatOrNull() ?: sleepStore.getDailyGoal()
            } else {
                sleepStore.getDailyGoal()
            }

            // Update goal if changed
            if (goal != sleepStore.getDailyGoal()) {
                sleepStore.setDailyGoal(goal)
            }

            val percentage = ((hours / goal) * 100).toInt().coerceAtMost(100)
            
            val sleepEntry = SleepEntry(
                id = System.currentTimeMillis().toString(),
                hours = hours,
                goal = goal,
                percentage = percentage,
                date = sleepStore.getCurrentDateString(),
                time = sleepStore.getCurrentTimeString(),
                timestamp = System.currentTimeMillis()
            )

            sleepStore.addSleepEntry(sleepEntry)
            etSleepHours.setText("")
            refreshUI()
            Toast.makeText(this, "Sleep logged successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshUI() {
        updateMainDisplay()
        updateStats()
        renderHistory()
    }

    private fun updateMainDisplay() {
        val todaySleep = sleepStore.getTodaySleep()
        val dailyGoal = sleepStore.getDailyGoal()
        val percentage = if (todaySleep > 0) {
            ((todaySleep / dailyGoal) * 100).toInt().coerceAtMost(100)
        } else 0

        tvPercentage.text = "$percentage%"
        tvCurrentSleep.text = "${todaySleep}h"
        tvGoalDisplay.text = "${dailyGoal}h"
        etSleepGoal.setText(dailyGoal.toString())
    }

    private fun updateStats() {
        val entries = sleepStore.getAllSleepEntries()
        val avgSleep = sleepStore.getAverageSleep()
        val totalEntries = entries.size
        val goalStreak = sleepStore.getGoalStreak()

        tvAvgSleep.text = "${String.format("%.1f", avgSleep)}h"
        tvTotalEntries.text = totalEntries.toString()
        tvGoalStreak.text = goalStreak.toString()
    }

    private fun renderHistory() {
        historyList.removeAllViews()
        val entries = sleepStore.getAllSleepEntries()

        if (entries.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "😴\n\nNo sleep entries yet. Start logging your sleep!"
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setPadding(40, 60, 40, 60)
                setTextColor(0xFF666666.toInt())
                textSize = 16f
            }
            historyList.addView(emptyView)
            return
        }

        for (entry in entries) {
            val historyCard = createHistoryCard(entry)
            historyList.addView(historyCard)
        }
    }

    private fun createHistoryCard(entry: SleepEntry): View {
        val cardView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(getQualityBackgroundColor(entry.percentage))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 15)
            }
        }

        // Left side - Date and quality
        val leftContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val dateView = TextView(this).apply {
            text = entry.date + " " + getQualityBadge(entry.percentage)
            textSize = 16f
            setTextColor(0xFF000000.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val timeView = TextView(this).apply {
            text = entry.time
            textSize = 14f
            setTextColor(0xFF666666.toInt())
        }

        leftContainer.addView(dateView)
        leftContainer.addView(timeView)

        // Right side - Hours and delete button
        val rightContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val hoursContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 20, 0)
            }
        }

        val hoursView = TextView(this).apply {
            text = "${entry.hours}h"
            textSize = 28f
            setTextColor(0xFF22C55E.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val percentageView = TextView(this).apply {
            text = "${entry.percentage}%"
            textSize = 18f
            setTextColor(0xFF666666.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        hoursContainer.addView(hoursView)
        hoursContainer.addView(percentageView)

        val deleteButton = Button(this).apply {
            text = "Delete"
            setBackgroundColor(0xFFFFFFFF.toInt())
            setTextColor(0xFFEF4444.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                sleepStore.deleteSleepEntry(entry.id)
                refreshUI()
                Toast.makeText(this@SleepActivity, "Sleep entry deleted", Toast.LENGTH_SHORT).show()
            }
        }

        rightContainer.addView(hoursContainer)
        rightContainer.addView(deleteButton)

        cardView.addView(leftContainer)
        cardView.addView(rightContainer)

        return cardView
    }

    private fun getQualityBackgroundColor(percentage: Int): Int {
        return when {
            percentage >= 100 -> 0xFFF0FDF4.toInt() // Light green
            percentage >= 75 -> 0xFFFEFCE8.toInt()  // Light yellow
            else -> 0xFFFEF2F2.toInt()               // Light red
        }
    }

    private fun getQualityBadge(percentage: Int): String {
        return when {
            percentage >= 100 -> "✅ Excellent"
            percentage >= 75 -> "⚠️ Good"
            else -> "❌ Poor"
        }
    }
}

