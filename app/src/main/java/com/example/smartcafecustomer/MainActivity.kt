package com.example.smartcafecustomer

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.buttonTerms).setOnClickListener {
            openPolicy(PolicyActivity.TERMS)
        }
        findViewById<Button>(R.id.buttonPrivacy).setOnClickListener {
            openPolicy(PolicyActivity.PRIVACY)
        }


        val emailInput =
            findViewById<EditText>(R.id.editTextEmail)

        val passwordInput =
            findViewById<EditText>(R.id.editTextPassword)

        val loginButton =
            findViewById<Button>(R.id.buttonLogin)

        val registerButton =
            findViewById<Button>(R.id.buttonRegister)

        val togglePassword =
            findViewById<TextView>(R.id.textTogglePassword)

        var passwordVisible = false

        // Show or hide password
        togglePassword.setOnClickListener {
            passwordVisible = !passwordVisible

            passwordInput.inputType =
                if (passwordVisible) {
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                } else {
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
                }

            togglePassword.text =
                if (passwordVisible) "Hide" else "Show"

            passwordInput.setSelection(
                passwordInput.text.length
            )
        }

        registerButton.setOnClickListener {
            startActivity(
                Intent(this, RegisterActivity::class.java)
            )
        }

        loginButton.setOnClickListener {

            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            when {
                email.isEmpty() -> {
                    emailInput.error = "Enter your email"
                    emailInput.requestFocus()
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailInput.error = "Enter a valid email address"
                    emailInput.requestFocus()
                }

                password.isEmpty() -> {
                    passwordInput.error = "Enter your password"
                    passwordInput.requestFocus()
                }

                else -> {
                    loginButton.isEnabled = false
                    loginButton.text = "Signing in..."

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->

                            loginButton.isEnabled = true
                            loginButton.text = "Sign In"

                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Welcome back!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent = Intent(
                                    this,
                                    DashboardActivity::class.java
                                )

                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                                startActivity(intent)
                                finish()

                            } else {
                                Toast.makeText(
                                    this,
                                    "Unable to sign in. Check your details and try again.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }
        }
    }

    private fun openPolicy(type: String) {
        startActivity(Intent(this, PolicyActivity::class.java).apply {
            putExtra(PolicyActivity.EXTRA_POLICY, type)
        })
    }
}
