package com.example.lab_exam_03.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.lab_exam_03.R
import android.content.Intent
import com.example.lab_exam_03.WaterActivity
import com.example.lab_exam_03.ExerciseActivity
import com.example.lab_exam_03.MoodActivity
import com.example.lab_exam_03.SleepActivity
import com.example.lab_exam_03.data.WaterStore
import com.example.lab_exam_03.data.SleepStore

class HomeFragment : Fragment() {
    private var progressBar: ProgressBar? = null
    private var countText: TextView? = null
    private var percentText: TextView? = null
    private var sleepProgressBar: ProgressBar? = null
    private var sleepCountText: TextView? = null
    private var sleepPercentText: TextView? = null
    private var glassesTodayText: TextView? = null
    private var sleepHoursText: TextView? = null
    private var logWaterBtn: Button? = null
    private var sleepQualityBtn: Button? = null
    private var recordExerciseBtn: Button? = null
    private var moodJournalBtn: Button? = null
    private var waterStore: WaterStore? = null
    private var sleepStore: SleepStore? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        progressBar = view.findViewById(R.id.progressWater)
        countText = view.findViewById(R.id.waterCountText)
        percentText = view.findViewById(R.id.waterPercentText)
        sleepProgressBar = view.findViewById(R.id.progressSleep)
        sleepCountText = view.findViewById(R.id.sleepCountText)
        sleepPercentText = view.findViewById(R.id.sleepPercentText)
        glassesTodayText = view.findViewById(R.id.tvGlassesToday)
        sleepHoursText = view.findViewById(R.id.tvSleepHours)
        // Initialize button references
        logWaterBtn = view.findViewById(R.id.btnLogWater)
        sleepQualityBtn = view.findViewById(R.id.btnSleepQuality)
        recordExerciseBtn = view.findViewById(R.id.btnRecordExercise)
        moodJournalBtn = view.findViewById(R.id.btnMoodJournal)
        waterStore = WaterStore(requireContext())
        sleepStore = SleepStore(requireContext())

        // Setup Intent navigation for Quick Actions
        setupQuickActionNavigation()
        return view
    }

    override fun onResume() {
        super.onResume()
        renderWater()
        renderSleep()
    }

    private fun setupQuickActionNavigation() {
        // Log Water - Navigate to WaterActivity
        logWaterBtn?.setOnClickListener {
            val intent = Intent(requireContext(), WaterActivity::class.java)
            startActivity(intent)
        }

        // Sleep Quality - Navigate to SleepActivity
        sleepQualityBtn?.setOnClickListener {
            val intent = Intent(requireContext(), SleepActivity::class.java)
            startActivity(intent)
        }

        // Record Exercise - Navigate to ExerciseActivity
        recordExerciseBtn?.setOnClickListener {
            val intent = Intent(requireContext(), ExerciseActivity::class.java)
            startActivity(intent)
        }

        // Mood Journal - Navigate to MoodActivity
        moodJournalBtn?.setOnClickListener {
            val intent = Intent(requireContext(), MoodActivity::class.java)
            startActivity(intent)
        }
    }

    private fun renderWater() {
        val store = waterStore ?: return
        val goal = store.getGoalGlasses()
        val consumed = store.getGlassesConsumed()
        val percent = (consumed * 100 / goal).coerceIn(0, 100)
        progressBar?.max = 100
        progressBar?.progress = percent
        countText?.text = "${consumed} of ${goal} glasses"
        percentText?.text = "${percent}%"
        glassesTodayText?.text = consumed.toString()
    }

    private fun renderSleep() {
        val store = sleepStore ?: return
        val todaySleep = store.getTodaySleep()
        val dailyGoal = store.getDailyGoal()
        val percent = if (todaySleep > 0) {
            ((todaySleep / dailyGoal) * 100).toInt().coerceIn(0, 100)
        } else 0

        // Update Today's Progress card
        sleepHoursText?.text = if (todaySleep > 0) todaySleep.toString() else "0"
        
        // Update Sleep Quality section
        sleepProgressBar?.max = 100
        sleepProgressBar?.progress = percent
        sleepCountText?.text = "${todaySleep} of ${dailyGoal} hours"
        sleepPercentText?.text = "${percent}%"
    }
}


