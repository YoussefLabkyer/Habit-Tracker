package com.example.lab_exam_03

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.lab_exam_03.data.Habit
import com.example.lab_exam_03.data.HabitStore

class ExerciseActivity : AppCompatActivity() {

    private lateinit var habitStore: HabitStore
    private lateinit var tvTotalHabits: TextView
    private lateinit var tvCompletedToday: TextView
    private lateinit var tvStreakCount: TextView
    private lateinit var etHabitName: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etHabitDescription: EditText
    private lateinit var btnAddHabit: Button
    private lateinit var habitsList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)


        val homeBtn = findViewById<Button>(R.id.backbtn1)
        homeBtn.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        habitStore = HabitStore(this)
        initViews()
        setupCategorySpinner()
        setupAddHabitButton()
        refreshUI()
    }



    private fun initViews() {
        tvTotalHabits = findViewById(R.id.tvTotalHabits)
        tvCompletedToday = findViewById(R.id.tvCompletedToday)
        tvStreakCount = findViewById(R.id.tvStreakCount)
        etHabitName = findViewById(R.id.etHabitName)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etHabitDescription = findViewById(R.id.etHabitDescription)
        btnAddHabit = findViewById(R.id.btnAddHabit)
        habitsList = findViewById(R.id.habitsList)
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf("Select a category", "Exercise", "Meditation", "Nutrition", "Sleep", "Hydration", "Reading", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun setupAddHabitButton() {
        btnAddHabit.setOnClickListener {
            val name = etHabitName.text.toString().trim()
            val categoryPosition = spinnerCategory.selectedItemPosition
            val description = etHabitDescription.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a habit name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (categoryPosition == 0) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = spinnerCategory.selectedItem.toString()
            val habit = Habit(
                id = System.currentTimeMillis().toString(),
                name = name,
                category = category,
                description = description,
                createdAt = System.currentTimeMillis()
            )

            habitStore.addHabit(habit)
            clearForm()
            refreshUI()
            Toast.makeText(this, "Habit added successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearForm() {
        etHabitName.setText("")
        spinnerCategory.setSelection(0)
        etHabitDescription.setText("")
    }

    private fun refreshUI() {
        updateStats()
        renderHabits()
    }

    private fun updateStats() {
        val habits = habitStore.getAllHabits()
        val completedToday = habitStore.getCompletedToday()
        val streak = habitStore.getStreakCount()

        tvTotalHabits.text = habits.size.toString()
        tvCompletedToday.text = completedToday.size.toString()
        tvStreakCount.text = streak.toString()
    }

    private fun renderHabits() {
        habitsList.removeAllViews()
        val habits = habitStore.getAllHabits()
        val completedToday = habitStore.getCompletedToday()

        if (habits.isEmpty()) {
            val emptyView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, habitsList, false) as TextView
            emptyView.text = "No habits yet. Add your first habit to get started!"
            emptyView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            emptyView.setPadding(40, 60, 40, 60)
            habitsList.addView(emptyView)
            return
        }

        for (habit in habits) {
            val habitCard = createHabitCard(habit, completedToday.contains(habit.id))
            habitsList.addView(habitCard)
        }
    }

    private fun createHabitCard(habit: Habit, isCompleted: Boolean): View {
        val cardView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(if (isCompleted) 0xFFF0FDF4.toInt() else 0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        // Category badge
        val categoryView = TextView(this).apply {
            text = habit.category
            textSize = 12f
            setTextColor(0xFF22C55E.toInt())
            setBackgroundColor(0xFFF0FDF4.toInt())
            setPadding(12, 4, 12, 4)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8)
            }
        }

        // Title
        val titleView = TextView(this).apply {
            text = habit.name
            textSize = 18f
            setTextColor(0xFF000000.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 4)
            }
        }

        // Description
        val descriptionView = if (habit.description.isNotEmpty()) {
            TextView(this).apply {
                text = habit.description
                textSize = 14f
                setTextColor(0xFF666666.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
            }
        } else null

        // Actions
        val actionsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val completeButton = Button(this).apply {
            text = if (isCompleted) "✓ Completed" else "Mark Complete"
            setBackgroundColor(if (isCompleted) 0xFFFFFFFF.toInt() else 0xFF22C55E.toInt())
            setTextColor(if (isCompleted) 0xFF22C55E.toInt() else 0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(0, 0, 8, 0)
            }
            setOnClickListener {
                habitStore.toggleHabitComplete(habit.id)
                refreshUI()
            }
        }

        val deleteButton = Button(this).apply {
            text = "Delete"
            setBackgroundColor(0xFFFFFFFF.toInt())
            setTextColor(0xFFEF4444.toInt())
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(8, 0, 0, 0)
            }
            setOnClickListener {
                habitStore.deleteHabit(habit.id)
                refreshUI()
                Toast.makeText(this@ExerciseActivity, "Habit deleted", Toast.LENGTH_SHORT).show()
            }
        }

        actionsLayout.addView(completeButton)
        actionsLayout.addView(deleteButton)

        cardView.addView(categoryView)
        cardView.addView(titleView)
        descriptionView?.let { cardView.addView(it) }
        cardView.addView(actionsLayout)

        return cardView
    }


}

