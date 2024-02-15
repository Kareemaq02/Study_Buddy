package com.example.studybuddy.auth
import android.content.ContentResolver
import android.provider.Settings
import com.google.firebase.database.*

object AdminAuth {



    fun isLoggedInAdmin(contentResolver: ContentResolver, callback: (Boolean) -> Unit) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val collectionRef: DatabaseReference = database.getReference("Sessions")
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val query = collectionRef.orderByChild("deviceId").equalTo(deviceId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var isLoggedIn = dataSnapshot.exists()
                for (snapshot in dataSnapshot.children) {
                    val isAdmin = snapshot.child("admin").getValue(Boolean::class.java)
                    println(isAdmin)
                    if (isAdmin == false) {
                        isLoggedIn = false
                        break
                    }
                }
                callback.invoke(isLoggedIn)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the database error, if necessary
                callback.invoke(false)
            }
        })
    }


    fun login(email: String, password: String, callback: (Boolean) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("admin")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val adminEmail = childSnapshot.child("email").getValue(String::class.java)
                        val adminPassword = childSnapshot.child("password").getValue(String::class.java)

                        if (adminPassword.equals(password)&&adminEmail.equals(email)) {
                            // Match found: email and password are correct
                            callback.invoke(true)
                            return
                        }
                    }
                } else {
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



    fun logout() {
        // Clear the loggedInUser to indicate that the user is logged out

    }
}
