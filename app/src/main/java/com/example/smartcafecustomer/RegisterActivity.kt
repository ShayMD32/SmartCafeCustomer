package com.example.smartcafecustomer

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.buttonTerms).setOnClickListener {
            openPolicy(PolicyActivity.TERMS)
        }
        findViewById<Button>(R.id.buttonPrivacy).setOnClickListener {
            openPolicy(PolicyActivity.PRIVACY)
        }


        val fullNameInput =
            findViewById<EditText>(R.id.editTextFullName)

        val emailInput =
            findViewById<EditText>(R.id.editTextRegisterEmail)

        val passwordInput =
            findViewById<EditText>(R.id.editTextRegisterPassword)

        val confirmPasswordInput =
            findViewById<EditText>(R.id.editTextConfirmPassword)

        val registerButton =
            findViewById<Button>(R.id.buttonCreateAccount)

        val backButton =
            findViewById<Button>(R.id.buttonBackToLogin)

        val agreeTerms = findViewById<CheckBox>(R.id.checkAgreeTerms)

        backButton.setOnClickListener {
            finish()
        }

        registerButton.setOnClickListener {

            val fullName = fullNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            when {
                fullName.isBlank() -> {
                    fullNameInput.error = "Please enter your full name"
                    fullNameInput.requestFocus()
                }

                email.isBlank() -> {
                    emailInput.error = "Please enter your email"
                    emailInput.requestFocus()
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailInput.error = "Enter a valid email address"
                    emailInput.requestFocus()
                }

                password.length < 8 -> {
                    passwordInput.error =
                        "Password must contain at least 8 characters"
                    passwordInput.requestFocus()
                }

                confirmPassword.isBlank() -> {
                    confirmPasswordInput.error =
                        "Please confirm your password"
                    confirmPasswordInput.requestFocus()
                }

                !agreeTerms.isChecked -> {
                    Toast.makeText(this, "Please read and agree to the service terms before registering.", Toast.LENGTH_LONG).show()
                    agreeTerms.requestFocus()
                }

                password != confirmPassword -> {
                    confirmPasswordInput.error =
                        "Passwords do not match"
                    confirmPasswordInput.requestFocus()
                }

                else -> {
                    registerButton.isEnabled = false
                    registerButton.text = "Creating account..."

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->

                            if (task.isSuccessful) {
                                val user = auth.currentUser

                                if (user == null) {
                                    resetRegisterButton(registerButton)
                                    showMessage(
                                        "Account created, but the session could not be loaded."
                                    )
                                    return@addOnCompleteListener
                                }

                                // Device-local prototype evidence only: not a server-side consent ledger.
                                getSharedPreferences("smart_cafe_terms", MODE_PRIVATE)
                                    .edit()
                                    .putString("terms_version_${user.uid}", PolicyActivity.TERMS_VERSION)
                                    .putLong("accepted_at_${user.uid}", System.currentTimeMillis())
                                    .apply()

                                val profileUpdates =
                                    UserProfileChangeRequest.Builder()
                                        .setDisplayName(fullName)
                                        .build()

                                user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(this) { profileTask ->
                                        resetRegisterButton(registerButton)

                                        if (!profileTask.isSuccessful) {
                                            showMessage(
                                                "Account created, but your name could not be saved."
                                            )
                                        } else {
                                            showMessage(
                                                "Account created successfully!"
                                            )
                                        }

                                        openDashboard()
                                    }

                            } else {
                                resetRegisterButton(registerButton)

                                showMessage(
                                    "Registration failed. Please check your details and try again."
                                )
                            }
                        }
                }
            }
        }
    }

    private fun resetRegisterButton(button: Button) {
        button.isEnabled = true
        button.text = "Create Account"
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun openDashboard() {
        val intent = Intent(
            this,
            DashboardActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }

    private fun openPolicy(type: String) {
        startActivity(Intent(this, PolicyActivity::class.java).apply {
            putExtra(PolicyActivity.EXTRA_POLICY, type)
        })
    }
}
