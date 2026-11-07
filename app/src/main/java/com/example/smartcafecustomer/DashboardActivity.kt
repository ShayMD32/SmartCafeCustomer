package com.example.smartcafecustomer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)


        val feedbackButton =
            findViewById<Button>(R.id.buttonFeedback)

        feedbackButton.setOnClickListener {
            val intent = Intent(this, FeedbackActivity::class.java)
            startActivity(intent)
        }


        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val logoutButton = findViewById<Button>(R.id.buttonLogout)
        val browseMenuButton = findViewById<Button>(R.id.buttonBrowseMenu)
        val orderHistoryButton = findViewById<Button>(R.id.buttonOrderHistory)

        logoutButton.setOnClickListener {
            auth.signOut()

            Toast.makeText(
                this,
                "Logged out successfully",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        browseMenuButton.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }


        orderHistoryButton.setOnClickListener {
            val intent = Intent(this, OrderHistoryActivity::class.java)
            startActivity(intent)
        }

    }
}
