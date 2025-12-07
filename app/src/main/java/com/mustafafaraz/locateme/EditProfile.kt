package com.mustafafaraz.locateme

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.data.model.UpdateProfileRequest
import com.mustafafaraz.locateme.utils.ImageHelper
import com.mustafafaraz.locateme.utils.TokenManager
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch

class EditProfile : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var profileImage: CircleImageView
    private lateinit var changePhotoButton: TextView
    private lateinit var editFullName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editStudentId: EditText
    private lateinit var editBatch: EditText
    private lateinit var editDepartment: EditText
    private lateinit var editSection: EditText
    private lateinit var saveButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var tokenManager: TokenManager
    private var selectedImageUri: Uri? = null
    private var originalProfilePicUrl: String? = null
    private var hasImageChanged = false

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            selectedImageUri = data?.data
            selectedImageUri?.let {
                profileImage.setImageURI(it)
                hasImageChanged = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        tokenManager = TokenManager(this)

        initializeViews()
        setupClickListeners()
        loadCurrentProfileData()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.back_button)
        profileImage = findViewById(R.id.profile_image)
        changePhotoButton = findViewById(R.id.change_photo_button)
        editFullName = findViewById(R.id.edit_full_name)
        editEmail = findViewById(R.id.edit_email)
        editStudentId = findViewById(R.id.edit_student_id)
        editBatch = findViewById(R.id.edit_batch)
        editDepartment = findViewById(R.id.edit_department)
        editSection = findViewById(R.id.edit_section)
        saveButton = findViewById(R.id.save_button)

        // Add progress bar to layout programmatically if not in XML
        progressBar = ProgressBar(this)
    }

    private fun loadCurrentProfileData() {
        lifecycleScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@EditProfile, "Please login", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                val authHeader = "Bearer $token"
                val response = RetrofitClient.apiService.getProfile(authHeader)

                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.data
                    user?.let { displayUserData(it) }
                } else {
                    Toast.makeText(this@EditProfile, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    Log.e("EditProfile", "Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("EditProfile", "Error loading profile", e)
                Toast.makeText(this@EditProfile, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayUserData(user: com.mustafafaraz.locateme.data.model.User) {
        // Load profile picture
        originalProfilePicUrl = user.profilePic
        if (!user.profilePic.isNullOrEmpty()) {
            Glide.with(this)
                .load(user.profilePic)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(profileImage)
        } else {
            profileImage.setImageResource(R.drawable.ic_person)
        }

        // Set user info
        editFullName.setText(user.fullName)
        editEmail.setText(user.email)
        editStudentId.setText(user.studentId)
        editBatch.setText(user.batch)
        editDepartment.setText(user.department)
        editSection.setText(user.section)

        // Make email read-only
        editEmail.isEnabled = false
        editEmail.alpha = 0.6f

        Log.d("EditProfile", "Profile loaded: ${user.fullName}")
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        changePhotoButton.setOnClickListener {
            openImagePicker()
        }

        saveButton.setOnClickListener {
            saveProfileChanges()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun saveProfileChanges() {
        val fullName = editFullName.text.toString().trim()
        val studentId = editStudentId.text.toString().trim()
        val batch = editBatch.text.toString().trim()
        val department = editDepartment.text.toString().trim()
        val section = editSection.text.toString().trim()

        // Validation
        if (fullName.isEmpty()) {
            editFullName.error = "Full name is required"
            editFullName.requestFocus()
            return
        }

        if (studentId.isEmpty()) {
            editStudentId.error = "Student ID is required"
            editStudentId.requestFocus()
            return
        }

        if (batch.isEmpty()) {
            editBatch.error = "Batch is required"
            editBatch.requestFocus()
            return
        }

        if (department.isEmpty()) {
            editDepartment.error = "Department is required"
            editDepartment.requestFocus()
            return
        }

        if (section.isEmpty()) {
            editSection.error = "Section is required"
            editSection.requestFocus()
            return
        }

        lifecycleScope.launch {
            try {
                saveButton.isEnabled = false
                saveButton.text = "Saving..."

                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@EditProfile, "Please login", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                val authHeader = "Bearer $token"

                // Only convert image to base64 if it was changed
                var profilePicBase64: String? = null
                if (hasImageChanged && selectedImageUri != null) {
                    profilePicBase64 = ImageHelper.uriToBase64(this@EditProfile, selectedImageUri!!)
                    Log.d("EditProfile", "Profile picture changed, converting to base64")
                } else {
                    Log.d("EditProfile", "Profile picture not changed, skipping conversion")
                }

                val updateRequest = UpdateProfileRequest(
                    fullName = fullName,
                    studentId = studentId,
                    batch = batch,
                    department = department,
                    section = section,
                    profilePic = profilePicBase64 // Only send if changed, otherwise null
                )

                val response = RetrofitClient.apiService.updateProfile(authHeader, updateRequest)

                saveButton.isEnabled = true
                saveButton.text = "Save Changes"

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@EditProfile, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@EditProfile, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    Log.e("EditProfile", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                saveButton.isEnabled = true
                saveButton.text = "Save Changes"
                Log.e("EditProfile", "Error updating profile", e)
                Toast.makeText(this@EditProfile, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}