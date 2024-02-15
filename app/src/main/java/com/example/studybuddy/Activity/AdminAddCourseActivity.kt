package com.example.studybuddy.Activity

import Course
import GlobalData
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.studybuddy.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminAddCourseActivity : AppCompatActivity() {

    private lateinit var majors: ArrayList<String>
    private lateinit var courseList: ArrayList<Course>
    private lateinit var majorId: String
    private lateinit var courseName: String
    private lateinit var courseId: String
    private lateinit var courseName2: String
    private lateinit var courseDescription: String
    private lateinit var courseCode: String
    private  var studyPlanId: String= ""
    private lateinit var preList: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_course_admin)

        val button: Button = findViewById(R.id.managePrerequisitesButton)


        val majorSpinner = findViewById<Spinner>(R.id.spinner2)
        val courseSpinner = findViewById<Spinner>(R.id.spinner3)
        val majorsRef = FirebaseDatabase.getInstance().getReference("majors")
        majorsRef.orderByChild("name").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                majors = ArrayList()
                majors.clear()
                for (majorSnapshot in dataSnapshot.children) {

                    val majorName = majorSnapshot.child("name").getValue(String::class.java)
                    majorName?.let { majors.add(it) }

                    val adapter = ArrayAdapter(
                        this@AdminAddCourseActivity,
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
                                    courseList = ArrayList() // Move the initialization outside the loop

                                    for (courseSnapshot in dataSnapshot.children) {
                                        val courseName = courseSnapshot.child("Name").getValue(String::class.java)
                                        if (courseName != null) {
                                            courses.add(courseName)
                                        }
                                        val courseDescription =
                                            courseSnapshot.child("Description").getValue(String::class.java)
                                        val courseCode = courseSnapshot.child("CourseCode").getValue(String::class.java)

                                        if (courseName != null && courseDescription != null && courseCode != null) {
                                            val course = Course(courseName, courseCode, courseDescription, "", null)
                                            courseList.add(course) // Add each course to the list
                                        }
                                    }

                                    // Update the spinner adapter with the course list
                                    val adapter2 = ArrayAdapter(
                                        this@AdminAddCourseActivity,
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


        //Allows selected major courses to appear in the courses spinner
        courseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                val selectedMajorName = majorSpinner.selectedItem as String

                majorsRef.orderByChild("name").equalTo(selectedMajorName)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (majorSnapshot in dataSnapshot.children) {
                                // Step 2: Retrieve the major ID
                                majorId = majorSnapshot.key.toString()


                                // Step 3: Retrieve the selected course
                                courseName = courseSpinner.selectedItem as String


                                val coursesRef = majorsRef.child(majorId).child("Courses")
                                coursesRef.orderByChild("Name").equalTo(courseName)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(courseSnapshot: DataSnapshot) {
                                            for (courseSnapshot in courseSnapshot.children) {
                                                // Step 4: Retrieve the course attributes
                                                courseId = courseSnapshot.key.toString()
                                                courseCode =
                                                    courseSnapshot.child("CourseCode").getValue(String::class.java)
                                                        .toString()
                                                courseDescription =
                                                    courseSnapshot.child("Description").getValue(String::class.java)
                                                        .toString()
                                                courseName2 =
                                                    courseSnapshot.child("Name").getValue(String::class.java).toString()


                                                val codeEditText = findViewById<TextView>(R.id.courseCodeEditText)
                                                val descriptionEditText =
                                                    findViewById<TextView>(R.id.descriptionEditText)
                                                val nameEditText = findViewById<TextView>(R.id.courseNameEditText)

                                                // Step 5: Update the EditText fields with the retrieved course attributes
                                                codeEditText.text=courseCode
                                                descriptionEditText.text=courseDescription
                                                nameEditText.text=courseName2
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
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

        button.setOnClickListener {
            val intent = Intent(this, AdminPrerequisitesActivity::class.java)
            intent.putExtra("studyPlanId",studyPlanId.toString())
            startActivity(intent)

        }
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()

        }



        val addCourseButton = findViewById<Button>(R.id.addCourseButton)


        addCourseButton.setOnClickListener {
            preList = ArrayList()
            val prerequisitesMap = intent.getSerializableExtra("prerequisites") as? HashMap<String, Course>

            if (prerequisitesMap != null) {
                for ((key, _) in prerequisitesMap) {
                    preList.add(key)
                }
            }


            checkIfCourseExists(GlobalData.globalMajorId, GlobalData.studyPlanId, courseSpinner.selectedItem.toString()) { courseExists ->
                if(!courseExists) {
                    val courseToAdd = Course(courseName2, courseCode, courseDescription,"", preList)
                    majorsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (majorSnapshot in dataSnapshot.children) {
                                val studyPlansRef = majorSnapshot.child("Study Plans").ref
                                studyPlansRef.child(GlobalData.studyPlanId)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(studyPlanSnapshot: DataSnapshot) {
                                            if (studyPlanSnapshot.exists()) {
                                                val coursesRef = studyPlanSnapshot.child("courses").ref
                                                val courseId = coursesRef.push().key


                                                if (courseId != null) {
                                                    coursesRef.child(courseId).setValue(courseToAdd)
                                                        .addOnSuccessListener {
                                                            // Course inserted successfully
                                                            GlobalData.savedPrerequisites.clear()
                                                            Toast.makeText(
                                                                this@AdminAddCourseActivity,
                                                                "Course added successfully",
                                                                Toast.LENGTH_SHORT
                                                            ).show()

                                                        }
                                                        .addOnFailureListener { e ->
                                                            // Error occurred while inserting course
                                                        }
                                                }
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                            // Error occurred while accessing study plan
                                        }
                                    })
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Error occurred while accessing majors
                        }
                    })
                }
                else
                {

                    val alertDialog = AlertDialog.Builder(this@AdminAddCourseActivity)
                        .setTitle("Course Already Added")
                        .setMessage("The course you selected has already been added.")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                    alertDialog.show()
                }
            }
        }


        button.setOnClickListener {
            val intent = Intent(this, AdminPrerequisitesActivity::class.java)
            intent.putExtra("studyPlanId",studyPlanId.toString())
            intent.putExtra("noPre",courseSpinner.selectedItem.toString())
            startActivity(intent)

        }





    }
    private fun checkIfCourseExists(majorId: String, studyPlanId: String, courseName: String, callback: (Boolean) -> Unit) {
        val studyPlanRef = FirebaseDatabase.getInstance().getReference("majors")
            .child(majorId)
            .child("Study Plans")
            .child(studyPlanId)



        studyPlanRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val coursesSnapshot = dataSnapshot.child("courses")
                for (courseSnapshot in coursesSnapshot.children) {
                    val existingCourseName = courseSnapshot.child("name").getValue(String::class.java)
                    if (existingCourseName == courseName) {
                        // Course already exists
                        callback(true)
                        return
                    }
                }
                callback(false)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Error occurred while accessing study plan
                callback(false)
            }
        })
    }



}
