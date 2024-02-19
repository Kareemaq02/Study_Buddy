package com.example.studybuddy.Activity

import CourseAdapter
import SemesterCourse
import UserAddSemesterCourseDialogFragment
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserSemesterCoursesActivity : AppCompatActivity(), CourseAdapter.OnDeleteLongClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CourseAdapter
    private var courseList = mutableListOf<SemesterCourse>()

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

        val addSemesterCourseButton = findViewById<FloatingActionButton>(R.id.addSemesterCourse)
        addSemesterCourseButton.setOnClickListener {
            // Open the dialog here
            val dialog = UserAddSemesterCourseDialogFragment()
            dialog.setAdapter(adapter, this) // Pass adapter and fragment reference
            dialog.show(supportFragmentManager, "AddCourseDialogFragment")
        }

        val email = GlobalData.userEmail
        val usersRef = FirebaseDatabase.getInstance().getReference("users")

        val userQuery = usersRef.orderByChild("email").equalTo(email)
        println(email)
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
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
                adapter = CourseAdapter(courseList, this@UserSemesterCoursesActivity)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this@UserSemesterCoursesActivity)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occur
            }
        })
    }

    override fun onDeleteLongClick(course: SemesterCourse) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Course")
        builder.setMessage("Are you sure you want to delete ${course.name}?")
        builder.setPositiveButton("Yes") { _, _ ->
            // Remove the course from the list and update the adapter
            courseList.remove(course)
            adapter.notifyDataSetChanged()
            // Perform deletion from the database
            deleteCourseFromDatabase(course)
        }
        builder.setNegativeButton("No", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteCourseFromDatabase(course: SemesterCourse) {
        val userEmail = GlobalData.userEmail
        val database = FirebaseDatabase.getInstance().reference
        val usersRef = database.child("users")

        val userQuery = usersRef.orderByChild("email").equalTo(userEmail)

        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach { userSnapshot ->
                    val semesterSnapshot = userSnapshot.child("semester").children.find {
                        it.child("name").getValue(String::class.java) == intent.getStringExtra("semesterName")
                    }

                    semesterSnapshot?.let { semester ->
                        val coursesSnapshot = semester.child("Courses")

                        coursesSnapshot.children.forEach { courseSnapshot ->
                            if (courseSnapshot.child("name").getValue(String::class.java) == course.name) {
                                courseSnapshot.ref.removeValue().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Deletion successful
                                        Toast.makeText(applicationContext, "Course deleted successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Handle deletion failure
                                        Toast.makeText(applicationContext, "Failed to delete course", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                return@forEach
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle cancellation
                Toast.makeText(applicationContext, "Database operation cancelled", Toast.LENGTH_SHORT).show()
            }
        })
    }
    fun updateAdapterData(newData: List<SemesterCourse>) {
        courseList.clear()
        courseList.addAll(newData)
        adapter.notifyDataSetChanged()
    }
}
