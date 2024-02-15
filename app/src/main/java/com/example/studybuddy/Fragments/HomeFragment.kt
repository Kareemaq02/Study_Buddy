package com.example.studybuddy.fragments
import RequestAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studybuddy.R
import android.widget.Toast
import com.example.studybuddy.data.teachRequest
import com.google.firebase.database.*

class HomeFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var requestAdapter: RequestAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var requestsRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        requestAdapter = RequestAdapter()
        recyclerView.adapter = requestAdapter
        database = FirebaseDatabase.getInstance()
        requestsRef = database.getReference("requests")

        //createDemoData()

        fetchRequests()

        return view
    }

    private fun fetchRequests() {
        requestsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                //Saving the Data that we get from firebase
                val requests = mutableListOf<teachRequest>()
                for (snapshot in dataSnapshot.children) {
                    val request = snapshot.getValue(teachRequest::class.java)
                    request?.let {
                        if (it.requestType =="Teach request") {
                            it.requestId = snapshot.key // overwrite the requestis so it store the requestkey
                            requests.add(it)
                        }
                    }
                }
                requestAdapter.submitList(requests)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "Error: " + databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }





}
