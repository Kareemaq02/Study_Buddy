package com.example.studybuddy.Activity
import GlobalData
import StudyPlan
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


class AdminMergedSelectedCourses : AppCompatActivity() {
    private lateinit var listView: ListView
    private var arrayList = ArrayList<String>()
    private lateinit var button: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.merged_selected_courses_view)
        val customToolbar = findViewById<TextView>(R.id.action_bar_title)
        customToolbar.text = "View Courses"


        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        listView = findViewById(R.id.courseCodeList)

        val majorName = intent.getStringExtra("majorname")
        val majorsRef = FirebaseDatabase.getInstance().getReference("majors")
        majorsRef.orderByChild("name").equalTo(majorName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (majorSnapshot in dataSnapshot.children) {
                    val coursesRef = majorSnapshot.child("Courses").ref
                    coursesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(studyPlanDataSnapshot: DataSnapshot) {
                            arrayList.clear()
                            for (studyPlanSnapshot in studyPlanDataSnapshot.children) {
                                val courseName = studyPlanSnapshot.child("Name").getValue(String::class.java)
                                courseName?.let { arrayList.add(it) }
                            }

                            val adapter = ArrayAdapter(
                                this@AdminMergedSelectedCourses,
                                R.layout.list_view_item, // Replace with the resource ID of your custom list item layout file
                                R.id.itemTextView, // Replace with the ID of the TextView within the custom layout to populate with data
                                arrayList
                            )

                            listView.adapter = adapter
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
}

