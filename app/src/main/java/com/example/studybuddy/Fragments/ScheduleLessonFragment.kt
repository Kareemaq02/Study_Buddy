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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
class ScheduleClassFragment : Fragment() {
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private  var selectedTime: String = "Select Time"
    private lateinit var majors: ArrayList<String>
    private lateinit var courseList: ArrayList<Course>
    private lateinit var majorId: String
    private  var actDate: String = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule_lesson, container, false)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Set up the spinners
        val spinnerMajor: Spinner = view.findViewById(R.id.spinnerMajorReq)
        val spinnerCourses: Spinner = view.findViewById(R.id.spinnerCoursesReq)
        val spinnerDuration: Spinner = view.findViewById(R.id.spinnerDuration)
        val spinnerLimit: Spinner = view.findViewById(R.id.spinnerLimit)

        val valuesDuration = listOf(1, 2, 3, 4)
        val adapterDuration = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, valuesDuration)
        spinnerDuration.adapter = adapterDuration

        val valuesLimit =  (1..20).map { it.toString() }
        val adapterLimit = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, valuesLimit)
        spinnerLimit.adapter = adapterLimit

        // Set up the button click listener

        dateButton = view.findViewById(R.id.etDate)
        dateButton.text=getTodayDatePlusOneDay()
        dateButton.setOnClickListener{
            openDatePicker()
        }

        initDatePicker()

        timeButton = view.findViewById(R.id.btnTime)
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

                if (GlobalData.arrangeClassClicked) {
                    val majorArrange = GlobalData.majorArrange

                    // Iterate over the spinner items
                    for (index in 0 until spinnerMajor.count) {
                        val item = spinnerMajor.getItemAtPosition(index) as? String

                        // Check if the item matches courseArrange
                        if (item == majorArrange) {
                            // Set the selected item to courseArrange
                            spinnerMajor.setSelection(index)
                            break
                        }
                    }
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
        val btnScheduleClass: Button = view.findViewById(R.id.btnScheduleClass)
        btnScheduleClass.setOnClickListener {

            //major
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
            val startDate: String = if(actDate=="") {
                reqDate
            } else {
                actDate
            }

            //get place
            val place = view.findViewById<EditText?>(R.id.etPlace).text.toString()

            //get start time
            val startTime = timeButton.text.toString()

            //get lesson description
            val lessonDescription = view.findViewById<EditText>(R.id.lessonDescription).text.toString()

            //logged in User Id
            GlobalData.loggedInUserId

            //get duration
            val lessonDuration = spinnerDuration.selectedItem as Int

            //get status
            val status = "Waiting confirmation"

            //get student limit
            val studentsLimit = spinnerLimit.selectedItem.toString().toInt()




            if(timeButton.text!="Select Time")
            {
                if(place!="")
                {
                    if(lessonDescription!="")
                    {

                        var endDate = ""
                        var endTime = ""
                        val dateFormat2 = SimpleDateFormat("MM dd yyyy", Locale.getDefault())
                        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

                        try {
                            // Parse the start date and time strings into Date objects
                            val startDateObj: Date? = dateFormat2.parse(startDate)
                            val startTimeObj: Date? = timeFormat.parse(startTime)

                            // Create a Calendar instance and set the start date and time
                            val calendar2 = Calendar.getInstance()
                            if (startDateObj != null && startTimeObj != null) {
                                calendar2.time = startDateObj
                                calendar2.set(Calendar.HOUR_OF_DAY, startTimeObj.hours)
                                calendar2.set(Calendar.MINUTE, startTimeObj.minutes)
                            }

                            // Add the lesson duration in hours to the start time
                            calendar2.add(Calendar.HOUR_OF_DAY, lessonDuration )

                            // Get the end date and time
                            val endDateObj = calendar2.time

                            // Format the end date and time into the desired formats
                            endDate = dateFormat2.format(endDateObj)
                            endTime = timeFormat.format(endDateObj)

                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }
                        val arrayList = ArrayList<String>()
                        arrayList.add(GlobalData.loggedInUserId)
                        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                        val requestsRef: DatabaseReference = database.getReference("requests")
                        val newRequestRef: DatabaseReference = requestsRef.push()
                        val request = teachRequest(newRequestRef.key,majorName,courseName,reqTime,reqDate,startTime,startDate,endTime,endDate,place, lessonDuration, GlobalData.loggedInUserId, status,
                            studentsLimit, lessonDescription,0,"Teach request",arrayList)

                        newRequestRef.setValue(request)


                        val context = requireContext() // Replace with your activity or context
                        Toast.makeText(context, "Successfully scheduled", Toast.LENGTH_SHORT).show()

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
                else
                {
                    val alertDialog = AlertDialog.Builder(context)
                        .setTitle("Invalid Place")
                        .setMessage("Place field cannot be empty.")
                        .setPositiveButton("OK", null)
                        .create()

                    alertDialog.show()

                }
            }
            else
            {
                val alertDialog = AlertDialog.Builder(context)
                    .setTitle("Invalid Start Time")
                    .setMessage("Please choose the lesson's time.")
                    .setPositiveButton("OK", null)
                    .create()

                alertDialog.show()

            }





        }

        println(GlobalData.arrangeClassClicked)
        println(GlobalData.courseArrange)
        if (GlobalData.arrangeClassClicked) {
            val courseArrange = GlobalData.courseArrange

            // Iterate over the spinner items
            for (index in 0 until spinnerCourses.count) {
                val item = spinnerCourses.getItemAtPosition(index) as? String

                // Check if the item matches courseArrange
                if (item == courseArrange) {
                    // Set the selected item to courseArrange
                    spinnerCourses.setSelection(index)
                    break
                }
            }
        }
        if(GlobalData.arrangeClassClicked)
        {
            val studentsNumArrange = GlobalData.studentsNumArrange

            // Iterate over the spinner items
            for (index in 0 until spinnerLimit.count) {
                val item = spinnerLimit.getItemAtPosition(index) as? String

                // Check if the item matches courseArrange
                if (item == studentsNumArrange) {
                    // Set the selected item to courseArrange
                    spinnerLimit.setSelection(index)
                    break
                }
            }

        }




    }
    private fun getTodayDatePlusOneDay(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, 1) // Add one day to today's date

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        return makeDateString(day, month, year)
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

    private fun openDatePicker() {
        datePickerDialog.show()
    }
}
