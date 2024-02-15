package com.example.studybuddy.Activity

import Course
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.studybuddy.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminAddCourseSettingActivity : AppCompatActivity() {
    private lateinit var courseCodeText: EditText
    private lateinit var courseNameText: EditText
    private lateinit var courseDescriptionText: EditText
    private lateinit var majors: ArrayList<String>
    private lateinit var courseList: ArrayList<Course>
    private lateinit var majorId: String
    private lateinit var preList: ArrayList<String>
    private lateinit var majorSpinner: Spinner
    private lateinit var addCourseButton: Button
    private lateinit var mergeCourseButton: Button

    //spinner major(100% 5ales)
    //back button(5ales) and titel(mesh 5ales)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_course_setting_admin)
        val customToolbar = findViewById<TextView>(R.id.action_bar_title)
        customToolbar.text = "Add Courses"
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
        val majorSpinner = findViewById<Spinner>(R.id.spinner2)
        val majorsRef = FirebaseDatabase.getInstance().getReference("majors")


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
                                        this@AdminAddCourseSettingActivity,
                                        com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                                        courses
                                    )
                                    //courseSpinner.adapter = adapter2
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

        majorsRef.orderByChild("name").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                majors = ArrayList()
                majors.clear()
                for (majorSnapshot in dataSnapshot.children) {

                    val majorName = majorSnapshot.child("name").getValue(String::class.java)
                    majorName?.let { majors.add(it) }

                    val adapter = ArrayAdapter(
                        this@AdminAddCourseSettingActivity,
                        com.google.android.material.R.layout.support_simple_spinner_dropdown_item, // Replace with the resource ID of your custom list item layout file , // Replace with the ID of the TextView within the custom layout to populate with data
                        majors
                    )


                    majorSpinner.adapter = adapter
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occur
            }
        })

        courseCodeText = findViewById(R.id.courseCodeEditText)
        courseNameText = findViewById(R.id.courseNameEditText)
        courseDescriptionText = findViewById(R.id.descriptionEditText)
        addCourseButton = findViewById(R.id.addCourseButton)

        addCourseButton.setOnClickListener {
            val newCourseCode = courseCodeText.text.toString().trim()
            val newCourseName = courseNameText.text.toString().trim()
            val newCourseDescription = courseDescriptionText.text.toString().trim()

            if (newCourseCode.isNotEmpty() && newCourseName.isNotEmpty() && newCourseDescription.isNotEmpty()) {
                val builder = AlertDialog.Builder(this@AdminAddCourseSettingActivity)
                builder.setTitle("Confirm Course Addition")
                builder.setMessage("Are you sure you want to add this course?")

                builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, _: Int ->
                    val selectedMajor = majorSpinner.selectedItem.toString()

                    val majorsRef = FirebaseDatabase.getInstance().getReference("majors")
                    majorsRef.orderByChild("name").equalTo(selectedMajor).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (majorSnapshot in dataSnapshot.children) {
                                val majorId = majorSnapshot.key.toString()
                                val coursesRef = majorsRef.child(majorId).child("Courses").push()
                                coursesRef.child("CourseCode").setValue(newCourseCode)
                                coursesRef.child("Name").setValue(newCourseName)
                                coursesRef.child("Description").setValue(newCourseDescription)
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle any errors that occur
                        }
                    })

                    dialogInterface.dismiss()
                }

                builder.setNegativeButton("No") { dialogInterface: DialogInterface, _: Int ->
                    // User cancelled the course addition
                    // You can perform any actions here
                    dialogInterface.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            } else {
                // Display an error message if any field is empty
                Toast.makeText(
                    this@AdminAddCourseSettingActivity,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        mergeCourseButton = findViewById(R.id.mergedCoursesButton)

        mergeCourseButton.setOnClickListener {

            val intent = Intent(this, AdminMergedSelectedCourses::class.java)
            intent.putExtra("majorname", majorSpinner.selectedItem.toString())
            startActivity(intent)
        }


    }





























    }

