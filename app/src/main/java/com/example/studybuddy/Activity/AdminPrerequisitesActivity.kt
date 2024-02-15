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

class AdminPrerequisitesActivity : AppCompatActivity() {

    private lateinit var majors: ArrayList<String>
    private lateinit var listView: ListView
    private lateinit var spinner: Spinner
    private lateinit var spinner2: Spinner
    private var arrayList = ArrayList<String>()
    val prerequisites = HashMap<String, Course>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prerequisites_management_page)


        listView = findViewById(R.id.prerequisitesListView)
        spinner = findViewById(R.id.majorSpinner)
        spinner2 = findViewById(R.id.courseSpinner)

        arrayList=GlobalData.savedPrerequisites
        val majorsRef = FirebaseDatabase.getInstance().getReference("majors")
        majorsRef.orderByChild("name").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                majors = ArrayList()
                majors.clear()
                for (majorSnapshot in dataSnapshot.children) {

                    val majorName = majorSnapshot.child("name").getValue(String::class.java)
                    majorName?.let { majors.add(it) }

                    val adapter = ArrayAdapter(
                        this@AdminPrerequisitesActivity,
                        com.google.android.material.R.layout.support_simple_spinner_dropdown_item, // Replace with the resource ID of your custom list item layout file , // Replace with the ID of the TextView within the custom layout to populate with data
                        majors
                    )



                    spinner.adapter = adapter
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occur
            }
        })


        val adapter = ArrayAdapter(
            this@AdminPrerequisitesActivity,
            R.layout.list_view_item_small,
            R.id.itemTextView,
            arrayList
        )
        listView.adapter = adapter

//Allows selected major courses to appear in the courses spinner
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val query = majorsRef.orderByChild("name").equalTo(majors[position])
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (majorSnapshot in dataSnapshot.children) {
                            val majorId = majorSnapshot.key.toString() // Retrieve the key of the major


                            // Fetch the courses for the selected major using the major ID
                            val coursesRef = FirebaseDatabase.getInstance().getReference("majors")
                                .child(majorId)
                                .child("Courses")

                            coursesRef.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val courses = ArrayList<String>()


                                    for (courseSnapshot in dataSnapshot.children) {
                                        val courseName = courseSnapshot.child("Name").getValue(String::class.java)
                                        if (courseName != null) {
                                            courses.add(courseName)
                                        }



                                    }

                                    // Update the spinner adapter with the course list

                                    val adapter2 = ArrayAdapter(
                                        this@AdminPrerequisitesActivity,
                                        com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                                        courses
                                    )
                                    spinner2.adapter = adapter2
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





        val addPrerequisiteButton = findViewById<Button>(R.id.addPrerequisiteButton)

        addPrerequisiteButton.setOnClickListener{
            var flag = false
            var flag2 = false

            if(spinner2.selectedItem.toString()==intent.getStringExtra("noPre"))
                flag2=true
            for (courseName in arrayList)
            {
                if(courseName==spinner2.selectedItem)
                    flag = true
            }
            if(!flag2) {
                if (!flag) {
                    arrayList.add(spinner2.selectedItem as String)
                    adapter.notifyDataSetChanged()

                    val query = majorsRef.orderByChild("name").equalTo(spinner.selectedItem.toString())

                    query.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (majorSnapshot in dataSnapshot.children) {
                                val coursesSnapshot = majorSnapshot.child("Courses")
                                for (courseSnapshot in coursesSnapshot.children) {
                                    val courseId = courseSnapshot.key.toString()
                                    val courseName = courseSnapshot.child("Name").getValue(String::class.java)
                                    val courseCode = courseSnapshot.child("CourseCode").getValue(String::class.java)
                                    val courseDescription =
                                        courseSnapshot.child("Description").getValue(String::class.java)
                                    if (courseName == spinner2.selectedItem.toString()) {

                                        if (courseCode != null && courseDescription != null) {
                                            val course = Course(courseName, courseCode, courseDescription, "", null)
                                            prerequisites[courseId] = course
                                        }
                                    }
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle any errors that occur
                        }
                    })


                } else {
                    val alertDialog = AlertDialog.Builder(this)
                        .setTitle("Prerequisite Already Added")
                        .setMessage("The course you selected has already been added as a prerequisite.")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                    alertDialog.show()

                }
            }
            else
            {
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle("Please select a different prerequisite")
                    .setMessage("A course can't be its own prerequisite.")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                alertDialog.show()
            }


        }


        listView.setOnItemLongClickListener { _, _, position, _ ->
            val selectedPrerequisite = listView.getItemAtPosition(position) as String


            val builder = AlertDialog.Builder(this)
            builder.setTitle("Remove Prerequisite")
            builder.setMessage("Are you sure you want to remove $selectedPrerequisite?")
            builder.setPositiveButton("Yes") { _, _ ->
            arrayList.remove(selectedPrerequisite)
                adapter.notifyDataSetChanged()
                prerequisites.remove(spinner2.selectedItem.toString())
                Toast.makeText(this, "Prerequisite removed", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("Cancel") { _, _ ->

            }
            val dialog = builder.create()
            dialog.show()


            true // Return true to indicate that the event is consumed
        }


        val saveButton = findViewById<Button>(R.id.savePrerequisitesButton)

        saveButton.setOnClickListener{
            val intent = Intent(this, AdminAddCourseActivity::class.java)
            intent.putExtra("prerequisites",prerequisites)
            GlobalData.savedPrerequisites=arrayList
            startActivity(intent)


        }


        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()


        }






    }


}
