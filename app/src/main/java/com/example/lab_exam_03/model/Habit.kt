package com.example.lab_exam_03.model

data class Habit(
    val id: String,
    var name: String,
    val completedDates: MutableSet<String> = mutableSetOf()
)


