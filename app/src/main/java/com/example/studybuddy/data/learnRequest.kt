package com.example.studybuddy.data

data class learnRequest(
    val requestId: String? = null,
    val major_Name: String,
    val course_Name: String,
    val req_time: String,
    val req_date: String,
    val pref_time: String,
    val pref_date: String,
    val userId: String,
    var upvoters: Int,
    val requestDescription: String,
    val requestType: String,
    var upVotersIds: ArrayList<String>
)
{
    // Empty constructor for Firebase deserialization
    constructor() : this("","", "", "", "", "", "", "", 0, "", "",ArrayList())
}