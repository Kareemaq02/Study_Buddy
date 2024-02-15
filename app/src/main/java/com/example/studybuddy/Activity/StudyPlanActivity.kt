package com.example.studybuddy.Activity
import GlobalData
import GlobalData.globalMajorId
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


class StudyPlanActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private var arrayList = ArrayList<String>()
    private lateinit var button: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_study_plan)

        val customToolbar = findViewById<TextView>(R.id.action_bar_title)
        customToolbar.text = intent.getStringExtra("majorName")


        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        listView = findViewById(R.id.studyplanListView2)

        val majorName = intent.getStringExtra("majorName")
        val majorsRef = FirebaseDatabase.getInstance().getReference("majors")
        majorsRef.orderByChild("name").equalTo(majorName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (majorSnapshot in dataSnapshot.children) {
                    val studyPlansRef = majorSnapshot.child("Study Plans").ref
                    studyPlansRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(studyPlanDataSnapshot: DataSnapshot) {
                            arrayList.clear()
                            for (studyPlanSnapshot in studyPlanDataSnapshot.children) {
                                val studyPlanName = studyPlanSnapshot.child("name").getValue(String::class.java)
                                studyPlanName?.let { arrayList.add(it) }
                            }

                            val adapter = ArrayAdapter(
                                this@StudyPlanActivity,
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

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedStudyPlan = arrayList[position]
            val intent = Intent(this, StudyPlanCoursesActivity::class.java)
            intent.putExtra("studyPlanName", selectedStudyPlan)

            // Retrieve the study plan ID for the selected study plan
            val studyPlanIdRef = majorsRef.orderByChild("name").equalTo(majorName)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (majorSnapshot in dataSnapshot.children) {
                            val studyPlansRef = majorSnapshot.child("Study Plans").ref
                            studyPlansRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(studyPlanDataSnapshot: DataSnapshot) {
                                    for (studyPlanSnapshot in studyPlanDataSnapshot.children) {
                                        val studyPlanName = studyPlanSnapshot.child("name").getValue(String::class.java)
                                        if (studyPlanName == selectedStudyPlan) {
                                            val studyPlanId = studyPlanSnapshot.key // Retrieve the study plan ID
                                            GlobalData.studyPlanId = studyPlanId.toString()
                                            intent.putExtra("studyPlanId", studyPlanId)
                                            intent.putExtra("majorName", majorName)
                                            startActivity(intent)
                                            return  // Exit the loop once the study plan ID is found
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
        }
        listView.setOnItemLongClickListener { parent, view, position, id ->
            val selectedStudyPlan = arrayList[position]

            val majorId = GlobalData.globalMajorId

            val builder = AlertDialog.Builder(listView.context)
            builder.setTitle(selectedStudyPlan)
            builder.setMessage("Are you sure you want to remove $selectedStudyPlan?")
            builder.setPositiveButton("Yes") { _, _ ->
                val studyPlansRef = FirebaseDatabase.getInstance().getReference("majors").child(majorId).child("Study Plans")

                studyPlansRef.orderByChild("name").equalTo(selectedStudyPlan)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (studyPlanSnapshot in dataSnapshot.children) {
                                    studyPlanSnapshot.ref.removeValue()
                                        .addOnSuccessListener {
                                            Toast.makeText(listView.context, "Study plan removed", Toast.LENGTH_SHORT).show()
                                            arrayList.removeAt(position)
                                            (listView.adapter as ArrayAdapter<String>).notifyDataSetChanged()
                                        }

                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
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






        button = findViewById(R.id.addStudyPlan)
        button.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_studyplan_admin, null)
            val dialogBuilder = AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Add Study Plan")

            val dialog = dialogBuilder.create()
            dialog.show()

            val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
            val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
            val studyPlanNameEditText = dialogView.findViewById<EditText>(R.id.studyPlanNameEditText)

            confirmButton.setOnClickListener {
                val studyPlanName = studyPlanNameEditText.text.toString()
                if (studyPlanName.isNotEmpty()) {
                    val studyPlanData = StudyPlan(studyPlanName, emptyMap())
                    val majorId = intent.getStringExtra("majorId")!!

                    val newStudyPlanRef =
                        majorsRef.child(majorId).child("Study Plans").push()
                    newStudyPlanRef.setValue(studyPlanData)

                    // Add the new study plan to the array list
                    arrayList.add(studyPlanName)

                    dialog.dismiss()
                } else {
                    val alertDialogBuilder = AlertDialog.Builder(this)
                    alertDialogBuilder.setTitle("Invalid Study Plan Name")
                        .setMessage("Study Plan name cannot be empty")
                        .setPositiveButton("OK", null)
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                }
            }
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }
        }





    }
}

