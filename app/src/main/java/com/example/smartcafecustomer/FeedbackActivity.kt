package com.example.smartcafecustomer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        // Connect to Firebase
        val auth = FirebaseAuth.getInstance()
        val database = FirebaseFirestore.getInstance()

        // Connect Kotlin to the XML components
        val ratingBar =
            findViewById<RatingBar>(R.id.ratingBarFeedback)

        val commentInput =
            findViewById<EditText>(R.id.editFeedbackComment)

        val submitButton =
            findViewById<Button>(R.id.buttonSubmitFeedback)

        val backToMenuButton =
            findViewById<Button>(R.id.buttonBackToMenu)

        val backToHomeButton =
            findViewById<Button>(R.id.buttonBackToHome)

        // Submit customer feedback
        submitButton.setOnClickListener {

            val rating = ratingBar.rating.toInt()
            val comment = commentInput.text.toString().trim()
            val currentUser = auth.currentUser

            when {
                currentUser == null -> {
                    Toast.makeText(
                        this,
                        "Please log in before submitting feedback",
                        Toast.LENGTH_LONG
                    ).show()
                }

                rating !in 1..5 -> {
                    Toast.makeText(
                        this,
                        "Please select a star rating",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                comment.isEmpty() -> {
                    commentInput.error = "Please enter a comment"
                }

                comment.length > 1000 -> {
                    commentInput.error = "Maximum 1000 characters"
                }

                else -> {
                    submitButton.isEnabled = false

                    val feedback = hashMapOf(
                        "userId" to currentUser.uid,
                        "rating" to rating,
                        "comment" to comment,
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    database.collection("feedback")
                        .add(feedback)
                        .addOnSuccessListener {
                            submitButton.isEnabled = true

                            Toast.makeText(
                                this,
                                "Feedback submitted successfully!",
                                Toast.LENGTH_LONG
                            ).show()

                            ratingBar.rating = 0f
                            commentInput.text.clear()
                        }
                        .addOnFailureListener { error ->
                            submitButton.isEnabled = true

                            Toast.makeText(
                                this,
                                "Submission failed: ${error.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
        }

        // Return to the café menu
        backToMenuButton.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            startActivity(intent)
            finish()
        }

        // Return to the dashboard
        backToHomeButton.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            startActivity(intent)
            finish()
        }
    }
}
