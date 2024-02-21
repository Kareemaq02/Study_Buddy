package com.example.studybuddy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R
import com.example.studybuddy.data.teachRequest
import com.google.firebase.database.*

class ScheduleClassAdapter(private val items: List<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {

        private const val VIEW_TYPE_TEACH_REQUEST = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {

            VIEW_TYPE_TEACH_REQUEST -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_scheduled_tracker_item, parent, false)
                TeachRequestViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {

            is TeachRequestViewHolder -> {
                val teachRequest = item as teachRequest
                holder.bind(teachRequest)
            }
            else -> throw IllegalArgumentException("Invalid view holder")
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return when (item) {
            is teachRequest -> VIEW_TYPE_TEACH_REQUEST
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    // View holder for LearnRequestItem


    // View holder for TeachRequest
    class TeachRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: teachRequest) {


            val majorNameTextView: TextView = itemView.findViewById(R.id.majorNameTrack)
            val courseNameTextView: TextView = itemView.findViewById(R.id.courseNameTrack)
            val statusTextView: TextView = itemView.findViewById(R.id.statusTrack)
            val dateTextView: TextView = itemView.findViewById(R.id.textViewDateTrack)
            val timeTextView: TextView = itemView.findViewById(R.id.textViewTimeTrack)
            val placeTextView: TextView = itemView.findViewById(R.id.textViewPlaceTrack)
            val participantsCountTextView: TextView = itemView.findViewById(R.id.textViewParticipantsCountTrack)
            val completeButton:Button = itemView.findViewById(R.id.buttonComplete)


            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val requestsRef: DatabaseReference = database.getReference("requests")


            // Get a reference to the Firebase Storage instance
            //val usersReference = reference.child("users")

            val userid = item.userId;







            majorNameTextView.text = item.major_Name
            courseNameTextView.text=item.course_Name
            statusTextView.text = item.status
            dateTextView.text = formatDate(item.start_date)
            val time= "${item.start_time} - ${item.end_time}"
            timeTextView.text = time
            placeTextView.text = item.place
            participantsCountTextView.text = item.number_of_students.toString()




            completeButton.setOnClickListener {
                val statusRef = requestsRef.child(item.requestId.toString()).child("status")

                // Retrieve the status value from Firebase
                statusRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val status = dataSnapshot.getValue(String::class.java)

                        // Check if the status is "Accepted"
                        if (status == "Accepted") {
                            // Update the status to "Completed"
                            statusRef.setValue("Completed").addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(itemView.context, "Lesson marked as complete", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(itemView.context, "Failed to mark lesson as complete", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(itemView.context, "Lesson can't be completed. Please check the status.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle error
                        Toast.makeText(itemView.context, "Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }






        }



        private fun formatDate(dateString: String): String {
            val parts = dateString.split(" ")
            val day = parts[0]
            val month = parts[1]
            val year = parts[2]
            return "$month/$day/$year"
        }
    }


}
