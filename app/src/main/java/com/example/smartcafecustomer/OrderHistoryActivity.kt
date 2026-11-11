package com.example.smartcafecustomer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
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

        setupNavigation()
    }

    // Set up navigation buttons
    private fun setupNavigation() {

        findViewById<Button>(R.id.buttonBackToMenu)
            .setOnClickListener {
                navigateTo(MenuActivity::class.java)
            }

        findViewById<Button>(R.id.buttonBackToDashboard)
            .setOnClickListener {
                navigateTo(DashboardActivity::class.java)
            }
    }

    // Navigate without creating unnecessary duplicate screens
    private fun navigateTo(destination: Class<*>) {

        val intent = Intent(this, destination)

        intent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        )

        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        loadOrders()
    }

    // Listen for changes to the signed-in customer's orders
    private fun loadOrders() {

        orderListener?.remove()
        orderListener = null

        val user = auth.currentUser

        if (user == null) {
            ordersContainer.removeAllViews()
            showStatus("Please log in to view orders.")
            return
        }

        ordersContainer.removeAllViews()
        showStatus("Loading your orders...")

        orderListener = db.collection("users")
            .document(user.uid)
            .collection("orders")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    ordersContainer.removeAllViews()

                    showStatus(
                        "Unable to load orders. Check your connection."
                    )
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    showStatus("Loading your orders...")
                    return@addSnapshotListener
                }

                if (snapshot.isEmpty) {
                    ordersContainer.removeAllViews()

                    showStatus(
                        "No orders yet.\nExplore the menu to get started."
                    )
                    return@addSnapshotListener
                }

                // Display newest orders first
                val orders = snapshot.documents.sortedByDescending {
                    it.getTimestamp("createdAt")?.seconds ?: 0L
                }

                displayOrders(orders)
            }
    }

    // Display a status, empty-state or error message
    private fun showStatus(message: String) {
        statusText.text = message
        statusText.visibility = View.VISIBLE
    }

    // Refresh the visible list of orders
    private fun displayOrders(
        orders: List<DocumentSnapshot>
    ) {

        ordersContainer.removeAllViews()
        statusText.visibility = View.GONE

        for (document in orders) {
            val orderCard = createOrderCard(document)
            ordersContainer.addView(orderCard)
        }
    }

    // Create a reusable order-history card
    private fun createOrderCard(
        document: DocumentSnapshot
    ): View {

        val orderCard = LayoutInflater.from(this)
            .inflate(
                R.layout.item_order_history,
                ordersContainer,
                false
            )

        val numberText = orderCard.findViewById<TextView>(
            R.id.textOrderNumber
        )

        val dateText = orderCard.findViewById<TextView>(
            R.id.textOrderDate
        )

        val badgeText = orderCard.findViewById<TextView>(
            R.id.textOrderBadge
        )

        val productsText = orderCard.findViewById<TextView>(
            R.id.textOrderProducts
        )

        val totalText = orderCard.findViewById<TextView>(
            R.id.textOrderTotal
        )

        // Display a shortened order reference
        numberText.text =
            "Order #${document.id.take(8).uppercase(Locale.UK)}"

        // Display the order date
        dateText.text = formatOrderDate(document)

        // Display the order status
        val status = document.getString("status") ?: "Unknown"

        badgeText.text = status
        badgeText.setTextColor(getStatusColor(status))

        // Display the ordered products and quantities
        productsText.text = formatOrderProducts(document)

        // Display the order total
        val totalPence = document.getLong("totalPence") ?: 0L

        totalText.text = formatPrice(totalPence)

        return orderCard
    }

    // Format the order date for UK customers
    private fun formatOrderDate(
        document: DocumentSnapshot
    ): String {

        val timestamp = document.getTimestamp("createdAt")
            ?: return "Date pending"

        return SimpleDateFormat(
            "dd MMM yyyy, HH:mm",
            Locale.UK
        ).format(timestamp.toDate())
    }

    // Format products as quantity × product name
    private fun formatOrderProducts(
        document: DocumentSnapshot
    ): String {

        val items = document.get("items") as? List<*>
            ?: emptyList<Any>()

        val productLines = mutableListOf<String>()

        for (entry in items) {

            val product = entry as? Map<*, *>
                ?: continue

            val name = product["name"] as? String
                ?: "Unknown product"

            val quantity = (
                    product["quantity"] as? Number
                    )?.toInt() ?: 0

            if (quantity > 0) {
                productLines.add("$quantity × $name")
            }
        }

        return if (productLines.isEmpty()) {
            "Item details unavailable"
        } else {
            productLines.joinToString("\n")
        }
    }

    // Use consistent colours for order statuses
    private fun getStatusColor(status: String): Int {

        val colour = when (
            status.trim().lowercase(Locale.UK)
        ) {
            "pending", "placed" -> "#A57550"
            "preparing" -> "#936122"
            "ready" -> "#467451"
            "completed", "collected" -> "#467451"
            "cancelled" -> "#B24A4A"
            else -> "#75665E"
        }

        return Color.parseColor(colour)
    }

    // Convert an order total from pence to pounds
    private fun formatPrice(pence: Long): String {

        return String.format(
            Locale.UK,
            "£%.2f",
            pence / 100.0
        )
    }

    override fun onStop() {

        // Prevent unnecessary Firestore listeners
        orderListener?.remove()
        orderListener = null

        super.onStop()
    }
}
