package com.capstone.injureal

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.capstone.injureal.api.ApiResponse
import com.capstone.injureal.api.RetrofitClient
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream

class HomeFragment : Fragment() {

    private val IMAGE_PICK_CODE = 1000
    private val CAMERA_REQUEST_CODE = 1001
    private val PERMISSION_REQUEST_CODE = 1002

    private lateinit var previewImageView: ImageView
    private lateinit var progressIndicator: LinearProgressIndicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        if (!hasPermissions()) {
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CODE
            )
        }

        previewImageView = view.findViewById(R.id.previewImageView)
        progressIndicator = view.findViewById(R.id.progressIndicator)

        val galleryButton: Button = view.findViewById(R.id.galleryButton)
        galleryButton.setOnClickListener { openGallery() }

        val cameraButton: Button = view.findViewById(R.id.cameraButton)
        cameraButton.setOnClickListener { openCamera() }

        val analyzeButton: Button = view.findViewById(R.id.analyzeButton)
        analyzeButton.setOnClickListener { analyzeImage() }

        return view
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun analyzeImage() {
        progressIndicator.visibility = View.VISIBLE
        val bitmap = (previewImageView.drawable as? BitmapDrawable)?.bitmap

        if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()

            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), byteArray)
            val body = MultipartBody.Part.createFormData("file", "image.png", requestFile)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.uploadImage(body)
                    withContext(Dispatchers.Main) {
                        progressIndicator.visibility = View.GONE
                        if (response.isSuccessful) {
                            val data = response.body()
                            data?.let {
                                navigateToResult(it)
                            }
                        } else {
                            Toast.makeText(requireContext(), "Analysis failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        progressIndicator.visibility = View.GONE
                        Toast.makeText(requireContext(), "There is an error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            progressIndicator.visibility = View.GONE
            Toast.makeText(requireContext(), "Please select an image first.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToResult(data: ApiResponse) {
        val intent = Intent(requireContext(), Result::class.java).apply {
            putExtra(Result.IMAGE_URL, data.imageUrl)
            putExtra(Result.WOUND_NAME, data.wounds_name)
            putExtra(Result.DESCRIPTION, data.description)
            putStringArrayListExtra(Result.TREATMENT, ArrayList(data.treatment))
            putStringArrayListExtra(Result.WARNINGS, ArrayList(data.warnings))
            putStringArrayListExtra(Result.DO_NOT, ArrayList(data.do_not))
            putStringArrayListExtra(Result.RECOMMENDED_MEDICINES, ArrayList(data.recommended_medicines))
        }
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IMAGE_PICK_CODE -> {
                    val imageUri: Uri? = data?.data
                    previewImageView.setImageURI(imageUri)
                }
                CAMERA_REQUEST_CODE -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    previewImageView.setImageBitmap(imageBitmap)
                }
            }
        }
    }
}