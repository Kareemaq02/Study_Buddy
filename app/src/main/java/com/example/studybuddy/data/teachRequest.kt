package com.example.studybuddy.data

data class teachRequest(
    var requestId: String? = null,
    val major_Name: String,
    val course_Name: String,
    val req_time: String,
    val req_date: String,
    val start_time: String,
    val start_date: String,
    val end_time: String,
    val end_date: String,
    val place: String,
    val duration: Int,
    val userId: String,
    val status: String,
    val studentsLimit: Int,
    val lessonDescription: String,
    val number_of_students: Int,
    val requestType: String,
    var participantsIds: ArrayList<String>
){
    // Empty constructor for Firebase deserialization
    constructor() : this("","", "", "", "", "", "", "", "", "", 0, "", "", 0, "", 0, "",ArrayList())
}