package com.example.studybuddy.Activity

import GlobalData
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.studybuddy.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AdminViewCourseInfoActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_info)

        val courseName: String = intent.getStringExtra("courseName").toString()


        val customToolbar = findViewById<TextView>(R.id.action_bar_title)
        customToolbar.text = courseName


        val majorNameTextView = findViewById<TextView>(R.id.majorName)
        majorNameTextView.text = intent.getStringExtra("majorName").toString()

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val majorsRef = FirebaseDatabase.getInstance().getReference("majors")
        val majorId = GlobalData.globalMajorId // Replace with the desired major ID

        var courseCode: String
        var courseDescription: String

        val majorRef = majorsRef.child(majorId)
        val coursesRef = majorRef.child("Courses")

        findViewById<TextView>(R.id.courseName3).text = courseName
        coursesRef.child(intent.getStringExtra("courseId").toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                     courseCode = dataSnapshot.child("CourseCode").getValue(String::class.java).toString()
                     courseDescription=dataSnapshot.child("Description").getValue(String::class.java).toString()
                    findViewById<TextView>(R.id.courseCode).text = courseCode
                    findViewById<TextView>(R.id.courseDescription).text = courseDescription
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle any errors that occur
                }
            })


    }
}

