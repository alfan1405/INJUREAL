package com.capstone.injureal

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore

class SignIn : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val nameEditText = findViewById<EditText>(R.id.et_name_sign_in)
        val passwordEditText = findViewById<EditText>(R.id.et_password_sign_in)
        val togglePasswordImageView = findViewById<ImageView>(R.id.iv_toggle_password_sign_in)
        val signUpTextView = findViewById<TextView>(R.id.tv_sign_up)
        val signInButton = findViewById<Button>(R.id.btn_sign_in)
        progressBar = findViewById(R.id.progressBar)

        firestore = FirebaseFirestore.getInstance()

        togglePasswordImageView.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                togglePasswordImageView.setColorFilter(
                    ContextCompat.getColor(this, R.color.warna_utama2),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                togglePasswordImageView.setColorFilter(
                    ContextCompat.getColor(this, R.color.gray_color),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        signUpTextView.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        signInButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (name.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both name and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressBar.visibility = View.VISIBLE
            signInUser(name, password)
        }
    }

    private fun signInUser(name: String, password: String) {
        val usersCollection = firestore.collection("users")

        usersCollection.whereEqualTo("et_name", name)
            .get()
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    val document = task.result?.documents?.firstOrNull()
                    if (document != null) {
                        val storedPassword = document.getString("et_password")
                        if (storedPassword == password) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("username", document.getString("et_name"))
                            editor.putString("email", document.getString("et_email"))
                            editor.putString("userId", document.id)
                            editor.apply()

                            val intent = Intent(this, MainActivity2::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(this, "No user found with this name", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error checking user", Toast.LENGTH_SHORT).show()
                }
            }
    }
}