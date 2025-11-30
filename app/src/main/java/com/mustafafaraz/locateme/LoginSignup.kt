package com.mustafafaraz.locateme

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class LoginSignup : AppCompatActivity() {

    private lateinit var loginToggle: TextView
    private lateinit var signupToggle: TextView
    private lateinit var titleText: TextView
    private lateinit var actionButton: Button

    // Login fields
    private lateinit var emailLabel: TextView
    private lateinit var username: EditText
    private lateinit var passwordLabel: TextView
    private lateinit var password: EditText

    // Signup only fields
    private lateinit var fullNameLabel: TextView
    private lateinit var fullName: EditText
    private lateinit var studentIdLabel: TextView
    private lateinit var studentId: EditText
    private lateinit var batchLabel: TextView
    private lateinit var batch: EditText
    private lateinit var departmentLabel: TextView
    private lateinit var department: EditText
    private lateinit var sectionLabel: TextView
    private lateinit var section: EditText

    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_signup)

        initializeViews()
        setupToggleListeners()
    }

    private fun initializeViews() {
        loginToggle = findViewById(R.id.login_toggle)
        signupToggle = findViewById(R.id.signup_toggle)
        titleText = findViewById(R.id.title_text)
        actionButton = findViewById(R.id.action_button)

        // Login fields
        emailLabel = findViewById(R.id.email_label)
        username = findViewById(R.id.username)
        passwordLabel = findViewById(R.id.password_label)
        password = findViewById(R.id.password)

        // Signup only fields
        fullNameLabel = findViewById(R.id.full_name_label)
        fullName = findViewById(R.id.full_name)
        studentIdLabel = findViewById(R.id.student_id_label)
        studentId = findViewById(R.id.student_id)
        batchLabel = findViewById(R.id.batch_label)
        batch = findViewById(R.id.batch)
        departmentLabel = findViewById(R.id.department_label)
        department = findViewById(R.id.department)
        sectionLabel = findViewById(R.id.section_label)
        section = findViewById(R.id.section)
    }

    private fun setupToggleListeners() {
        loginToggle.setOnClickListener {
            if (!isLoginMode) {
                switchToLogin()
            }
        }

        signupToggle.setOnClickListener {
            if (isLoginMode) {
                switchToSignup()
            }
        }

        actionButton.setOnClickListener {
            if (isLoginMode) {
                handleLogin()
            } else {
                handleSignup()
            }
        }
    }

    private fun switchToLogin() {
        isLoginMode = true

        // Update title
        titleText.text = "Sign In to Locate Me"

        // Update toggle buttons
        loginToggle.setBackgroundResource(R.drawable.selected_button_login_signup)
        loginToggle.setTextColor(ContextCompat.getColor(this, R.color.black))

        signupToggle.background = null
        signupToggle.setTextColor(ContextCompat.getColor(this, R.color.black))

        // Hide signup-only fields
        fullNameLabel.visibility = View.GONE
        fullName.visibility = View.GONE
        studentIdLabel.visibility = View.GONE
        studentId.visibility = View.GONE
        batchLabel.visibility = View.GONE
        batch.visibility = View.GONE
        departmentLabel.visibility = View.GONE
        department.visibility = View.GONE
        sectionLabel.visibility = View.GONE
        section.visibility = View.GONE

        // Update button text
        actionButton.text = "Login"
    }

    private fun switchToSignup() {
        isLoginMode = false

        // Update title
        titleText.text = "Join Locate Me"

        // Update toggle buttons
        loginToggle.background = null
        loginToggle.setTextColor(ContextCompat.getColor(this, R.color.black))

        signupToggle.setBackgroundResource(R.drawable.selected_button_login_signup)
        signupToggle.setTextColor(ContextCompat.getColor(this, R.color.black))

        // Show signup-only fields
        fullNameLabel.visibility = View.VISIBLE
        fullName.visibility = View.VISIBLE
        studentIdLabel.visibility = View.VISIBLE
        studentId.visibility = View.VISIBLE
        batchLabel.visibility = View.VISIBLE
        batch.visibility = View.VISIBLE
        departmentLabel.visibility = View.VISIBLE
        department.visibility = View.VISIBLE
        sectionLabel.visibility = View.VISIBLE
        section.visibility = View.VISIBLE

        // Update button text
        actionButton.text = "Create Account"
    }

    private fun handleLogin() {
        val email = username.text.toString()
        val pass = password.text.toString()

        // TODO: Add validation and authentication logic

        // Navigate to Home page
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
        finish() // Close login screen so user can't go back to it
    }

    private fun handleSignup() {
        val name = fullName.text.toString()
        val email = username.text.toString()
        val studId = studentId.text.toString()
        val batchVal = batch.text.toString()
        val dept = department.text.toString()
        val sect = section.text.toString()
        val pass = password.text.toString()

        // TODO: Add validation and signup logic

        // Navigate to Home page after successful signup
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
        finish() // Close login screen so user can't go back to it
    }
}