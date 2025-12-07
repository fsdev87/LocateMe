package com.mustafafaraz.locateme

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.data.model.LoginRequest
import com.mustafafaraz.locateme.data.model.SignupRequest
import com.mustafafaraz.locateme.utils.TokenManager
import kotlinx.coroutines.launch

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
    private lateinit var togglePassword: ImageView

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
    private var isPasswordVisible = false
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_signup)

        // Initialize TokenManager
        tokenManager = TokenManager(this)

        initializeViews()
        setupToggleListeners()
        setupPasswordToggle()
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
        togglePassword = findViewById(R.id.toggle_password)

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

    private fun setupPasswordToggle() {
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                password.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_visibility)
            } else {
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_visibility_off)
            }
            // Move cursor to end of text
            password.setSelection(password.text.length)
        }
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
        val email = username.text.toString().trim()
        val pass = password.text.toString().trim()

        // Validation
        if (email.isEmpty()) {
            username.error = "Email is required"
            username.requestFocus()
            return
        }

        if (pass.isEmpty()) {
            password.error = "Password is required"
            password.requestFocus()
            return
        }

        // Disable button and show loading
        actionButton.isEnabled = false
        actionButton.text = "Logging in..."

        // Make API call
        lifecycleScope.launch {
            try {
                val request = LoginRequest(email, pass)
                val response = RetrofitClient.apiService.login(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val authData = response.body()?.data

                    if (authData != null) {
                        // Save token and user data
                        tokenManager.saveAuthData(
                            token = authData.token,
                            userId = authData.user.id.toString(),
                            userName = authData.user.fullName,
                            userEmail = authData.user.email
                        )

                        Toast.makeText(
                            this@LoginSignup,
                            "Login successful! Welcome ${authData.user.fullName}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to Home
                        navigateToHome()
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Login failed. Please check your credentials."
                    Toast.makeText(this@LoginSignup, errorMsg, Toast.LENGTH_LONG).show()

                    // Re-enable button
                    actionButton.isEnabled = true
                    actionButton.text = "Login"
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginSignup,
                    "Network error: ${e.message}. Please check your connection.",
                    Toast.LENGTH_LONG
                ).show()

                // Re-enable button
                actionButton.isEnabled = true
                actionButton.text = "Login"
            }
        }
    }

    private fun handleSignup() {
        val name = fullName.text.toString().trim()
        val email = username.text.toString().trim()
        val studId = studentId.text.toString().trim()
        val batchVal = batch.text.toString().trim()
        val dept = department.text.toString().trim()
        val sect = section.text.toString().trim()
        val pass = password.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            fullName.error = "Full name is required"
            fullName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            username.error = "Email is required"
            username.requestFocus()
            return
        }

        if (studId.isEmpty()) {
            studentId.error = "Student ID is required"
            studentId.requestFocus()
            return
        }

        if (batchVal.isEmpty()) {
            batch.error = "Batch is required"
            batch.requestFocus()
            return
        }

        if (dept.isEmpty()) {
            department.error = "Department is required"
            department.requestFocus()
            return
        }

        if (sect.isEmpty()) {
            section.error = "Section is required"
            section.requestFocus()
            return
        }

        if (pass.isEmpty() || pass.length < 6) {
            password.error = "Password must be at least 6 characters"
            password.requestFocus()
            return
        }

        // Disable button and show loading
        actionButton.isEnabled = false
        actionButton.text = "Creating account..."

        // Make API call
        lifecycleScope.launch {
            try {
                val request = SignupRequest(
                    fullName = name,
                    email = email,
                    password = pass,
                    studentId = studId,
                    batch = batchVal,
                    department = dept,
                    section = sect
                )

                val response = RetrofitClient.apiService.signup(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val authData = response.body()?.data

                    if (authData != null) {
                        // Save token and user data
                        tokenManager.saveAuthData(
                            token = authData.token,
                            userId = authData.user.id.toString(),
                            userName = authData.user.fullName,
                            userEmail = authData.user.email
                        )

                        Toast.makeText(
                            this@LoginSignup,
                            "Account created successfully! Welcome ${authData.user.fullName}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to Home
                        navigateToHome()
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Signup failed. Please try again."
                    Toast.makeText(this@LoginSignup, errorMsg, Toast.LENGTH_LONG).show()

                    // Re-enable button
                    actionButton.isEnabled = true
                    actionButton.text = "Create Account"
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginSignup,
                    "Network error: ${e.message}. Please check your connection.",
                    Toast.LENGTH_LONG
                ).show()

                // Re-enable button
                actionButton.isEnabled = true
                actionButton.text = "Create Account"
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
        finish() // Close login screen so user can't go back
    }
}