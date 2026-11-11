package com.example.smartcafecustomer

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()

    private lateinit var ratingBar: RatingBar
    private lateinit var commentInput: EditText
    private lateinit var submitButton: Button
    private lateinit var ratingText: TextView
    private lateinit var counterText: TextView
    private lateinit var statusText: TextView

    private var isSubmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        ratingBar = findViewById(R.id.ratingBarFeedback)
        commentInput = findViewById(R.id.editFeedbackComment)
        submitButton = findViewById(R.id.buttonSubmitFeedback)
        ratingText = findViewById(R.id.textSelectedRating)
        counterText = findViewById(R.id.textFeedbackCounter)
        statusText = findViewById(R.id.textFeedbackStatus)

        setupRating()
        setupCommentCounter()
        setupNavigation()

        submitButton.setOnClickListener {
            submitFeedback()
        }
    }

    private fun setupRating() {
        ratingBar.setOnRatingBarChangeListener {
                _, rating, _ ->

            ratingText.text = if (rating == 0f) {
                "No rating selected"
            } else {
                val stars = rating.toInt()
                if (stars == 1) {
                    "1 out of 5 stars"
                } else {
                    "$stars out of 5 stars"
                }
            }

            statusText.visibility = View.GONE
        }
    }

    private fun setupCommentCounter() {
        commentInput.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    val length = s?.length ?: 0
                    counterText.text =
                        "$length / 1000 characters"

                    statusText.visibility = View.GONE
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {}
            }
        )
    }

    private fun submitFeedback() {
        if (isSubmitting) return

        val currentUser = auth.currentUser
        val rating = ratingBar.rating.toInt()
        val comment = commentInput.text.toString().trim()

        statusText.visibility = View.GONE

        if (currentUser == null) {
            Toast.makeText(
                this,
                "Please log in before submitting feedback",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (rating !in 1..5) {
            ratingText.text = "Please select a star rating"
            return
        }

        if (comment.isEmpty()) {
            commentInput.error = "Please enter a comment"
            commentInput.requestFocus()
            return
        }

        if (comment.length > 1000) {
            commentInput.error = "Maximum 1000 characters"
            return
        }

        val feedback = hashMapOf(
            "userId" to currentUser.uid,
            "rating" to rating,
            "comment" to comment,
            "createdAt" to FieldValue.serverTimestamp()
        )

        setSubmitting(true)

        database.collection("feedback")
            .add(feedback)
            .addOnSuccessListener {
                setSubmitting(false)

                ratingBar.rating = 0f
                commentInput.text.clear()

                statusText.text =
                    "Thank you! Your feedback was submitted successfully."
                statusText.visibility = View.VISIBLE

                Toast.makeText(
                    this,
                    "Feedback submitted successfully!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                setSubmitting(false)

                statusText.text =
                    "Unable to submit feedback. Please try again."
                statusText.visibility = View.VISIBLE

                Toast.makeText(
                    this,
                    "Feedback submission failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun setSubmitting(submitting: Boolean) {
        isSubmitting = submitting

        submitButton.isEnabled = !submitting
        ratingBar.isEnabled = !submitting
        commentInput.isEnabled = !submitting

        submitButton.text = if (submitting) {
            "Submitting..."
        } else {
            "Submit Feedback"
        }
    }

    private fun setupNavigation() {

        findViewById<Button>(R.id.buttonBackToMenu)
            .setOnClickListener {
                navigateTo(MenuActivity::class.java)
            }

        findViewById<Button>(R.id.buttonBackToHome)
            .setOnClickListener {
                navigateTo(DashboardActivity::class.java)
            }
    }

    private fun navigateTo(
        destination: Class<*>
    ) {
        val intent = Intent(this, destination)

        intent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        )

        startActivity(intent)
        finish()
    }
}
