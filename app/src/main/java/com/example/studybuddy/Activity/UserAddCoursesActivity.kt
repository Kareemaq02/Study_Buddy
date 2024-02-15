package com.example.studybuddy.Activity

import Course
import GlobalData
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.studybuddy.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserAddCoursesActivity : AppCompatActivity() {

    private lateinit var majors: ArrayList<String>
    private lateinit var courseList: ArrayList<Course>
    private lateinit var majorId: String
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var semestersRef: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_courses)
        val addCourseButton = findViewById<Button>(R.id.addCourseButton)
        firebaseAuth = FirebaseAuth.getInstance()
        semestersRef = FirebaseDatabase.getInstance().getReference("users")
            .child(firebaseAuth.currentUser?.uid ?: "")

        val semesterNumber = intent.getStringExtra("semesterNumber")
        val selectedSemester = intent.getStringExtra("selectedSemester")
        val uniqeSemesterId = intent.getStringExtra("uniqeSemesterId")



        val majorSpinner = findViewById<Spinner>(R.id.spinner2)
        val courseSpinner = findViewById<Spinner>(R.id.spinner3)
        val grade = findViewById<EditText>(R.id.grade)
        grade.text.clear()
        val majorsRef = FirebaseDatabase.getInstance().getReference("majors")
        majorsRef.orderByChild("name").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                majors = ArrayList()
                for (majorSnapshot in dataSnapshot.children) {
                    val majorName = majorSnapshot.child("name").getValue(String::class.java)
                    majorName?.let { majors.add(it) }
                }
                val adapter = ArrayAdapter(
                    this@UserAddCoursesActivity,
                    com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                    majors
                )
                majorSpinner.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occur
            }
        })

        addCourseButton.setOnClickListener {


            val selectedMajor = majorSpinner.selectedItem.toString()
            var selectedCourse = courseList[courseSpinner.selectedItemPosition]
            val courseGrade = grade.text.toString()
            val selector = "$selectedSemester - $semesterNumber"
            selectedCourse.grade = courseGrade

            // Additional functionality to add the selected course to the semester
            val semesterRef = semestersRef.child(selector ?: "")
            val coursesRef = semesterRef.child("courses")
            val courseRef = coursesRef.push()
            courseRef.child("name").setValue(selectedCourse.name)
            courseRef.child("grade").setValue(selectedCourse.grade)


            // Display a success message or perform any additional actions
        }

        majorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val query = majorsRef.orderByChild("name").equalTo(majors[position])
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (majorSnapshot in dataSnapshot.children) {
                            majorId = majorSnapshot.key.toString() // Retrieve the key of the major

                            // Fetch the courses for the selected major using the major ID
                            val coursesRef = FirebaseDatabase.getInstance().getReference("majors")
                                .child(majorId)
                                .child("Courses")

                            coursesRef.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val courses = ArrayList<String>()
                                    courseList = ArrayList()

                                    for (courseSnapshot in dataSnapshot.children) {
                                        val courseName = courseSnapshot.child("Name").getValue(String::class.java)
                                        val courseDescription = courseSnapshot.child("Description").getValue(String::class.java)
                                        val courseCode = courseSnapshot.child("CourseCode").getValue(String::class.java)

                                        if (courseName != null && courseDescription != null && courseCode != null) {
                                            val course = Course(courseName, courseCode, courseDescription, "", null)
                                            courses.add(courseName)
                                            courseList.add(course)
                                        }
                                    }

                                    val adapter2 = ArrayAdapter(
                                        this@UserAddCoursesActivity,
                                        com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                                        courses
                                    )
                                    courseSpinner.adapter = adapter2
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle any errors that occur
                                }
                            })

                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle any errors that occur
                    }
                })
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Write code to perform some action
            }
        }

        courseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Handle item selection
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Write code to perform some action
            }
        }
    }
}
