package com.example.studybuddy.Activity

import GlobalData
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.studybuddy.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StudyPlanCoursesActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private var courseList = ArrayList<String>()
    private lateinit var button: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_study_plan_courses)
        val customToolbar = findViewById<TextView>(R.id.action_bar_title)
        customToolbar.text = intent.getStringExtra("studyPlanName")

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        listView = findViewById(R.id.studyplanListView)

        val majorName = intent.getStringExtra("majorName")
        val studyPlanName = intent.getStringExtra("studyPlanName")

        val majorsRef = FirebaseDatabase.getInstance().getReference("majors")
        majorsRef.orderByChild("name").equalTo(majorName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (majorSnapshot in dataSnapshot.children) {
                    val studyPlansRef = majorSnapshot.child("Study Plans").ref
                    studyPlansRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(studyPlanDataSnapshot: DataSnapshot) {
                            for (studyPlanSnapshot in studyPlanDataSnapshot.children) {
                                val name = studyPlanSnapshot.child("name").getValue(String::class.java)
                                if (name == studyPlanName) {
                                    val coursesRef = studyPlanSnapshot.child("courses").ref
                                    coursesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(coursesDataSnapshot: DataSnapshot) {
                                            courseList.clear()
                                            GlobalData.studyPlanCourseList?.clear()
                                            for (courseSnapshot in coursesDataSnapshot.children) {
                                                val courseName = courseSnapshot.child("name").getValue(String::class.java)
                                                courseName?.let { courseList.add(it) }
                                                GlobalData.studyPlanCourseList?.let { courseList.add(it.toString()) }
                                            }
                                            val adapter = ArrayAdapter(
                                                this@StudyPlanCoursesActivity,
                                                R.layout.list_view_item, // Replace with the resource ID of your custom list item layout file
                                                R.id.itemTextView, // Replace with the ID of the TextView within the custom layout to populate with data
                                                courseList
                                            )
                                            listView.adapter = adapter

                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                            // Handle any errors that occur
                                        }
                                    })
                                    break
                                }
                            }
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

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedCourse = courseList[position]
            val intent = Intent(this, AdminViewCourseInfoActivity::class.java)
            intent.putExtra("courseName", selectedCourse)

            val majorId = GlobalData.globalMajorId // Replace with the desired major ID
            val majorRef = majorsRef.child(majorId)
            val coursesRef = majorRef.child("Courses")
            val majorNameRef = majorRef.child("name")

            majorNameRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    intent.putExtra("majorName", snapshot.getValue(String::class.java))

                    val query = coursesRef.orderByChild("Name").equalTo(selectedCourse)
                    query.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (courseSnapshot in dataSnapshot.children) {
                                val courseId = courseSnapshot.key
                                intent.putExtra("courseId", courseId)
                            }

                            startActivity(intent) // Move startActivity call here
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle any errors that occur
                        }
                    })
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle any errors that occur
                }
            })
        }
        listView.setOnItemLongClickListener { _, view, position, _ ->
            val selectedCourse = courseList[position]
            val majorId = GlobalData.globalMajorId
            val builder = AlertDialog.Builder(view.context)
            builder.setTitle(selectedCourse)
            builder.setMessage("Are you sure you want to remove $selectedCourse?")
            builder.setPositiveButton("Yes") { _, _ ->
                val studyPlansRef = FirebaseDatabase.getInstance().getReference("majors").child(majorId).child("Study Plans")
                studyPlansRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (studyPlanSnapshot in dataSnapshot.children) {
                            val coursesRef = studyPlanSnapshot.child("courses").ref

                            coursesRef.orderByChild("name").equalTo(selectedCourse)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(coursesDataSnapshot: DataSnapshot) {
                                        if (coursesDataSnapshot.exists()) {
                                            for (courseSnapshot in coursesDataSnapshot.children) {
                                                courseSnapshot.ref.removeValue()
                                                    .addOnSuccessListener {
                                                        Toast.makeText(view.context, "Course removed", Toast.LENGTH_SHORT).show()
                                                        courseList.removeAt(position)
                                                        (listView.adapter as ArrayAdapter<String>).notifyDataSetChanged()
                                                    }
                                            }
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                    }
                                })
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle any errors that occur
                    }
                })
            }

            builder.setNegativeButton("Cancel") { _, _ ->
                // Cancel button clicked
            }

            val dialog = builder.create()
            dialog.show()

            true
        }





        button = findViewById(R.id.addCourse)
        button.setOnClickListener {
            val intent = Intent(this, AdminAddCourseActivity::class.java)
            intent.putExtra("studyPlanName", studyPlanName)
            startActivity(intent)

        }
    }
}
