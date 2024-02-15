package com.example.studybuddy.adapters

import GlobalData
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.studybuddy.Activity.UserMakeRequestActivity
import com.example.studybuddy.R
import com.example.studybuddy.data.learnRequest
import com.example.studybuddy.data.teachRequest
import com.google.firebase.database.*
import kotlin.math.roundToInt
class RequestsAdapter(private val items: List<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {

        private const val VIEW_TYPE_LEARN_REQUEST = 0
        private const val VIEW_TYPE_TEACH_REQUEST = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LEARN_REQUEST -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_learn_request, parent, false)
                LearnRequestViewHolder(view)
            }
            VIEW_TYPE_TEACH_REQUEST -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_teach_request, parent, false)
                TeachRequestViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is LearnRequestViewHolder -> {
                val learnRequest = item as learnRequest
                holder.bind(learnRequest)
            }
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
            is learnRequest -> VIEW_TYPE_LEARN_REQUEST
            is teachRequest -> VIEW_TYPE_TEACH_REQUEST
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    // View holder for LearnRequestItem
    class LearnRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: learnRequest) {
            val firstNameTextView: TextView = itemView.findViewById(R.id.textViewFirstName2)
            val lastNameTextView: TextView = itemView.findViewById(R.id.textViewLastName2)
            val majorTextView: TextView = itemView.findViewById(R.id.textViewMajor2)
            val courseTextView: TextView = itemView.findViewById(R.id.textViewCourseName2)
            val preferredTimeTextView: TextView = itemView.findViewById(R.id.textViewPreferredTime)
            val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDescription2)
            val upVotesCountTextView: TextView = itemView.findViewById(R.id.textViewUpvotesCount)

            val userKey = item.userId


            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val usersRef: DatabaseReference = database.getReference("users")


            val userFirstNameRef: DatabaseReference = usersRef.child(userKey).child("firstname")
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


            val userLastNameRef: DatabaseReference = usersRef.child(userKey).child("lastname")
            userLastNameRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val userLastName: String? = dataSnapshot.getValue(String::class.java)


                    if (userLastName != null) {
                        lastNameTextView.text = userLastName
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })








            majorTextView.text = item.major_Name
            courseTextView.text = item.course_Name
            preferredTimeTextView.text = item.pref_time
            descriptionTextView.text = item.requestDescription
            upVotesCountTextView.text = item.upvoters.toString()
            val upvoteButton: ImageButton = itemView.findViewById(R.id.buttonUpvote)

            val learnRequestRef: DatabaseReference = database.getReference("requests").child(item.requestId.toString())

            learnRequestRef.get().addOnSuccessListener { dataSnapshot ->
                val learnRequest: learnRequest? = dataSnapshot.getValue(learnRequest::class.java)

                if (learnRequest != null) {
                    val upvotersList: ArrayList<String> = learnRequest.upVotersIds
                    val currentUserID = GlobalData.loggedInUserId
                    val isSelected = upvotersList.contains(currentUserID)
                    upvoteButton.isSelected = isSelected
                }
            }.addOnFailureListener { exception ->
                // Handle the failure to retrieve data from the database
            }


            upvoteButton.setOnClickListener {
                if(upvoteButton.isSelected)
                {
                    upvoteButton.isSelected=false
                    val count :Int = upVotesCountTextView.text.toString().toInt()-1
                    upVotesCountTextView.text = count.toString()


                }
                else if(!upvoteButton.isSelected)
                {
                    upvoteButton.isSelected=true
                    val count :Int = upVotesCountTextView.text.toString().toInt()+1
                    upVotesCountTextView.text = count.toString()


                }
            }


            val arrangeButton: Button = itemView.findViewById(R.id.buttonArrangeClass)

            arrangeButton.setOnClickListener{
                GlobalData.arrangeClassClicked = true
                GlobalData.courseArrange = courseTextView.text.toString()
                GlobalData.majorArrange = majorTextView.text.toString()
                GlobalData.studentsNumArrange = upVotesCountTextView.text.toString()

                val intent = Intent(itemView.context, UserMakeRequestActivity::class.java)
                itemView.context.startActivity(intent)



            }






        }


    }

    // View holder for TeachRequest
    class TeachRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: teachRequest) {
            loadImageFromFirebaseStorage(item)
            val firstNameTextView: TextView = itemView.findViewById(R.id.textViewFirstName)
            val lastNameTextView: TextView = itemView.findViewById(R.id.textViewLastName)
            val majorTextView: TextView = itemView.findViewById(R.id.textViewMajor)
            val courseTextView: TextView = itemView.findViewById(R.id.textViewCourseName)
            val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDescription)
            val dateTextView: TextView = itemView.findViewById(R.id.textViewDate)
            val timeTextView: TextView = itemView.findViewById(R.id.textViewTime)
            val placeTextView: TextView = itemView.findViewById(R.id.textViewPlace)
            val participantsCountTextView: TextView = itemView.findViewById(R.id.textViewParticipantsCount)
            val userKey = item.userId
            val image : ImageView = itemView.findViewById(R.id.imageViewReq)
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val usersRef: DatabaseReference = database.getReference("users")
            var ratersCount : Int = 0
            var numStars: Double = 0.0

            // Get a reference to the Firebase Storage instance
            //val usersReference = reference.child("users")

            val userid = item.userId;

            // Query the users node to find the user with the matching email
            val query = usersRef.orderByChild("email").equalTo(GlobalData.userEmail)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (userSnapShot in dataSnapshot.children) {
                        val image2 = userSnapShot.child("image").getValue(String::class.java)

                        // Load the image into the changeImage ImageView using Glide
                        if (!image2.isNullOrEmpty()) {
                            Glide.with(itemView.context)
                                .load(image2)
                                .apply(RequestOptions.circleCropTransform())
                                .into(image)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
            val userFirstNameRef: DatabaseReference = usersRef.child(userKey).child("firstname")
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


            val userLastNameRef: DatabaseReference = usersRef.child(userKey).child("lastname")
            userLastNameRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val userLastName: String? = dataSnapshot.getValue(String::class.java)


                    if (userLastName != null) {
                        lastNameTextView.text = userLastName
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
            val userRatersRef: DatabaseReference = usersRef.child(userKey).child("ratersCount")
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
            val userStarsRef: DatabaseReference = usersRef.child(userKey).child("numStars")
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
                    val ratingBar = itemView.findViewById<RatingBar>(R.id.ratingBar)
                    ratingBar.rating = rating.toFloat()
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })


            println(ratersCount)
            println(numStars)
            val rating: Float = if (ratersCount != 0) {
                val ratingValue = numStars.toDouble() / ratersCount.toDouble()
                roundToHalf(ratingValue).toFloat()
            } else {
                0.0f
            }
            println(rating)
            val ratingBar = itemView.findViewById<RatingBar>(R.id.ratingBar)
            ratingBar.rating=rating.toFloat()


            majorTextView.text = item.major_Name
            courseTextView.text = item.course_Name
            descriptionTextView.text = item.lessonDescription
            dateTextView.text = formatDate(item.start_date)
            val time= "${item.start_time} - ${item.end_time}"
            timeTextView.text = time
            placeTextView.text = item.place
            participantsCountTextView.text = item.number_of_students.toString()


           val participateButton: Button = itemView.findViewById(R.id.buttonParticipate)

            participateButton.setOnClickListener {
                val requestRef: DatabaseReference = database.getReference("requests").child(item.requestId.toString())
                val currentUserId = GlobalData.loggedInUserId

                requestRef.child("participantsIds").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val participantsList: MutableList<String> = if (dataSnapshot.exists()) {
                            dataSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {})
                                ?: mutableListOf()
                        } else {
                            mutableListOf()
                        }

                        if (item.number_of_students != item.studentsLimit) {
                            // Check if the current user is already in the participants list
                            if (!participantsList.contains(currentUserId)) {
                                // Add the current user to the participants list
                                participantsList.add(currentUserId)

                                // Update the participants list in the database
                                requestRef.child("participantsIds").setValue(participantsList)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val participantsCount: Int = item.number_of_students + 1
                                            requestRef.child("number_of_students").setValue(participantsCount)
                                            participantsCountTextView.text = "$participantsCount"
                                           val partReqRef= usersRef.child(GlobalData.loggedInUserId).child("participatedRequests")
                                            val newParticipantReq = partReqRef.push()
                                            newParticipantReq.setValue(item.requestId)
                                            Toast.makeText(itemView.context, "You have successfully participated", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(itemView.context, "Failed to participate in the request", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(itemView.context, "You are already a participant in this class", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(itemView.context, "Class has already reached participants limit", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle database error
                    }
                })


            }






        }

        private fun loadImageFromFirebaseStorage(item: teachRequest) {
            var posterImage: ImageView = itemView.findViewById(R.id.imageViewReq)
            val database = FirebaseDatabase.getInstance()
            val posterUser = item.userId
            // Get a reference to the Firebase Storage instance
            //val usersReference = reference.child("users")
            val usersRef = database.reference.child("users")
            val email = GlobalData.userEmail
            // Query the users node to find the user with the matching email
            val query = usersRef.orderByKey().equalTo(posterUser)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (userSnapShot in dataSnapshot.children) {
                        val image = userSnapShot.child("image").getValue(String::class.java)

                        // Load the image into the changeImage ImageView using Glide
                        if (!image.isNullOrEmpty()) {
                            Glide.with(itemView.context)
                                .load(image)
                                .apply(RequestOptions.circleCropTransform())
                                .into(posterImage)
                        }
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
