package com.example.studybuddy.data

data class LearnRequestItem(
    val requestId: String,
    val firstName: String,
    val lastName: String,
    val description: String,
    val upvotersCount: Int,
    val major: String,
    val preferredTime: String,
    val courseName: String
    )