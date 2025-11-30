package com.example.lab_exam_03

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Onboarding2Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding2)

        val btnNext: Button = findViewById(R.id.btnNext)
        val btnSkip: Button = findViewById(R.id.btnSkip)

        btnNext.setOnClickListener {
            startActivity(Intent(this, Onboarding3Activity::class.java))
        }
        btnSkip.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
