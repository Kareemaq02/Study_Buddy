package com.example.studybuddy.UserFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R
import com.example.studybuddy.adapters.RequestsAdapter
import com.example.studybuddy.data.learnRequest
import com.example.studybuddy.data.teachRequest
import com.google.firebase.database.*


class UserRequestFragment : Fragment() {
    // ...

    private lateinit var database: FirebaseDatabase
    private lateinit var requestsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 1: Set up Firebase
        database = FirebaseDatabase.getInstance()
        requestsRef = database.reference.child("requests")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_request, container, false)

        // Get reference to the RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewRequests)

        // Set layout manager to the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        requestsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests: MutableList<Any> = mutableListOf()

                for (childSnapshot in snapshot.children) {
                    val requestType = childSnapshot.child("requestType").getValue(String::class.java)

                    when (requestType) {
                        "Learn request" -> {
                            val learnRequest = childSnapshot.getValue(learnRequest::class.java)
                            if (learnRequest != null) {
                                requests.add(learnRequest)
                            }
                        }
                        "Teach request" -> {
                            val teachRequest = childSnapshot.getValue(teachRequest::class.java)
                            val status = teachRequest?.status
                            if (teachRequest != null && status == "Accepted") {
                                requests.add(teachRequest)
                            }
                        }
                        else -> {
                            // Handle invalid request type
                        }
                    }
                }

                // Step 4: Sort the data by publish date
                val sortedRequests = requests.sortedByDescending { request ->
                    when (request) {
                        is learnRequest -> request.req_date // Assuming LearnRequest has a 'publishDate' property
                        is teachRequest -> request.req_date // Assuming TeachRequest has a 'publishDate' property
                        else -> throw IllegalArgumentException("Invalid request type")
                    }
                }//.reversed()

                // Step 5: Pass the sorted list of requests to the adapter
                val adapter = RequestsAdapter(sortedRequests)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database retrieval error
            }
        })



        // ...

        return view
    }

    // ...
}