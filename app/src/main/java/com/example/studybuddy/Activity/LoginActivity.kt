package com.example.studybuddy.Activity

import GlobalData
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.studybuddy.EmailViewModel
import com.example.studybuddy.R
import com.example.studybuddy.auth.AdminAuth
import com.example.studybuddy.auth.UserAuth
import com.example.studybuddy.data.Session
import com.google.firebase.database.*
class LoginActivity : AppCompatActivity() {

    private lateinit var loginButton: Button
    private lateinit var loginButtonAdmin: Button

    private lateinit var emailViewModel: EmailViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //email view model to store and share emial
        emailViewModel = ViewModelProvider(this).get(EmailViewModel::class.java)

        val createAccountTextView = findViewById<TextView>(R.id.createAccountTextView)
        createAccountTextView.paintFlags = createAccountTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        createAccountTextView.setOnClickListener {
            // Handle the click event here
            // For example, navigate to the create account page
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }


        loginButton = findViewById(R.id.loginButton)

        //login button for user
        loginButton.setOnClickListener {


            val emailtext = findViewById<EditText>(R.id.editTextTextPersonName)
            val email = emailtext.text.toString()
            val passwordtext = findViewById<EditText>(R.id.editTextTextPassword)
            val password = passwordtext.text.toString()

            // now we store the email in the view model to share it among all project
            emailViewModel.email = email


            // Validate credentials
            validateCredentials(email, password) { isValid ->
                if (isValid) {
                    // Perform login
                    login(email, password, false)
                    // Send email to Semester Manager Fragment
                } else {
                    // Show error message for invalid credentials
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }

        }

        loginButtonAdmin = findViewById(R.id.loginButtonAdmin)

        loginButtonAdmin.setOnClickListener {
            val emailtext = findViewById<EditText>(R.id.editTextTextPersonName)
            val email = emailtext.text.toString()
            val passwordtext = findViewById<EditText>(R.id.editTextTextPassword)
            val password = passwordtext.text.toString()

            // Validate credentials
            validateCredentialsAdmin(email, password) { isValid ->
                if (isValid) {
                    // Perform login
                    loginAdmin(email, password, true)
                    // Send email to Semester Manager Fragment
                } else {
                    // Show error message for invalid credentials
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

    private fun validateCredentials(email: String, password: String, callback: (Boolean) -> Unit) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            val database = FirebaseDatabase.getInstance().reference
            val usersRef = database.child("users")

            // Query to find the user with the matching email and password
            val userQuery = usersRef.orderByChild("email").equalTo(email)

            userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (userSnapshot in dataSnapshot.children) {
                        val dbPassword = userSnapshot.child("password").getValue(String::class.java)
                        if (dbPassword == password) {
                            // Password matches, invoke callback with true
                            callback(true)
                            return
                        }
                    }
                    // No matching user found or password doesn't match, invoke callback with false
                    callback(false)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle any errors that occur, invoke callback with false
                    callback(false)
                }
            })
        } else {
            // Email or password is empty, invoke callback with false
            callback(false)
        }
    }
    private fun validateCredentialsAdmin(email: String, password: String, callback: (Boolean) -> Unit) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            val database = FirebaseDatabase.getInstance().reference
            val usersRef = database.child("admin")

            // Query to find the user with the matching email and password
            val userQuery = usersRef.orderByChild("email").equalTo(email)

            userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (userSnapshot in dataSnapshot.children) {
                        val dbPassword = userSnapshot.child("password").getValue(String::class.java)
                        if (dbPassword == password) {
                            // Password matches, invoke callback with true
                            callback(true)
                            return
                        }
                    }
                    // No matching user found or password doesn't match, invoke callback with false
                    callback(false)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle any errors that occur, invoke callback with false
                    callback(false)
                }
            })
        } else {
            // Email or password is empty, invoke callback with false
            callback(false)
        }
    }


    private fun login(email: String, password: String, isAdmin: Boolean) {
        // Call the UserAuth class to perform the login process
        UserAuth.login(email, password) { success ->
            if (success) {
                val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                val collectionRef: DatabaseReference = database.getReference("Sessions")
                val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                val query = collectionRef.orderByChild("deviceId").equalTo(deviceId)

                GlobalData.userEmail = email
                println(GlobalData.userEmail)

                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val isDeviceRegistered = dataSnapshot.exists()

                        if (!isDeviceRegistered) {
                            // The device ID is not registered, create a new session
                            val key = collectionRef.push().key
                            val session = Session(email, deviceId, isAdmin)

                            if (key != null) {
                                collectionRef.child(key).setValue(session)
                                    .addOnSuccessListener {
                                    }
                                    .addOnFailureListener { error ->
                                        println("Failed to add session: $error")
                                    }
                            } else {
                                Toast.makeText(this@LoginActivity, "Login failed, please try again later", Toast.LENGTH_SHORT).show()
                            }
                        }

                        // Navigate to the home screen or main app screen
                        val intent = Intent(this@LoginActivity, UserHomePageActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle the database error, if necessary
                        // For example, display an error message or log the error
                    }
                })
            } else {
                // Login failed, display an error message or handle the case
                Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
                print("$password     $email")
            }
        }
    }

    private fun loginAdmin(email: String, password: String, isAdmin: Boolean) {
        // Call the UserAuth class to perform the login process
        AdminAuth.login(email, password) { success ->
            if (success) {
                val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                val collectionRef: DatabaseReference = database.getReference("Sessions")
                val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                val query = collectionRef.orderByChild("deviceId").equalTo(deviceId)

                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val isDeviceRegistered = dataSnapshot.exists()

                        if (!isDeviceRegistered) {
                            // The device ID is not registered, create a new session
                            val key = collectionRef.push().key
                            val session = Session(email, deviceId, isAdmin)

                            if (key != null) {
                                collectionRef.child(key).setValue(session)
                                    .addOnSuccessListener {
                                        Toast.makeText(this@LoginActivity, "Session created successfully", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { error ->
                                        println("Failed to add session: $error")
                                    }
                            } else {
                                Toast.makeText(this@LoginActivity, "Login failed, please try again later", Toast.LENGTH_SHORT).show()
                            }
                        }

                        // Navigate to the home screen or main app screen
                        val intent = Intent(this@LoginActivity, AdminHomepageActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle the database error, if necessary
                        // For example, display an error message or log the error
                    }
                })
            } else {
                // Login failed, display an error message or handle the case
                Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

}