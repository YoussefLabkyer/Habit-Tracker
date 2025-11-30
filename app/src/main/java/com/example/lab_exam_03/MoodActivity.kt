package com.example.lab_exam_03

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.lab_exam_03.data.MoodEntry
import com.example.lab_exam_03.data.MoodStore

class MoodActivity : AppCompatActivity() {

    private lateinit var moodStore: MoodStore
    private lateinit var etMoodNote: EditText
    private lateinit var btnLogMood: Button
    private lateinit var listView: LinearLayout
    private lateinit var calendarView: GridLayout
    private lateinit var btnListView: Button
    private lateinit var btnCalendarView: Button
    private lateinit var tvTotalEntries: TextView
    private lateinit var tvThisWeek: TextView
    private lateinit var tvTopMood: TextView

    private var selectedEmoji: String? = null
    private var selectedMood: String? = null
    private var selectedButton: Button? = null

    private val emojiButtons = mutableListOf<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("MoodActivity", "MoodActivity onCreate called")
        setContentView(R.layout.activity_mood)

        moodStore = MoodStore(this)
        initViews()
        setupEmojiButtons()
        setupLogButton()
        setupViewToggle()
        refreshUI()
    }

    private fun initViews() {
        etMoodNote = findViewById(R.id.etMoodNote)
        btnLogMood = findViewById(R.id.btnLogMood)
        listView = findViewById(R.id.listView)
        calendarView = findViewById(R.id.calendarView)
        btnListView = findViewById(R.id.btnListView)
        btnCalendarView = findViewById(R.id.btnCalendarView)
        tvTotalEntries = findViewById(R.id.tvTotalEntries)
        tvThisWeek = findViewById(R.id.tvThisWeek)
        tvTopMood = findViewById(R.id.tvTopMood)

        // Collect all emoji buttons
        emojiButtons.addAll(listOf(
            findViewById(R.id.emoji1), findViewById(R.id.emoji2), findViewById(R.id.emoji3),
            findViewById(R.id.emoji4), findViewById(R.id.emoji5), findViewById(R.id.emoji6),
            findViewById(R.id.emoji7), findViewById(R.id.emoji8), findViewById(R.id.emoji9),
            findViewById(R.id.emoji10)
        ))
    }

    private fun setupEmojiButtons() {
        for (button in emojiButtons) {
            button.setOnClickListener {
                // Reset all buttons to transparent background
                for (btn in emojiButtons) {
                    btn.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                }
                

                button.setBackgroundColor(0xFF90EE90.toInt())
                selectedButton = button
                selectedEmoji = button.text.toString()
                selectedMood = button.tag.toString()
                btnLogMood.isEnabled = true
            }
        }
    }

    private fun setupLogButton() {
        btnLogMood.setOnClickListener {
            if (selectedEmoji != null && selectedMood != null) {
                val moodEntry = MoodEntry(
                    id = System.currentTimeMillis().toString(),
                    emoji = selectedEmoji!!,
                    mood = selectedMood!!,
                    note = etMoodNote.text.toString().trim(),
                    date = moodStore.getCurrentDateString(),
                    time = moodStore.getCurrentTimeString(),
                    timestamp = System.currentTimeMillis()
                )

                moodStore.addMood(moodEntry)
                clearForm()
                refreshUI()
                Toast.makeText(this, "Mood logged successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearForm() {
        // Reset emoji selection to transparent background
        for (btn in emojiButtons) {
            btn.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }
        selectedButton = null
        selectedEmoji = null
        selectedMood = null
        etMoodNote.setText("")
        btnLogMood.isEnabled = false
    }

    private fun setupViewToggle() {
        btnListView.setOnClickListener {
            btnListView.setBackgroundColor(0xFF22C55E.toInt())
            btnListView.setTextColor(0xFFFFFFFF.toInt())
            btnCalendarView.setBackgroundColor(0xFFFFFFFF.toInt())
            btnCalendarView.setTextColor(0xFF000000.toInt())
            
            listView.visibility = View.VISIBLE
            calendarView.visibility = View.GONE
        }

        btnCalendarView.setOnClickListener {
            btnCalendarView.setBackgroundColor(0xFF22C55E.toInt())
            btnCalendarView.setTextColor(0xFFFFFFFF.toInt())
            btnListView.setBackgroundColor(0xFFFFFFFF.toInt())
            btnListView.setTextColor(0xFF000000.toInt())
            
            listView.visibility = View.GONE
            calendarView.visibility = View.VISIBLE
            renderCalendarView()
        }
    }

    private fun refreshUI() {
        updateStats()
        renderListView()
    }

    private fun updateStats() {
        val moods = moodStore.getAllMoods()
        tvTotalEntries.text = moods.size.toString()
        tvThisWeek.text = moodStore.getThisWeekCount().toString()
        tvTopMood.text = moodStore.getMostCommonMood()
    }

    private fun renderListView() {
        listView.removeAllViews()
        val moods = moodStore.getAllMoods()

        if (moods.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "📝\n\nNo mood entries yet. Start logging your feelings!"
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setPadding(40, 60, 40, 60)
                setTextColor(0xFF666666.toInt())
                textSize = 16f
            }
            listView.addView(emptyView)
            return
        }

        for (mood in moods) {
            val moodCard = createMoodCard(mood)
            listView.addView(moodCard)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createMoodCard(mood: MoodEntry): View {
        val cardView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(0xFFF8F8F8.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 15)
            }
        }

        // Emoji
        val emojiView = TextView(this).apply {
            text = mood.emoji
            textSize = 48f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 20, 0)
            }
        }

        // Details container
        val detailsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        // Mood name
        val moodNameView = TextView(this).apply {
            text = mood.mood
            textSize = 16f
            setTextColor(0xFF000000.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        // Date and time
        val dateTimeView = TextView(this).apply {
            text = "${mood.date} at ${mood.time}"
            textSize = 14f
            setTextColor(0xFF666666.toInt())
            setPadding(0, 5, 0, if (mood.note.isNotEmpty()) 10 else 0)
        }

        detailsContainer.addView(moodNameView)
        detailsContainer.addView(dateTimeView)

        // Note (if exists)
        if (mood.note.isNotEmpty()) {
            val noteView = TextView(this).apply {
                text = mood.note
                textSize = 14f
                setTextColor(0xFF333333.toInt())
            }
            detailsContainer.addView(noteView)
        }

        // Delete button
        val deleteButton = Button(this).apply {
            text = "Delete"
            setBackgroundColor(0xFFFFFFFF.toInt())
            setTextColor(0xFFEF4444.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                moodStore.deleteMood(mood.id)
                refreshUI()
                Toast.makeText(this@MoodActivity, "Mood deleted", Toast.LENGTH_SHORT).show()
            }
        }

        cardView.addView(emojiView)
        cardView.addView(detailsContainer)
        cardView.addView(deleteButton)

        return cardView
    }

    private fun renderCalendarView() {
        calendarView.removeAllViews()
        
        // Simple calendar for current month
        val moods = moodStore.getAllMoods()
        val moodsByDate = moods.groupBy { it.date }
        
        // Create 30 day squares for simplicity
        for (day in 1..30) {
            val dayView = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(10, 10, 10, 10)
                setBackgroundColor(0xFFF8F8F8.toInt())
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 120
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(5, 5, 5, 5)
                }
                gravity = android.view.Gravity.CENTER
            }

            val dayNumber = TextView(this).apply {
                text = day.toString()
                textSize = 12f
                setTextColor(0xFF000000.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            dayView.addView(dayNumber)

            // Check if there's a mood for this day (simplified)
            val dayMood = moods.find { it.date.contains(day.toString()) }
            if (dayMood != null) {
                dayView.setBackgroundColor(0xFFF0FDF4.toInt())
                val emojiView = TextView(this).apply {
                    text = dayMood.emoji
                    textSize = 24f
                }
                dayView.addView(emojiView)
            }

            calendarView.addView(dayView)
        }
    }
}
