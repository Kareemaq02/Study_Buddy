package com.example.studybuddy.Activity

import CourseAdapter
import SemesterCourse
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserSemesterCoursesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_semester_courses)

        val customToolbar = findViewById<TextView>(R.id.action_bar_title)
        customToolbar.text = getString(R.string.courses)

        recyclerView = findViewById(R.id.semesterRecyclerView)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val email = GlobalData.userEmail
        val usersRef = FirebaseDatabase.getInstance().getReference("users")

        val userQuery = usersRef.orderByChild("email").equalTo(email)
        println(email)
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val courseList = mutableListOf<SemesterCourse>()
                for (userSnapshot in dataSnapshot.children) {
                    val semesterDataSnapshot = userSnapshot.child("semester")
                    for (semesterSnapshot in semesterDataSnapshot.children) {
                        val semesterName = semesterSnapshot.child("name").getValue(String::class.java)
                        println(semesterName)
                        if (semesterName == intent.getStringExtra("semesterName")) {
                            val coursesSnapshot = semesterSnapshot.child("Courses")
                            for (courseSnapshot in coursesSnapshot.children) {
                                val courseName = courseSnapshot.child("name").getValue(String::class.java)
                                val courseLocation = courseSnapshot.child("location").getValue(String::class.java)
                                val courseTime = courseSnapshot.child("time").getValue(String::class.java)
                                val courseDate = courseSnapshot.child("date").getValue(String::class.java)

                                val semesterCourse = SemesterCourse(courseName ?: "", courseLocation ?: "", courseTime ?: "", courseDate ?: "")
                                println(semesterCourse)
                                semesterCourse.let { courseList.add(semesterCourse) }
                            }
                        }
                    }
                }
                println(courseList)
                val adapter = CourseAdapter(
                    courseList // Pass the SemesterCourse list to the adapter
                )
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this@UserSemesterCoursesActivity)

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occur
            }
        })
    }
}
