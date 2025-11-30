package com.example.lab_exam_03

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Onboarding1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding1)

        val btnNext: Button = findViewById(R.id.btnNext)
        val btnSkip: Button = findViewById(R.id.btnSkip)

        btnNext.setOnClickListener {
            startActivity(Intent(this, Onboarding2Activity::class.java))
        }
        btnSkip.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
