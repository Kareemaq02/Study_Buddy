package com.example.studybuddy.fragments
import Course
import GlobalData
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.studybuddy.R
import com.example.studybuddy.data.*
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class RequestLessonFragment : Fragment() {
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private  var selectedTime: String = "Select Time"
    private  var actDate: String = ""
    private lateinit var majors: ArrayList<String>
    private lateinit var majorId: String
    private lateinit var courseList: ArrayList<Course>
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_request_lesson, container, false)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinnerMajor: Spinner = view.findViewById(R.id.spinnerMajor2)
        val spinnerCourses: Spinner = view.findViewById(R.id.spinnerCourses2)
        val btnSubmitRequest: Button = view.findViewById(R.id.btnSubmitRequest)



        dateButton = view.findViewById(R.id.dateButton)
        dateButton.setOnClickListener{
            openDatePicker()
        }
        initDatePicker()

        timeButton = view.findViewById(R.id.timeButton)
        timeButton.setOnClickListener {
            openTimePicker()
        }
        val majorsRef = FirebaseDatabase.getInstance().getReference("majors")
        majorsRef.orderByChild("name").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                majors = ArrayList()
                majors.clear()
                for (majorSnapshot in dataSnapshot.children) {

                    val majorName = majorSnapshot.child("name").getValue(String::class.java)
                    majorName?.let { majors.add(it) }

                    val majorAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, majors)
                    spinnerMajor.adapter = majorAdapter


                    spinnerMajor.adapter = majorAdapter

                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occur
            }
        })


        spinnerMajor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                                            val course = Course(courseName, courseCode, courseDescription, "",null)
                                            courseList.add(course) // Add each course to the list
                                        }
                                    }

                                    val courseAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses)
                                    spinnerCourses.adapter = courseAdapter
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


        btnSubmitRequest.setOnClickListener {
            val majorName = spinnerMajor.selectedItem.toString()
            //course
            val courseName = spinnerCourses.selectedItem.toString()

            //get current time
            val calendar = Calendar.getInstance()
            val hour = calendar[Calendar.HOUR_OF_DAY]
            val minute = calendar[Calendar.MINUTE]
            val reqTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            //get req date
            val dateFormat = SimpleDateFormat("MM dd yyyy")
            val reqDate: String = dateFormat.format(Date())
            //get startDate
            val prefStartDate: String = if(actDate=="") {
                "Any"
            } else {
                actDate
            }


            //get start time
            val prefStartTime = timeButton.text.toString()

            //get lesson description
            val briefDescription = view.findViewById<EditText>(R.id.Description).text.toString()

            //logged in User Id
            GlobalData.loggedInUserId



            if(briefDescription!="")
            {

                val arrayList = ArrayList<String>()
                arrayList.add(GlobalData.loggedInUserId)
                val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                val requestsRef: DatabaseReference = database.getReference("requests")
                val newRequestRef: DatabaseReference = requestsRef.push()
                val request = learnRequest(newRequestRef.key,majorName,courseName, reqTime,reqDate,prefStartTime,prefStartDate, GlobalData.loggedInUserId,
                    1, briefDescription,"Learn request",arrayList)
                newRequestRef.setValue(request)

                val context = requireContext() // Replace with your activity or context
                Toast.makeText(context, "Your request has been posted successfully", Toast.LENGTH_SHORT).show()


                requireActivity().finish()






            }
            else
            {
                val alertDialog = AlertDialog.Builder(context)
                    .setTitle("Invalid Description")
                    .setMessage("Lesson Description cannot be empty.")
                    .setPositiveButton("OK", null)
                    .create()

                alertDialog.show()

            }

        }

    }
    private fun openDatePicker() {
        datePickerDialog.show()
    }
    private fun initDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
            val formattedDate = makeDateString(day, month + 1, year)
            dateButton.text = formattedDate
            actDate = "$month $day $year"
        }
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH,1)
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val style = DatePickerDialog.THEME_HOLO_LIGHT

        datePickerDialog = DatePickerDialog(requireContext(), style, dateSetListener, year, month, day)
        datePickerDialog.datePicker.minDate = cal.timeInMillis
    }
    private fun makeDateString(day: Int, month: Int, year: Int): String {
        val monthStr = getMonthFormat(month)
        return "$monthStr $day $year"
    }
    private fun getMonthFormat(month: Int): String {
        return when (month) {
            1 -> "JAN"
            2 -> "FEB"
            3 -> "MAR"
            4 -> "APR"
            5 -> "MAY"
            6 -> "JUN"
            7 -> "JUL"
            8 -> "AUG"
            9 -> "SEP"
            10 -> "OCT"
            11 -> "NOV"
            12 -> "DEC"
            else -> ""
        }
    }
    private fun openTimePicker() {
        val cal = Calendar.getInstance()

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, minute)

                selectedTime = makeTimeString(cal)

                // Update the text of the time button
                timeButton.text = selectedTime
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        )

        timePickerDialog.show()
    }
    private fun makeTimeString(cal: Calendar): String {
        val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        val hour = when {
            hourOfDay == 0 -> 12
            hourOfDay > 12 -> hourOfDay - 12
            else -> hourOfDay
        }

        val amPm = if (hourOfDay < 12) "AM" else "PM"

        return String.format("%02d:%02d %s", hour, minute, amPm)
    }


}
