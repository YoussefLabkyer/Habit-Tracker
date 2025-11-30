package com.example.lab_exam_03

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lab_exam_03.data.WaterStore
import android.widget.NumberPicker
import android.widget.EditText
import android.view.View
import java.util.Calendar

class WaterActivity : AppCompatActivity() {

	private lateinit var store: WaterStore
	private var selectedHour = 9
	private var selectedMinute = 0
	private val NOTIFICATION_PERMISSION_CODE = 100

	@SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_water)

		store = WaterStore(this)

		val progress = findViewById<ProgressBar>(R.id.progress)
		val label = findViewById<TextView>(R.id.label)
		val resetBtn = findViewById<Button>(R.id.resetBtn)
		val btnQuick1 = findViewById<Button>(R.id.btnQuick1)
		val btnQuick2 = findViewById<Button>(R.id.btnQuick2)
		val btnQuick3 = findViewById<Button>(R.id.btnQuick3)
		val etCustomAmount = findViewById<EditText>(R.id.etCustomAmount)
		val btnbackHome = findViewById<Button>(R.id.backHome2)
		val btnSetGoal = findViewById<Button>(R.id.btnSetGoal)

		val tvCurrentAmount = findViewById<TextView>(R.id.tvCurrentAmount)
		val tvDailyGoal = findViewById<TextView>(R.id.tvDailyGoal)
		val tvRemaining = findViewById<TextView>(R.id.tvRemaining)
		val tvProgressPercent = findViewById<TextView>(R.id.tvProgressPercent)
		val tvGoalBig = findViewById<TextView>(R.id.tvGoalBig)
		val btnAddSingle = findViewById<Button>(R.id.btnAddSingle)
		
		// Reminder buttons
		val btnSelectTime = findViewById<Button>(R.id.btnSelectTime)
		val btnSetReminder = findViewById<Button>(R.id.btnSetReminder)
		
		val bars = listOf(
			findViewById<View>(R.id.bar0),
			findViewById<View>(R.id.bar1),
			findViewById<View>(R.id.bar2),
			findViewById<View>(R.id.bar3),
			findViewById<View>(R.id.bar4),
			findViewById<View>(R.id.bar5),
			findViewById<View>(R.id.bar6)
		)

		fun render() {
			val goal = store.getGoalGlasses()
			val consumed = store.getGlassesConsumed()
			val percent = (consumed * 100 / goal).coerceIn(0, 100)
			// Keep progress linear to percentage for consistent UI
			progress.max = 100
			progress.progress = percent
			label.text = "Water: ${consumed} / ${goal} glasses (${percent}%)"
			tvCurrentAmount.text = consumed.toString()
			tvDailyGoal.text = goal.toString()
			tvGoalBig.text = goal.toString()
			tvRemaining.text = (goal - consumed).coerceAtLeast(0).toString()
			tvProgressPercent.text = "${percent}%"

			// Weekly chart render
			val weekData = store.getWeekData()
			val maxHeight = 100
			val density = resources.displayMetrics.density
			for (i in 0 until bars.size) {
				val value = weekData.getOrNull(i) ?: 0
				val ratio = if (goal > 0) (value.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
				val heightPx = (ratio * maxHeight * density).toInt()
				val params = bars[i].layoutParams
				params.height = heightPx
				bars[i].layoutParams = params
			}
		}

		resetBtn.setOnClickListener {
			store.setToday(0)
			render()
		}

		// Quick add buttons add to consumed directly (and weekly data)
		btnQuick1.setOnClickListener { store.addToToday(1); render() }
		btnQuick2.setOnClickListener { store.addToToday(2); render() }
		btnQuick3.setOnClickListener { store.addToToday(3); render() }

		btnAddSingle.setOnClickListener { store.addToToday(1); render() }


		btnbackHome.setOnClickListener {
			val intent = Intent(this, Home::class.java)
			startActivity(intent)
		}

		// Set Goal reads the value from the custom amount field and updates goal
		btnSetGoal.setOnClickListener {
			val value = etCustomAmount.text.toString().toIntOrNull()
			if (value != null && value in 1..20) {
				store.setGoalGlasses(value)
				render()
			}
		}
		
		// Request notification permission
		requestNotificationPermission()
		
		// Time picker
		btnSelectTime.setOnClickListener {
			val calendar = Calendar.getInstance()
			val hour = calendar.get(Calendar.HOUR_OF_DAY)
			val minute = calendar.get(Calendar.MINUTE)
			
			TimePickerDialog(this, { _, selectedHourOfDay, selectedMinuteOfHour ->
				selectedHour = selectedHourOfDay
				selectedMinute = selectedMinuteOfHour
				
				// Format time for display
				val amPm = if (selectedHour >= 12) "PM" else "AM"
				val displayHour = if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour
				val timeText = String.format("%d:%02d %s", displayHour, selectedMinute, amPm)
				btnSelectTime.text = timeText
				
				Toast.makeText(this, "Time selected: $timeText", Toast.LENGTH_SHORT).show()
			}, hour, minute, false).show()
		}
		
		// Set reminder button
		btnSetReminder.setOnClickListener {
			setWaterReminder()
		}

		render()
	}
	
	private fun requestNotificationPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
				!= PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(
					this,
					arrayOf(Manifest.permission.POST_NOTIFICATIONS),
					NOTIFICATION_PERMISSION_CODE
				)
			}
		}
	}
	
	private fun setWaterReminder() {
		// Set alarm for the selected time
		val calendar = Calendar.getInstance()
		calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
		calendar.set(Calendar.MINUTE, selectedMinute)
		calendar.set(Calendar.SECOND, 0)
		calendar.set(Calendar.MILLISECOND, 0)
		
		// If time is in the past, set for tomorrow
		if (calendar.timeInMillis <= System.currentTimeMillis()) {
			calendar.add(Calendar.DAY_OF_YEAR, 1)
		}
		
		val intent = Intent(this, WaterReminderReceiver::class.java)
		val pendingIntent = PendingIntent.getBroadcast(
			this,
			0,
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)
		
		val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
		
		// Save time to SharedPreferences for rescheduling
		val sharedPref = getSharedPreferences("WaterReminder", Context.MODE_PRIVATE)
		val editor = sharedPref.edit()
		editor.putInt("hour", selectedHour)
		editor.putInt("minute", selectedMinute)
		editor.apply()
		
		// Use setExactAndAllowWhileIdle for better reliability
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				alarmManager.setExactAndAllowWhileIdle(
					AlarmManager.RTC_WAKEUP,
					calendar.timeInMillis,
					pendingIntent
				)
			} else {
				alarmManager.setExact(
					AlarmManager.RTC_WAKEUP,
					calendar.timeInMillis,
					pendingIntent
				)
			}
			
			// Format time for display
			val amPm = if (selectedHour >= 12) "PM" else "AM"
			val displayHour = if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour
			val timeText = String.format("%d:%02d %s", displayHour, selectedMinute, amPm)
			
			Toast.makeText(this, "Reminder set for $timeText", Toast.LENGTH_LONG).show()
		} catch (e: Exception) {
			Toast.makeText(this, "Failed to set reminder: ${e.message}", Toast.LENGTH_LONG).show()
		}
	}
}


