package com.mustafafaraz.locateme

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class EditProfile : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var profileImage: ImageView
    private lateinit var changePhotoButton: TextView
    private lateinit var editFullName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editStudentId: EditText
    private lateinit var editBatch: EditText
    private lateinit var editDepartment: EditText
    private lateinit var editSection: EditText
    private lateinit var saveButton: Button

    private var selectedImageUri: Uri? = null

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            selectedImageUri = data?.data
            profileImage.setImageURI(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initializeViews()
        loadCurrentProfileData()
        setupClickListeners()
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
    }

    private fun loadCurrentProfileData() {
        // TODO: Load actual user data from your data source (SharedPreferences, Database, etc.)
        // For now, using placeholder data
        editFullName.setText("John Doe")
        editEmail.setText("i230631@isb.nu.edu.pk")
        editStudentId.setText("i230631")
        editBatch.setText("2023")
        editDepartment.setText("Computer Science")
        editSection.setText("A")
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

        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}