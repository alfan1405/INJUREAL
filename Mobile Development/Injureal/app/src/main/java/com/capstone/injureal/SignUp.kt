package com.capstone.injureal

import android.content.Intent
import android.os.Bundle
import android.os.Handler
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

class SignUp : AppCompatActivity() {
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val nameEditText = findViewById<EditText>(R.id.et_name)
        val emailEditText = findViewById<EditText>(R.id.et_email)
        val passwordEditText = findViewById<EditText>(R.id.et_password)
        val togglePasswordImageView = findViewById<ImageView>(R.id.iv_toggle_password)
        val createAccountButton = findViewById<Button>(R.id.btn_create_account)
        val signInText = findViewById<TextView>(R.id.tv_sign_in)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val firestore = FirebaseFirestore.getInstance()
        val usersCollection = firestore.collection("users")

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

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

        createAccountButton.setOnClickListener {
            val isAccountCreated = sharedPreferences.getBoolean("isAccountCreated", false)
            if (isAccountCreated) {
                Toast.makeText(this, "An account has already been created on this device!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val et_name = nameEditText.text.toString().trim()
            val et_email = emailEditText.text.toString().trim()
            val et_password = passwordEditText.text.toString().trim()

            if (et_name.isEmpty() || et_email.isEmpty() || et_password.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = User(et_name, et_email, et_password)

            progressBar.visibility = View.VISIBLE

            usersCollection.add(user)
                .addOnCompleteListener { task ->
                    progressBar.visibility = View.GONE

                    if (task.isSuccessful) {
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("isAccountCreated", true)
                        editor.apply()

                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()

                        Handler().postDelayed({
                            val intent = Intent(this, SignIn::class.java)
                            startActivity(intent)
                            finish()
                        }, 3000)
                    } else {
                        Toast.makeText(this, "Failed to create account. Try again!", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        signInText.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }

        val rootView = findViewById<View>(R.id.root_view)
        rootView?.setOnApplyWindowInsetsListener { _, insets -> insets }
    }

    data class User(
        val et_name: String = "",
        val et_email: String = "",
        val et_password: String = ""
    )
}