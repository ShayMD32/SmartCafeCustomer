package com.example.smartcafecustomer

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class OrderHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val ordersContainer =
            findViewById<LinearLayout>(R.id.ordersContainer)

        val statusText =
            findViewById<TextView>(R.id.textOrderStatus)

        val backButton =
            findViewById<Button>(R.id.buttonBackToDashboard)

        backButton.setOnClickListener {
            finish()
        }

        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(
                this,
                "Please log in to view your orders",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        statusText.text = "Loading orders..."

        db.collection("users")
            .document(user.uid)
            .collection("orders")
            .get()
            .addOnSuccessListener { snapshot ->

                ordersContainer.removeAllViews()

                if (snapshot.isEmpty) {
                    statusText.text = "You haven't placed any orders yet."
                } else {
                    statusText.text = ""

                    val orders = snapshot.documents.sortedByDescending {
                        it.getTimestamp("createdAt")?.seconds ?: 0L
                    }

                    for (document in orders) {

                        val orderId = document.id.take(8)
                        val status =
                            document.getString("status") ?: "Unknown"

                        val totalPence =
                            document.getLong("totalPence") ?: 0L

                        val timestamp =
                            document.getTimestamp("createdAt")

                        val orderDate = if (timestamp != null) {
                            SimpleDateFormat(
                                "dd MMM yyyy, HH:mm",
                                Locale.UK
                            ).format(timestamp.toDate())
                        } else {
                            "Date unavailable"
                        }

                        val orderText = TextView(this)

                        val productLines = StringBuilder()

                        val items =
                            document.get("items") as? List<*>
                                ?: emptyList<Any>()

                        for (entry in items) {

                            val product = entry as? Map<*, *>
                                ?: continue

                            val name =
                                product["name"] as? String
                                    ?: "Unknown product"

                            val quantity =
                                (product["quantity"] as? Number)
                                    ?.toInt() ?: 0

                            productLines.append(
                                "$quantity x $name\n"
                            )
                        }

                        val total =
                            String.format(
                                Locale.UK,
                                "£%.2f",
                                totalPence / 100.0
                            )

                        orderText.text = """
                            Order #$orderId
                            Date: $orderDate
                            Status: $status

                            ${productLines.toString().trim()}

                            Total: $total
                        """.trimIndent()

                        orderText.textSize = 16f
                        orderText.setPadding(16, 20, 16, 28)

                        ordersContainer.addView(orderText)
                    }
                }
            }
            .addOnFailureListener { exception ->

                statusText.text = "Unable to load orders."

                Toast.makeText(
                    this,
                    "Error: ${exception.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
