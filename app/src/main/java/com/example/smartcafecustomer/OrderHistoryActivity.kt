
package com.example.smartcafecustomer

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Locale

class OrderHistoryActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var orderListener: ListenerRegistration? = null

    private lateinit var ordersContainer: LinearLayout
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        ordersContainer = findViewById(R.id.ordersContainer)
        statusText = findViewById(R.id.textOrderStatus)

        val backButton =
            findViewById<Button>(R.id.buttonBackToDashboard)

        backButton.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        val user = auth.currentUser

        if (user == null) {
            statusText.text = "Please log in to view orders."
            finish()
            return
        }

        statusText.text = "Loading orders..."
        ordersContainer.removeAllViews()

        // Listen for changes to this customer's orders
        orderListener = db.collection("users")
            .document(user.uid)
            .collection("orders")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    statusText.text = "Unable to load orders."
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    return@addSnapshotListener
                }

                ordersContainer.removeAllViews()

                if (snapshot.isEmpty) {
                    statusText.text =
                        "You haven't placed any orders yet."
                    return@addSnapshotListener
                }

                statusText.text = ""

                // Newest orders first
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

                    val items =
                        document.get("items") as? List<*>
                            ?: emptyList<Any>()

                    val productLines = StringBuilder()

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

                    val total = String.format(
                        Locale.UK,
                        "£%.2f",
                        totalPence / 100.0
                    )

                    val orderText = TextView(this)

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

    override fun onStop() {
        super.onStop()

        // Stop listening when the screen is no longer visible
        orderListener?.remove()
        orderListener = null
    }
}
