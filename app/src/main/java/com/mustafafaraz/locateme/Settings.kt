package com.mustafafaraz.locateme

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mustafafaraz.locateme.data.api.RetrofitClient
import com.mustafafaraz.locateme.data.model.ChangePasswordRequest
import com.mustafafaraz.locateme.data.model.DeleteAccountRequest
import com.mustafafaraz.locateme.utils.TokenManager
import kotlinx.coroutines.launch

class Settings : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var currentPassword: EditText
    private lateinit var newPassword: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var toggleCurrentPassword: ImageView
    private lateinit var toggleNewPassword: ImageView
    private lateinit var toggleConfirmPassword: ImageView
    private lateinit var changePasswordButton: Button
    private lateinit var deleteAccountButton: Button

    private lateinit var tokenManager: TokenManager

    private var isCurrentPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        tokenManager = TokenManager(this)

        initializeViews()
        setupClickListeners()
        setupPasswordToggles()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.back_button)
        currentPassword = findViewById(R.id.current_password)
        newPassword = findViewById(R.id.new_password)
        confirmPassword = findViewById(R.id.confirm_password)
        toggleCurrentPassword = findViewById(R.id.toggle_current_password)
        toggleNewPassword = findViewById(R.id.toggle_new_password)
        toggleConfirmPassword = findViewById(R.id.toggle_confirm_password)
        changePasswordButton = findViewById(R.id.change_password_button)
        deleteAccountButton = findViewById(R.id.delete_account_button)
    }

    private fun setupPasswordToggles() {
        toggleCurrentPassword.setOnClickListener {
            isCurrentPasswordVisible = !isCurrentPasswordVisible
            togglePasswordVisibility(currentPassword, toggleCurrentPassword, isCurrentPasswordVisible)
        }

        toggleNewPassword.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            togglePasswordVisibility(newPassword, toggleNewPassword, isNewPasswordVisible)
        }

        toggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            togglePasswordVisibility(confirmPassword, toggleConfirmPassword, isConfirmPasswordVisible)
        }
    }

    private fun togglePasswordVisibility(editText: EditText, imageView: ImageView, isVisible: Boolean) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            imageView.setImageResource(R.drawable.ic_visibility)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imageView.setImageResource(R.drawable.ic_visibility_off)
        }
        // Move cursor to end of text
        editText.setSelection(editText.text.length)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        changePasswordButton.setOnClickListener {
            handleChangePassword()
        }

        deleteAccountButton.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun handleChangePassword() {
        val current = currentPassword.text.toString().trim()
        val new = newPassword.text.toString().trim()
        val confirm = confirmPassword.text.toString().trim()

        // Validation
        if (current.isEmpty()) {
            currentPassword.error = "Current password is required"
            currentPassword.requestFocus()
            return
        }

        if (new.isEmpty()) {
            newPassword.error = "New password is required"
            newPassword.requestFocus()
            return
        }

        if (new.length < 6) {
            newPassword.error = "Password must be at least 6 characters"
            newPassword.requestFocus()
            return
        }

        if (confirm.isEmpty()) {
            confirmPassword.error = "Please confirm your password"
            confirmPassword.requestFocus()
            return
        }

        if (new != confirm) {
            confirmPassword.error = "Passwords do not match"
            confirmPassword.requestFocus()
            return
        }

        lifecycleScope.launch {
            try {
                changePasswordButton.isEnabled = false
                changePasswordButton.text = "Changing..."

                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@Settings, "Please login", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                val authHeader = "Bearer $token"
                val request = ChangePasswordRequest(
                    currentPassword = current,
                    newPassword = new
                )

                val response = RetrofitClient.apiService.changePassword(authHeader, request)

                changePasswordButton.isEnabled = true
                changePasswordButton.text = "Change Password"

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@Settings, "Password changed successfully", Toast.LENGTH_SHORT).show()

                    // Clear password fields
                    currentPassword.text?.clear()
                    newPassword.text?.clear()
                    confirmPassword.text?.clear()
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to change password"
                    Toast.makeText(this@Settings, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("Settings", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                changePasswordButton.isEnabled = true
                changePasswordButton.text = "Change Password"
                Log.e("Settings", "Error changing password", e)
                Toast.makeText(this@Settings, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteAccountConfirmation() {
        // Create a custom dialog with password input
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_delete, null)
        val passwordInput = dialogView.findViewById<EditText>(R.id.delete_password_input)
        val togglePassword = dialogView.findViewById<ImageView>(R.id.toggle_delete_password)

        var isPasswordVisible = false

        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                passwordInput.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_visibility)
            } else {
                passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_visibility_off)
            }
            passwordInput.setSelection(passwordInput.text.length)
        }

        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you absolutely sure you want to delete your account? This action cannot be undone. All your items and data will be permanently deleted.\n\nPlease enter your password to confirm:")
            .setView(dialogView)
            .setPositiveButton("Delete") { _, _ ->
                val password = passwordInput.text.toString().trim()
                if (password.isEmpty()) {
                    Toast.makeText(this, "Password is required to delete account", Toast.LENGTH_SHORT).show()
                } else {
                    handleDeleteAccount(password)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleDeleteAccount(password: String) {
        lifecycleScope.launch {
            try {
                deleteAccountButton.isEnabled = false
                deleteAccountButton.text = "Deleting..."

                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@Settings, "Please login", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                val authHeader = "Bearer $token"
                val request = DeleteAccountRequest(password = password)
                val response = RetrofitClient.apiService.deleteAccount(authHeader, request)

                deleteAccountButton.isEnabled = true
                deleteAccountButton.text = "Delete Account"

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@Settings, "Account deleted successfully", Toast.LENGTH_LONG).show()

                    // Clear session data
                    tokenManager.clearAllData()

                    // Redirect to login page
                    val intent = Intent(this@Settings, LoginSignup::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to delete account"
                    Toast.makeText(this@Settings, errorMsg, Toast.LENGTH_LONG).show()
                    Log.e("Settings", "Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                deleteAccountButton.isEnabled = true
                deleteAccountButton.text = "Delete Account"
                Log.e("Settings", "Error deleting account", e)
                Toast.makeText(this@Settings, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
