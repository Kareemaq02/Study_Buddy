import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.Activity.UserSemesterCoursesActivity
import com.example.studybuddy.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class UserSemestersManagerFragment : Fragment(), UserScheduleAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserScheduleAdapter
    private val semesterInfoList = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and adapter
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2) // 2 columns

        adapter = UserScheduleAdapter(semesterInfoList, this) // Pass the fragment itself as the listener
        recyclerView.adapter = adapter

        // Get the currently logged-in user's email from GlobalData
        val userEmail = GlobalData.userEmail

        // Reference to the Firebase database
        val database = FirebaseDatabase.getInstance()

        // Reference to the "users" node in the database
        val usersRef = database.getReference("users")

        // Query to find the user with the matching email
        val userQuery = usersRef.orderByChild("email").equalTo(userEmail)

        // Retrieve user's semesters from the database
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Clear existing data
                semesterInfoList.clear()

                // Iterate over user data
                for (userSnapshot in dataSnapshot.children) {
                    // Get semesters for the user
                    val semesterDataSnapshot = userSnapshot.child("semester")

                    // Iterate over semesters
                    for (semesterSnapshot in semesterDataSnapshot.children) {
                        // Extract semester details
                        val selectedSemester = semesterSnapshot.child("name").getValue(String::class.java)

                        // Add the semester to the list
                        selectedSemester?.let {
                            semesterInfoList.add(it)
                        }
                    }
                }

                // Notify adapter about data change
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })

        // Find the FloatingActionButton
        val fab: FloatingActionButton = view.findViewById(R.id.addMajorButton)

        // Set OnClickListener to the floating action button
        fab.setOnClickListener {
            // Show your custom dialog here
            showCustomDialog()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_semesters_manager, container, false)
    }

    override fun onItemClick(semester: String) {
        val intent = Intent(requireContext(), UserSemesterCoursesActivity::class.java)
        intent.putExtra("semesterName", semester)
        startActivity(intent)
    }


    private fun showCustomDialog() {
        val dialog = UserAddSemesterDialog()
        dialog.setAdapter(adapter, this) // Pass adapter and fragment reference
        dialog.show(parentFragmentManager, "UserAddSemesterDialog")
    }
    fun updateAdapterData(newData: List<String>) {
        semesterInfoList.clear()
        semesterInfoList.addAll(newData)
        adapter.notifyDataSetChanged()
    }
}
