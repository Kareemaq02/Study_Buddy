// Inside SignupActivity.kt

package com.example.studybuddy.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.studybuddy.R
import com.example.studybuddy.data.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val signupButton = findViewById<Button>(R.id.signupButton)
        signupButton.setOnClickListener {
            // Call the signup function and insert an example user
            signup()
        }


        // Co


        //code for Spinner
        val spinner = findViewById<Spinner>(R.id.spinner)
        val items = arrayOf("Computer Science", "Computer Engineering")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = items[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }


        // Other signup-related code, such as UI interactions and user input handling
        // ...


    }

    private fun signup() {
        val email = getEmailFromInput() // Replace with the actual email input
        val password = getPasswordFromInput() // Replace with the actual password input
        val confirmPassword = getConfirmPasswordFromInput()
        val selectedSpinnerItem = getSelectedSpinnerItem()
        val firstName = getFirstNameFromInput()
        val lastName = getLastNameFromInput()

        // Check if the email is already registered
        if (isValidEmail(email)) {
            if (password == confirmPassword) {
                // Check if the password meets the criteria
                val hasUppercase = password.any { it.isUpperCase() }
                val hasSpecialChar = password.any { it.isLetterOrDigit().not() }

                if (hasUppercase && hasSpecialChar) {
                    isEmailAlreadyRegistered(email) { isRegistered ->
                        if (isRegistered) {
                            // Email is already registered, display an error message or handle the case
                            // For example, show a Toast message
                            Toast.makeText(this, "Email is already registered", Toast.LENGTH_SHORT).show()
                        } else {
                            // Email is not registered, proceed with the signup process
                            val database = FirebaseDatabase.getInstance()
                            val usersRef = database.getReference("users")
                            val userData = User(email, password, firstName, lastName, selectedSpinnerItem,0.0,0)
                            val newUserRef = usersRef.push()
                            newUserRef.setValue(userData)
                            // Show a success message or navigate to the next screen
                            // For example, show a Toast message and go back to the login screen

                            Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    // Display an error message if the password doesn't meet the criteria
                    val errorMessage = "Password must contain at least one uppercase letter and one special character."
                    val errorDialog = AlertDialog.Builder(this)
                        .setTitle("Invalid Password")
                        .setMessage(errorMessage)
                        .setPositiveButton("OK") { dialog, which ->
                            // Close the dialog
                            dialog.dismiss()
                        }
                        .create()

                    errorDialog.show()
                }
            } else {
                Toast.makeText(this, "Password and confirm password do not match", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show()
        }
    }


    private fun isEmailAlreadyRegistered(email: String, callback: (Boolean) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        val query = usersRef.orderByChild("email").equalTo(email)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // If a user with the given email exists, the email is already registered
                val isRegistered = dataSnapshot.exists()
                callback.invoke(isRegistered)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the database error, if necessary
                // For example, display an error message or log the error
                callback.invoke(false) // Assume email is not registered in case of an error
            }
        })
    }
    fun isValidEmail(email: String): Boolean {
        val pattern = Regex("^[A-Za-z0-9.]+@gju\\.edu\\.jo$")
        return pattern.matches(email)
    }
    private fun getEmailFromInput(): String {
        val emailEditText = findViewById<EditText>(R.id.PersonalEmail)
        return emailEditText.text.toString().trim()
    }

    private fun getPasswordFromInput(): String {
        val passwordEditText = findViewById<EditText>(R.id.PersonalPassword)
        val password = passwordEditText.text.toString()
        val passwordSuggestions = generatePasswordSuggestions()




        // Set the dialog window's gravity to display above the keyboard
        val suggestionsDialog = AlertDialog.Builder(this)
            .setTitle("Password Suggestions")
            .setItems(generatePasswordSuggestions().toTypedArray()) { dialog, which ->
                // Set the selected suggestion as the password
                passwordEditText.setText(passwordSuggestions[which])
            }
            .setNegativeButton("Cancel") { dialog, which ->
                // User clicked cancel, do nothing
            }
            .create()

        // Set the long click listener to show the suggestions dialog
        passwordEditText.setOnLongClickListener {
            // Show the suggestions dialog
            suggestionsDialog.show()
            true // Consume the long click event
        }

        return password
    }


    private fun generatePasswordSuggestions(): List<String> {
        val passwordSuggestions = mutableListOf<String>()

        // Define the set of special characters
        val specialCharacters = listOf('!', '@', '#', '$', '%', '&', '*')

        // Generate password suggestions based on the criteria
        for (i in 0 until 5) { // Generate 5 suggestions
            val uppercaseLetter = ('A'..'Z').random()
            val specialCharacter = specialCharacters.random()
            val lowercaseLetters = ('a'..'z').toList()
            val password = listOf(uppercaseLetter, specialCharacter) +
                    lowercaseLetters.shuffled().take(8) // Random lowercase letters

            passwordSuggestions.add(password.joinToString(""))
        }

        return passwordSuggestions
    }

    private fun getSelectedSpinnerItem(): String {
        val spinner = findViewById<Spinner>(R.id.spinner)
        return spinner.selectedItem as String
    }
    private fun getFirstNameFromInput(): String {
        val firstNameEditText = findViewById<EditText>(R.id.FirstName)
        return firstNameEditText.text.toString().trim()
    }

    private fun getLastNameFromInput(): String {
        val lastNameEditText = findViewById<EditText>(R.id.LastName)
        return lastNameEditText.text.toString().trim()
    }
    private fun getConfirmPasswordFromInput(): String {
        val confirmPasswordEditText = findViewById<EditText>(R.id.ConfirmPersonalPassword)
        return confirmPasswordEditText.text.toString()
    }
   /*
    fun clearTextOnClick(view: View) {
        if (view is EditText) {
            view.text.clear()
        }
    }
    */


    // Other methods and functions related to signup functionality
    // ...


}
