
package com.example.smartcafecustomer

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<EditText>(R.id.editTextEmail)
        val passwordInput = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val registerButton = findViewById<Button>(R.id.buttonRegister)

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {

            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            when {
                email.isEmpty() -> {
                    emailInput.error = "Enter your email"
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailInput.error = "Enter a valid email address"
                }

                password.isEmpty() -> {
                    passwordInput.error = "Enter your password"
                }

                else -> {
                    loginButton.isEnabled = false

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->

                            loginButton.isEnabled = true


                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Login successful!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent = Intent(this, DashboardActivity::class.java)
                                startActivity(intent)
                                finish()
                            }



                            else {
                                val errorMessage = task.exception?.localizedMessage
                                    ?: "Unknown login error"

                                Toast.makeText(
                                    this,
                                    errorMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        }
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }
}
