import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.studybuddy.Activity.UserSemesterCoursesActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.studybuddy.R
class UserAddSemesterCourseDialogFragment : DialogFragment() {
    fun setAdapter(adapter: CourseAdapter, fragment: UserSemesterCoursesActivity) {
        this.adapter = adapter
        this.fragment = fragment
    }
    private lateinit var adapter: CourseAdapter
    private lateinit var database: DatabaseReference
    private lateinit var userEmail: String
    private lateinit var semesterName: String
    private lateinit var fragment: UserSemesterCoursesActivity // Add fragment variable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(com.example.studybuddy.R.layout.dialog_add_semester_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Retrieve the user's email from the global variable
        userEmail = GlobalData.userEmail

        // Retrieve the semester name from the global variable
        semesterName = GlobalData.semesterName

        // Find views
        val acceptButton = view.findViewById<Button>(R.id.acceptButton)
        val editTextCourseName = view.findViewById<EditText>(com.example.studybuddy.R.id.editTextCourseName)
        val editTextLocation = view.findViewById<EditText>(R.id.editTextLocation)
        val editTextTime = view.findViewById<EditText>(R.id.editTextTime)
        val editTextDays = view.findViewById<EditText>(R.id.editTextDays)

        // Set click listener for the accept button
        acceptButton.setOnClickListener {
            // Get course information from EditText fields
            val courseName = editTextCourseName.text.toString().trim()
            val location = editTextLocation.text.toString().trim()
            val time = editTextTime.text.toString().trim()
            val days = editTextDays.text.toString().trim()

            // Add course to the database under the specific semester name
            addCourseToDatabase(userEmail, semesterName, courseName, location, time, days)

            // Dismiss the dialog
            dismiss()
        }
    }

    private fun addCourseToDatabase(userEmail: String, semesterName: String, courseName: String, location: String, time: String, days: String) {
        // Reference to the user's data based on email
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        // Query to find the user with the matching email
        val userQuery = usersRef.orderByChild("email").equalTo(userEmail)

        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    // Find the semester with the specified name
                    val semesterSnapshot = userSnapshot.child("semester").children.find { it.child("name").getValue(String::class.java) == semesterName }

                    semesterSnapshot?.let { semester ->
                        // Construct a reference to the "Courses" node under the found semester
                        val coursesRef = semester.child("Courses").ref

                        // Push a new child node under "Courses" to add the new course
                        val newCourseRef = coursesRef.push()
                        val courseData = mapOf(
                            "name" to courseName,
                            "location" to location,
                            "time" to time,
                            "date" to days
                        )

                        // Set the value of the new course using the generated key
                        newCourseRef.setValue(courseData).addOnSuccessListener {
                            // Semester added successfully, update the list and notify adapter
                            updateCourseList()

                        }.addOnFailureListener {
                            // Handle failure
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occur
            }
        })
    }
    private fun updateCourseList() {
        val userEmail = GlobalData.userEmail
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        val userQuery = usersRef.orderByChild("email").equalTo(userEmail)

        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val courseList = mutableListOf<SemesterCourse>() // Initialize local list

                for (userSnapshot in dataSnapshot.children) {
                    val semesterSnapshot = userSnapshot.child("semester").children.find { it.child("name").getValue(String::class.java) == semesterName }

                    semesterSnapshot?.let { semester ->
                        val coursesSnapshot = semester.child("Courses")

                        for (courseSnapshot in coursesSnapshot.children) {
                            val courseName = courseSnapshot.child("name").getValue(String::class.java)
                            val location = courseSnapshot.child("location").getValue(String::class.java)
                            val time = courseSnapshot.child("time").getValue(String::class.java)
                            val days = courseSnapshot.child("date").getValue(String::class.java)

                            val course = SemesterCourse(courseName.toString(), location.toString(), time.toString(), days.toString())
                            courseList.add(course)
                        }
                    }
                }

                // Call method in fragment to update list
                fragment.updateAdapterData(courseList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

}
