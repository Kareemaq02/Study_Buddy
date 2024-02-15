package com.example.studybuddy.auth
import GlobalData
import android.content.ContentResolver
import android.provider.Settings
import com.google.firebase.database.*


object UserAuth {


    fun isLoggedIn(contentResolver: ContentResolver, callback: (Boolean) -> Unit) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val collectionRef: DatabaseReference = database.getReference("Sessions")
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val query = collectionRef.orderByChild("deviceId").equalTo(deviceId)

        /*
            Get email from database
        */

        // check if the user is logged in
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var isLoggedIn = dataSnapshot.exists()
                var email: String? = null
                var loggedInUserId: String? = null // Variable to store the user's key

                for (snapshot in dataSnapshot.children) {
                    email = snapshot.child("email").getValue(String::class.java)
                    val isAdmin = snapshot.child("admin").getValue(Boolean::class.java)

                    if (isAdmin == true) {
                        isLoggedIn = false
                        break
                    }
                }

                if (email != null) {
                    GlobalData.userEmail = email

                    // Retrieve the user's key from the "Users" table
                    val usersRef = database.getReference("users")
                    val userQuery = usersRef.orderByChild("email").equalTo(email)

                    userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            if (userSnapshot.exists()) {
                                for (user in userSnapshot.children) {
                                    loggedInUserId = user.key.toString() // Retrieve the user's key
                                    break
                                }
                            }

                            if (loggedInUserId != null) {
                                GlobalData.loggedInUserId = loggedInUserId as String
                            }

                            callback.invoke(isLoggedIn)
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle the database error, if necessary
                            callback.invoke(false)
                        }
                    })
                } else {
                    callback.invoke(isLoggedIn)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the database error, if necessary
                callback.invoke(false)
            }
        })
    }




    fun login(email: String, password: String, callback: (Boolean) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val userEmail = childSnapshot.child("email").getValue(String::class.java)
                        val userPassword = childSnapshot.child("password").getValue(String::class.java)

                        if (userPassword.equals(password)&&userEmail.equals(email)) {
                            GlobalData.loggedInUserId = childSnapshot.key.toString()
                            // Match found: email and password are correct
                            callback.invoke(true)
                            return
                        }
                    }
                } else {
                    println("No records found for email: $email")
                    callback.invoke(false)
                }

                // No match found: login failed

            }


            override fun onCancelled(databaseError: DatabaseError) {
                // Error occurred while querying the database
                // Handle the error here
                println("Database error: $databaseError")
                callback.invoke(false)
            }
        })
    }
}
