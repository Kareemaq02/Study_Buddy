package com.example.studybuddy.adapters

import GlobalData
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.studybuddy.R
import com.example.studybuddy.data.teachRequest
import com.google.firebase.database.*
import kotlin.math.roundToInt

class RegisterClassAdapter(private val items: List<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_TEACH_REQUEST = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TEACH_REQUEST -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_registred_lessons, parent, false)
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

    // View holder for TeachRequest
    class TeachRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: teachRequest) {


            val majorNameTextView: TextView = itemView.findViewById(R.id.majorNameTrack2)
            val courseNameTextView: TextView = itemView.findViewById(R.id.courseNameTrack2)
            val dateTextView: TextView = itemView.findViewById(R.id.textViewDateTrack2)
            val timeTextView: TextView = itemView.findViewById(R.id.textViewTimeTrack2)
            val placeTextView: TextView = itemView.findViewById(R.id.textViewPlaceTrack2)
            val participantsCountTextView: TextView = itemView.findViewById(R.id.textViewParticipantsCountTrack2)
            val completeButton: Button = itemView.findViewById(R.id.buttonComplete2)
            val firstNameTextView: TextView = itemView.findViewById(R.id.FirstNameTrack)
            val lastNameTextView: TextView = itemView.findViewById(R.id.LastNameTrack)

            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val requestsRef: DatabaseReference = database.getReference("requests")
            var ratersCount: Int = 0
            var numStars: Double = 0.0

            val userid = item.userId
            val usersRef: DatabaseReference = database.getReference("users")
            var userFirstName2: String = ""

            majorNameTextView.text = item.major_Name
            courseNameTextView.text = item.course_Name
            dateTextView.text = formatDate(item.start_date)
            val time = "${item.start_time} - ${item.end_time}"
            timeTextView.text = time
            placeTextView.text = item.place
            participantsCountTextView.text = item.number_of_students.toString()
            val userFirstNameRef: DatabaseReference = usersRef.child(item.userId).child("firstname")
            userFirstNameRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val userFirstName: String? = dataSnapshot.getValue(String::class.java)


                    if (userFirstName != null) {
                        firstNameTextView.text = userFirstName
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })

            val userLastNameRef: DatabaseReference = usersRef.child(item.userId).child("lastname")
            userLastNameRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val userLastName: String? = dataSnapshot.getValue(String::class.java)


                    if (userLastName != null) {
                        lastNameTextView.text = userLastName
                        println(userLastName)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })

            completeButton.setOnClickListener {
                val dialog = Dialog(itemView.context)
                dialog.setContentView(R.layout.dialog_rating)
                val firstNameEditText = dialog.findViewById<TextView>(R.id.editTextFirstName3)
                val lastNameEditText = dialog.findViewById<TextView>(R.id.editTextLastName3)
                val ratingBar = dialog.findViewById<RatingBar>(R.id.ratingBar2)
                val submitButton = dialog.findViewById<Button>(R.id.buttonSubmit3)

                // Set initial values
                firstNameEditText.text = firstNameTextView.text
                lastNameEditText.text = lastNameTextView.text

                val userRatersRef: DatabaseReference = usersRef.child(item.userId).child("ratersCount")
                userRatersRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val raters: Int? = dataSnapshot.getValue(Int::class.java)


                        if (raters != null) {
                            println(raters)
                            ratersCount=raters
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
                val userStarsRef: DatabaseReference = usersRef.child(item.userId).child("numStars")
                userStarsRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val starsCount: Double? = dataSnapshot.getValue(Double::class.java)

                        if (starsCount != null) {
                            numStars = starsCount
                        }

                        // Calculate the rating and update the UI
                        val rating: Float = if (ratersCount != 0) {
                            val ratingValue = numStars.toDouble() / ratersCount.toDouble()
                            roundToHalf(ratingValue).toFloat()
                        } else {
                            0.0f
                        }
                        println(rating)

                       val ratingTextView: TextView= dialog.findViewById<TextView>(R.id.TextRating3)
                        ratingTextView.text = rating.toString()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
              dialog.show()

                // Set a click listener for the submit button
                // Set a click listener for the submit button
                submitButton.setOnClickListener {
                    val firstName = firstNameEditText.text.toString()
                    val lastName = lastNameEditText.text.toString()
                    val rating = ratingBar.rating

                    // Retrieve the current values from the database
                    val userStarsRef = usersRef.child(item.userId).child("numStars")
                    val userRatersCountRef = usersRef.child(item.userId).child("ratersCount")

                    userStarsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val currentStars: Double = dataSnapshot.getValue(Double::class.java) ?: 0.0
                            val newStars = currentStars + rating

                            // Update the numStars value in the database
                            userStarsRef.setValue(newStars)

                            // Increment the ratersCount value in the database
                            userRatersCountRef.runTransaction(object : Transaction.Handler {
                                override fun doTransaction(currentData: MutableData): Transaction.Result {
                                    val currentCount = currentData.getValue(Int::class.java) ?: 0
                                    currentData.value = currentCount + 1
                                    return Transaction.success(currentData)
                                }

                                override fun onComplete(
                                    databaseError: DatabaseError?,
                                    committed: Boolean,
                                    currentData: DataSnapshot?
                                ) {
                                    // Handle transaction completion if needed
                                }
                            })
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle error
                        }
                    })




                    val participatedRequestsRef = usersRef.child(GlobalData.loggedInUserId).child("participatedRequests")
                    val requestIdToDelete = item.requestId.toString()
                    println("Participated Request ID: $requestIdToDelete")
                    participatedRequestsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (childSnapshot in dataSnapshot.children) {
                                val key = childSnapshot.key
                                val requestId = childSnapshot.getValue(String::class.java)
                                if (requestId == requestIdToDelete && key != null) {
                                    participatedRequestsRef.child(key).removeValue()
                                        .addOnSuccessListener {
                                            // Deletion successful
                                        }
                                        .addOnFailureListener { exception ->
                                            // Handle deletion failure
                                        }
                                    break  // Exit the loop after deleting the node
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle the query cancellation or error
                        }
                    })




                    dialog.dismiss() // Close the dialog
                }

            }
        }

        private fun loadImageFromFirebaseStorage(item: teachRequest) {

            val database = FirebaseDatabase.getInstance()
            val posterUser = item.userId
            val usersRef = database.reference.child("users")
            val email = GlobalData.userEmail

            // Query the users node to find the user with the matching email
            val query = usersRef.orderByKey().equalTo(posterUser)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (userSnapShot in dataSnapshot.children) {
                        val image = userSnapShot.child("image").getValue(String::class.java)


                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
        }

        private fun roundToHalf(value: Double): Double {
            return (value * 2).roundToInt() / 2.0
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
