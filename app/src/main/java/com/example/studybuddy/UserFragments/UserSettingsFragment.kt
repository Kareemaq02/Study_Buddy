package com.example.studybuddy.UserFragments

import GlobalData
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.studybuddy.Activity.LoginActivity
import com.example.studybuddy.R
import com.example.studybuddy.R.id.firstNameTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.InputStream



class UserSettingsFragment : Fragment() {

    private lateinit var changePasswordButton: Button
    private lateinit var newPasswordEditText: EditText

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference
    private lateinit var currentUserRef: DatabaseReference
    private lateinit var userFirstName: TextView
    private lateinit var userLastName: TextView
    private lateinit var userEmail: TextView
    private lateinit var email: String
    private lateinit var showFirstName: String
    private lateinit var showLastName: String
    private lateinit var logoutButton: Button
    private lateinit var changeImage: ImageView
    private lateinit var userImageView: ImageView

    private val GALLERY_PERMISSION_REQUEST = 123

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_settings, container, false)

        userFirstName = view.findViewById(firstNameTextView)
        //userLastName = view.findViewById(R.id.lastname)
        userEmail = view.findViewById(R.id.emailTextView)
        changePasswordButton = view.findViewById(R.id.changePasswordButton)

        //newPasswordEditText = view.findViewById(R.id.edit_text_new_password)
        //go to new activity instead of doing it here.
        //changeImage = view.findViewById(R.id.changePhoto)
        //userImageView = view.findViewById(R.id.userImage)

        // logout
        logoutButton = view.findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener { showLogoutDialog() }

        // Initialize Firebase instances
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        usersRef = database.reference.child("users")

        // Get the current user reference using the email
        email = GlobalData.userEmail
        currentUserRef = usersRef.orderByChild("email").equalTo(email).limitToFirst(1).ref

        displayUserInfo()
        loadImageFromFirebaseStorage()


        changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }

        changeImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    GALLERY_PERMISSION_REQUEST
                )
            } else {
                openGallery()
            }
        }

        return view
    }

    private fun loadImageFromFirebaseStorage() {
        // Get a reference to the Firebase Storage instance
        //val usersReference = reference.child("users")

        // Query the users node to find the user with the matching email
        val query = usersRef.orderByChild("email").equalTo(email)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapShot in dataSnapshot.children) {
                    val image = userSnapShot.child("image").getValue(String::class.java)

                    // Load the image into the changeImage ImageView using Glide
                    if (!image.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(image)
                            .apply(RequestOptions.circleCropTransform())
                            .into(userImageView)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun displayUserInfo() {

        val userQuery = usersRef.orderByChild("email").equalTo(email)
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val firstName = userSnapshot.child("firstname").getValue(String::class.java)

                    Log.d("UserSettingsFragment", "First Name: $firstName")

                    userFirstName.text = "Welcome $firstName"
                    userEmail.text = "Email: $email"


                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error here
            }
        })
    }

    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Change Password")
        builder.setMessage("Enter your new password")

        val passwordEditText = EditText(requireContext())
        builder.setView(passwordEditText)

        builder.setPositiveButton("Change") { dialogInterface: DialogInterface, _: Int ->
            val newPassword = passwordEditText.text.toString().trim()

            if (newPassword.isNotEmpty()) {
                // Update the password in Firebase
                editUserPassword(newPassword)
            } else {
                // Show an error message for empty password
                showInvalidPasswordDialog()
            }

            dialogInterface.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun editUserPassword(newPassword: String) {
        currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {

                        // Update the password value for the user
                        userSnapshot.child("password").ref.setValue(newPassword)
                            .addOnSuccessListener {
                                // Password update successful
                                showPasswordChangeSuccessDialog()
                            }
                            .addOnFailureListener { exception ->
                                // Password update failed
                                // Handle the error here
                                showPasswordChangeFailureDialog()
                            }
                    }
                } else {
                    // User not found
                    showUserNotFoundErrorDialog()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the database error here
                showDatabaseErrorDialog()
            }
        })
    }

    private fun showPasswordChangeSuccessDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Password Change Success")
        builder.setMessage("Your password has been changed successfully.")

        builder.setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showPasswordChangeFailureDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Password Change Failed")
        builder.setMessage("Failed to change your password. Please try again.")

        builder.setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showInvalidPasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Invalid Password")
        builder.setMessage("Please enter a valid password.")

        builder.setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showUserNotFoundErrorDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("User Not Found")
        builder.setMessage("Failed to find the user. Please try again.")

        builder.setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showDatabaseErrorDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Database Error")
        builder.setMessage("An error occurred while accessing the database. Please try again.")

        builder.setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Log Out")
        builder.setMessage("Are you sure you want to log out?")
        builder.setPositiveButton("Yes") { dialog, which -> // Perform logout actions and navigate to another page
            performLogout()
        }
        builder.setNegativeButton("No", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun performLogout() {
        val database = FirebaseDatabase.getInstance()
        val sessionRef = database.getReference("Sessions")
        val deviceId =
            Settings.Secure.getString(requireActivity().contentResolver, Settings.Secure.ANDROID_ID)
        val query = sessionRef.orderByChild("deviceId").equalTo(deviceId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (sessionSnapshot in dataSnapshot.children) {
                    sessionSnapshot.ref.removeValue()
                }
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_PERMISSION_REQUEST)
    }

    val userID = GlobalData.loggedInUserId

    private fun uploadImageToFirebase(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = firebaseAuth.currentUser?.uid ?: ""
        val imageName = "user_images_$userID.jpg" // Specify the desired image name
        val imageRef = storageRef.child(imageName)

        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData: ByteArray = baos.toByteArray()

        val uploadTask = imageRef.putBytes(imageData)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Image upload success
            val imageUrlTask = taskSnapshot.storage.downloadUrl
            imageUrlTask.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                saveImageUrlToDatabase(imageUrl)
            }.addOnFailureListener { exception ->
                // Handle the failure to get the image URL
            }
        }.addOnFailureListener { exception ->
            // Image upload failed
            // Handle the failure here
        }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        val userQuery = usersRef.orderByChild("email").equalTo(email)
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    // Save the image URL under the "image" field of the user's node
                    userSnapshot.child("image").ref.setValue(imageUrl)
                        .addOnSuccessListener {
                            // Image URL save successful
                            // Handle the success here
                        }
                        .addOnFailureListener { exception ->
                            // Image URL save failed
                            // Handle the failure here
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error here
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == GALLERY_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Gallery permission denied.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_PERMISSION_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage: Uri? = data.data
            if (selectedImage != null) {
                uploadImageToFirebase(selectedImage)
            }
        }
    }
}
