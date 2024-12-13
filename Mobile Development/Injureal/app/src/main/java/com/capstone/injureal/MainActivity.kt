package com.capstone.injureal

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        progressBar = findViewById(R.id.progressBar)

        imageView.visibility = ImageView.VISIBLE
        progressBar.visibility = ProgressBar.GONE

        Handler().postDelayed({
            imageView.visibility = ImageView.GONE
            progressBar.visibility = ProgressBar.VISIBLE
        }, 3000)

        Handler().postDelayed({
            progressBar.visibility = ProgressBar.GONE

            val intent = Intent(this@MainActivity, SignIn::class.java)
            startActivity(intent)
            finish()
        }, 6000)
    }
}
