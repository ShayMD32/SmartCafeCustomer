
package com.example.smartcafecustomer

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val fullNameInput = findViewById<EditText>(R.id.editTextFullName)
        val emailInput = findViewById<EditText>(R.id.editTextRegisterEmail)
        val passwordInput = findViewById<EditText>(R.id.editTextRegisterPassword)
        val confirmPasswordInput = findViewById<EditText>(R.id.editTextConfirmPassword)

        val registerButton = findViewById<Button>(R.id.buttonCreateAccount)
        val backButton = findViewById<Button>(R.id.buttonBackToLogin)

        registerButton.setOnClickListener {

            val fullName = fullNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            when {
                fullName.isEmpty() -> {
                    fullNameInput.error = "Please enter your full name"
                }

                email.isEmpty() -> {
                    emailInput.error = "Please enter your email"
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailInput.error = "Please enter a valid email"
                }

                password.length < 8 -> {
                    passwordInput.error = "Password must be at least 8 characters"
                }

                password != confirmPassword -> {
                    confirmPasswordInput.error = "Passwords do not match"
                }

                else -> {
                    Toast.makeText(
                        this,
                        "Details valid. Account creation not connected yet.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}
