package com.example.studybuddy.fragments


import GlobalData
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R
import com.example.studybuddy.adapters.RegisterClassAdapter
import com.example.studybuddy.adapters.ScheduleClassAdapter
import com.example.studybuddy.data.teachRequest
import com.google.firebase.database.*

class registredLessonsTrackerFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    private lateinit var database: FirebaseDatabase
    private lateinit var requestsRef: DatabaseReference
    private lateinit var participationRef: DatabaseReference
    private lateinit var adapter: ScheduleClassAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 1: Set up Firebase
        database = FirebaseDatabase.getInstance()
        requestsRef = database.reference.child("requests")
        participationRef= database.reference.child("users").child(GlobalData.loggedInUserId).child("participatedRequests")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.registred_lessons, container, false)

        // Get reference to the RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewTrack3)

        // Set layout manager to the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        participationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requestIds: MutableList<String> = mutableListOf()

                for (childSnapshot in snapshot.children) {
                    val requestId = childSnapshot.getValue(String::class.java)
                    requestId?.let {
                        requestIds.add(it)
                    }
                }

                val requestsRef = database.reference.child("requests")
                requestsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(requestSnapshot: DataSnapshot) {
                        val requests: MutableList<Any> = mutableListOf()

                        for (requestId in requestIds) {
                            val requestSnapshotId = requestSnapshot.child(requestId)
                            val requestType = requestSnapshotId.child("requestType").getValue(String::class.java)

                            when (requestType) {
                                "Teach request" -> {
                                    val teachRequest = requestSnapshotId.getValue(teachRequest::class.java)
                                    val status = teachRequest?.status
                                    if (teachRequest != null) {
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
                                is teachRequest -> request.req_date // Assuming TeachRequest has a 'publishDate' property
                                else -> throw IllegalArgumentException("Invalid request type")
                            }
                        }

                        // Step 5: Pass the sorted list of requests to the adapter
                        val adapter = RegisterClassAdapter(sortedRequests)
                        recyclerView.adapter = adapter
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle database retrieval error
                    }
                })
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