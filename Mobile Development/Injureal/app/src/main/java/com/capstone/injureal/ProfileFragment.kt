package com.capstone.injureal

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val usernameTextView = view.findViewById<TextView>(R.id.username)
        val emailTextView = view.findViewById<TextView>(R.id.email)
        val editProfileButton = view.findViewById<Button>(R.id.btnEditProfile)
        val signOutButton = view.findViewById<Button>(R.id.btnSignOut)

        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "Unknown User")
        val email = sharedPreferences.getString("email", "Unknown Email")
        val profileImageUri = sharedPreferences.getString("profileImageUri", null)

        if (profileImageUri == null) {
            imageView.setImageResource(R.drawable.logo_injureal3)
        } else {
            imageView.setImageURI(Uri.parse(profileImageUri))
        }

        usernameTextView.text = username
        emailTextView.text = email

        editProfileButton.setOnClickListener {
            showEditProfileDialog(sharedPreferences, usernameTextView, emailTextView, imageView)
        }

        signOutButton.setOnClickListener {
            sharedPreferences.edit().clear().apply()
            Toast.makeText(requireContext(), "Successfully exited!", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }

        return view
    }

    private fun showEditProfileDialog(
        sharedPreferences: android.content.SharedPreferences,
        usernameTextView: TextView,
        emailTextView: TextView,
        imageView: ImageView
    ) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.activity_edit_profile, null)
        val usernameEditText = dialogView.findViewById<EditText>(R.id.etUsername)
        val emailEditText = dialogView.findViewById<EditText>(R.id.etEmail)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.etPassword)
        val profileImageView = dialogView.findViewById<ImageView>(R.id.ivProfileImage)
        val chooseImageButton = dialogView.findViewById<Button>(R.id.btnChooseImage)

        usernameEditText.setText(sharedPreferences.getString("username", ""))
        emailEditText.setText(sharedPreferences.getString("email", ""))
        passwordEditText.setText(sharedPreferences.getString("password", ""))

        passwordEditText.inputType = InputType.TYPE_CLASS_TEXT

        val currentImageUri = sharedPreferences.getString("profileImageUri", null)
        if (currentImageUri != null) {
            profileImageView.setImageURI(Uri.parse(currentImageUri))
        } else {
            profileImageView.setImageResource(R.drawable.logo_injureal3)
        }

        chooseImageButton.setOnClickListener {
            openImageChooser()
        }

        val alertDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Edit Profile")
            setView(dialogView)
            setPositiveButton("Save", null)
            setNegativeButton("Cancel", null)
        }.create()

        alertDialog.setOnShowListener {
            val saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val cancelButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            saveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.warna_utama3))
            cancelButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.warna_utama3))

            saveButton.setOnClickListener {
                val newUsername = usernameEditText.text.toString().trim()
                val newEmail = emailEditText.text.toString().trim()
                val newPassword = passwordEditText.text.toString().trim()

                if (newUsername.isNotEmpty() && newEmail.isNotEmpty() && newPassword.isNotEmpty()) {
                    val db = FirebaseFirestore.getInstance()
                    val userId = sharedPreferences.getString("userId", null)

                    if (userId != null) {
                        val userRef = db.collection("users").document(userId)
                        val updatedData = mutableMapOf<String, Any>(
                            "et_name" to newUsername,
                            "et_email" to newEmail,
                            "et_password" to newPassword
                        )

                        selectedImageUri?.let {
                            updatedData["profileImageUri"] = it.toString()
                        }

                        userRef.update(updatedData)
                            .addOnSuccessListener {
                                sharedPreferences.edit().apply {
                                    putString("username", newUsername)
                                    putString("email", newEmail)
                                    putString("password", newPassword)
                                    selectedImageUri?.let {
                                        putString("profileImageUri", it.toString())
                                    }
                                    apply()
                                }
                                usernameTextView.text = newUsername
                                emailTextView.text = newEmail

                                selectedImageUri?.let { imageView.setImageURI(it) }

                                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                alertDialog.dismiss()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }

            cancelButton.setOnClickListener {
                alertDialog.dismiss()
            }
        }

        alertDialog.show()
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            val profileImageView = view?.findViewById<ImageView>(R.id.ivProfileImage)
            profileImageView?.setImageURI(selectedImageUri)
        }
    }
}