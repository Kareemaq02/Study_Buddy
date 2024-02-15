package com.example.studybuddy.data

data class User(
    val email: String,
    var password: String,
    val firstname: String,
    val lastname: String,
    var major: String,
    var numStars: Double,
    var ratersCount: Int
)
