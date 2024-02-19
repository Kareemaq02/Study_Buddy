import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import com.example.studybuddy.data.teachRequest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class RequestAdapter : ListAdapter<teachRequest, RequestAdapter.RequestViewHolder>(RequestDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.request_item, parent, false)
        return RequestViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        //this function for each individual view item
        val currentRequest = getItem(position)

        // Check if the date and time have passed
        val isDateTimePassed = isDateTimePassed(currentRequest.end_date, currentRequest.end_time)

        if (isDateTimePassed) {
            // If date and time have passed, change the status to "Rejected"
            val database = FirebaseDatabase.getInstance()
            val childRef = database.reference.child("requests").child(currentRequest.requestId.toString())
            childRef.child("status").setValue("Rejected")
            holder.itemView.visibility = View.GONE
        }
        else if (currentRequest.status == "Waiting confirmation") {
            holder.bind(currentRequest)

        } else {
            holder.itemView.visibility = View.GONE
        }
    }

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val placeTextView: TextView = itemView.findViewById(R.id.placeTextView)
        private val studentLimitTextView: TextView = itemView.findViewById(R.id.studentLimitTextView)
        private val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        private val declineButton: Button = itemView.findViewById(R.id.declineButton)
        private val infoButton: ImageButton = itemView.findViewById(R.id.infoButton)

        fun bind(request: teachRequest) {
            titleTextView.text = request.course_Name
            descriptionTextView.text = "Description:"+ request.lessonDescription
            dateTextView.text ="Date: "+"${request.start_date}" + " - " + "${request.end_date}"
            timeTextView.text ="Time: "+"${request.start_time}" + " - " + "${request.end_time}"
            studentLimitTextView.text = "Student Limit: "+request.studentsLimit.toString()
            placeTextView.text = "Place: "+request.place

            acceptButton.setOnClickListener {
                //when clicked it will change the state to "Accepted"
                val database = FirebaseDatabase.getInstance()
                val childRef = database.reference.child("requests")
                val requestId = request.requestId
                val query = childRef.orderByKey().equalTo(requestId)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (childSnapshot in dataSnapshot.children) {
                                val childRef = childSnapshot.ref
                                childRef.child("status").setValue("Accepted")


                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle any potential errors
                    }
                })
            }

            declineButton.setOnClickListener {
                //when clicked it will change the state to "Declined"
                val database = FirebaseDatabase.getInstance()
                val childRef = database.reference.child("requests")
                val requestId = request.requestId
                val query = childRef.orderByKey().equalTo(requestId)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (childSnapshot in dataSnapshot.children) {
                                val childRef = childSnapshot.ref
                                childRef.child("status").setValue("Rejected")



                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle any potential errors
                    }
                })
            }

            infoButton.setOnClickListener {
                //it gets the info of requester based on his key that have been sent by him in the
                //request to tech activity that the user have
                val database = FirebaseDatabase.getInstance()
                val childRef = database.reference.child("users")
                val requesterkey = request.userId
                val query = childRef.orderByKey().equalTo(requesterkey)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (childSnapshot in dataSnapshot.children) {
                                val email = childSnapshot.child("email").getValue(String::class.java)
                                val major = childSnapshot.child("major").getValue(String::class.java)
                                val numberofstars = childSnapshot.child("numStars").getValue(Double::class.java)
                                val numberofraters = childSnapshot.child("ratersCount").getValue(Double::class.java)

                                //calculating the rating
                                val rating = if (numberofraters != 0.0) {
                                    (numberofstars ?: 0.0) / numberofraters!!
                                } else {
                                    0.0
                                }


                                val message = "Email: $email\nMajor: $major\nRating: $rating"

                                val alertDialog = AlertDialog.Builder(itemView.context)
                                    .setTitle("User Details")
                                    .setMessage(message)
                                    .setPositiveButton("OK") { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    .create()
                                alertDialog.show()
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle any potential errors
                    }
                })
            }

        }
    }
    //chec if the date and time hase passed, it return true if passed
    // Function to check if date and time have passed
    private fun isDateTimePassed(dateString: String, timeString: String): Boolean {
        if (dateString.isBlank() || timeString.isBlank()) {
            // Handle empty or blank date or time strings
            return false
        }

        val dateTimeFormat = SimpleDateFormat("MM dd yyyy hh:mm a", Locale.getDefault())
        val currentDateTime = Calendar.getInstance().time
        val comparisonDateTime = dateTimeFormat.parse("$dateString $timeString")
        return currentDateTime.after(comparisonDateTime)
    }

}


class RequestDiffCallback : DiffUtil.ItemCallback<teachRequest>() {
    //so it does not show the same request twice
    override fun areItemsTheSame(oldItem: teachRequest, newItem: teachRequest): Boolean {

        return oldItem.requestId == newItem.requestId
    }

    override fun areContentsTheSame(oldItem: teachRequest, newItem: teachRequest): Boolean {
        return oldItem == newItem
    }
}
