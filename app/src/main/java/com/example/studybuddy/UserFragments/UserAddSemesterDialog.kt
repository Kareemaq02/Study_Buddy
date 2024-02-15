import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.example.studybuddy.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class UserAddSemesterDialog : DialogFragment() {

    private lateinit var adapter: UserScheduleAdapter // Add adapter variable
    private lateinit var fragment: UserSemestersManagerFragment // Add fragment variable

    // Method to set the adapter
    fun setAdapter(adapter: UserScheduleAdapter, fragment: UserSemestersManagerFragment) {
        this.adapter = adapter
        this.fragment = fragment
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_add_semester_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the EditText, confirmButton, and cancelButton
        val studyPlanNameEditText = view.findViewById<EditText>(R.id.semesterEditText)
        val confirmButton = view.findViewById<Button>(R.id.confirmButton)
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)

        // Find the Spinner
        val semesterTypeSpinner = view.findViewById<Spinner>(R.id.semesterTypeSpinner)

        // Set up options for the spinner
        val semesterOptions = arrayOf("SS", "WS")

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, semesterOptions).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            semesterTypeSpinner.adapter = adapter
        }

        // Set OnClickListener to the confirmButton
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        confirmButton.setOnClickListener {
            val semesterName = studyPlanNameEditText.text.toString()
            val semesterType = semesterTypeSpinner.selectedItem.toString()

            // Check if the entered year is a valid integer, greater than or equal to 2000,
            // and not greater than the current year
            val enteredYear = semesterName.toIntOrNull()
            if (enteredYear != null && enteredYear >= 2000 && enteredYear <= currentYear) {

                addSemesterToDatabase(semesterName, semesterType)
                dismiss() // Dismiss the dialog
            } else {
                // Display an error message to the user indicating that the year is invalid
                studyPlanNameEditText.error = "Invalid year. Please enter a valid year from 2000 to the current year."
            }
        }


        // Set OnClickListener to the cancelButton
        cancelButton.setOnClickListener {
            dismiss() // Dismiss the dialog
        }

    }

    private fun addSemesterToDatabase(semesterName: String, semesterType: String) {
        val userEmail = GlobalData.userEmail

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")
        val fullSemesterName = "$semesterName $semesterType"
        // Query to find the user with the matching email
        val userQuery = usersRef.orderByChild("email").equalTo(userEmail)

        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    // Get the user's ID
                    val userId = userSnapshot.key

                    // Add the new semester data to the database under the "semester" node
                    usersRef.child(userId.toString()).child("semester").push().apply {
                        child("name").setValue(fullSemesterName)
                            .addOnSuccessListener {
                                // Semester added successfully, update the list and notify adapter
                                updateSemesterList()
                            }
                            .addOnFailureListener {
                                // Handle failure
                            }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun updateSemesterList() {
        val userEmail = GlobalData.userEmail
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        val userQuery = usersRef.orderByChild("email").equalTo(userEmail)

        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val semesterInfoList = mutableListOf<String>() // Initialize local list

                for (userSnapshot in dataSnapshot.children) {
                    val semesterDataSnapshot = userSnapshot.child("semester")

                    for (semesterSnapshot in semesterDataSnapshot.children) {
                        val selectedSemester = semesterSnapshot.child("name").value as? String

                        selectedSemester?.let {
                            semesterInfoList.add(it)
                        }
                    }
                }

                fragment.updateAdapterData(semesterInfoList) // Call method in fragment to update list
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }
}
