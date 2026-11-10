package com.example.smartcafecustomer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()

        // Ensure the customer is signed in
        if (auth.currentUser == null) {
            returnToLogin()
            return
        }

        // Browse café products
        findViewById<Button>(R.id.buttonBrowseMenu)
            .setOnClickListener {
                startActivity(
                    Intent(this, MenuActivity::class.java)
                )
            }

        // View previous orders
        findViewById<Button>(R.id.buttonOrderHistory)
            .setOnClickListener {
                startActivity(
                    Intent(this, OrderHistoryActivity::class.java)
                )
            }

        // Submit customer feedback
        findViewById<Button>(R.id.buttonFeedback)
            .setOnClickListener {
                startActivity(
                    Intent(this, FeedbackActivity::class.java)
                )
            }

        // Sign out securely
        findViewById<Button>(R.id.buttonLogout)
            .setOnClickListener {
                auth.signOut()

                Toast.makeText(
                    this,
                    "Logged out successfully",
                    Toast.LENGTH_SHORT
                ).show()

                returnToLogin()
            }
    }

    private fun returnToLogin() {
        val intent = Intent(this, MainActivity::class.java)

        // Clear previous screens from navigation history
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}
