package com.example.studybuddy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.studybuddy.Activity.AdminHomepageActivity
import com.example.studybuddy.Activity.LoginActivity
import com.example.studybuddy.Activity.UserHomePageActivity
import com.example.studybuddy.auth.AdminAuth
import com.example.studybuddy.auth.UserAuth
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Register for FCM token
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    // Save the token to your server or use it to send notifications
                } else {
                    // Handle the error
                }
            }


        // Check if the user is logged in
        UserAuth.isLoggedIn(contentResolver) { isLoggedIn ->
            if (isLoggedIn) {
                // User is already logged in, navigate to the home screen or main app screen
                navigateToHome()
            } else {
                AdminAuth.isLoggedInAdmin(contentResolver) { isLoggedInAdmin ->
                    if (isLoggedInAdmin) {
                        // User is already logged in, navigate to the home screen or main app screen
                        navigateToHomeAdmin()
                    } else {
                        // User is not logged in, navigate to the login screen
                        navigateToLogin()
                    }
                }
                // User is not logged in, navigate to the login screen

            }
        }

    }

    private fun navigateToHome() {
        // TODO: Implement navigation to the home screen or main app screen
        val intent = Intent(this, UserHomePageActivity::class.java)
        startActivity(intent)
        finish()

    }
    private fun navigateToHomeAdmin() {
        // TODO: Implement navigation to the home screen or main app screen
        val intent = Intent(this, AdminHomepageActivity::class.java)
        startActivity(intent)
        finish()

    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    //test
}
