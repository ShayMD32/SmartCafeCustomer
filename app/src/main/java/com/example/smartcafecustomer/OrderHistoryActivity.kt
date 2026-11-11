package com.example.smartcafecustomer

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
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

    // Remember previously observed order statuses
    private val knownStatuses = mutableMapOf<String, String>()
    private var firstSnapshotReceived = false

    companion object {
        private const val CHANNEL_ID = "smart_cafe_orders"
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            // If denied, order history still works normally.
            // The customer can enable notifications in Settings.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        ordersContainer = findViewById(R.id.ordersContainer)
        statusText = findViewById(R.id.textOrderStatus)

        setupNavigation()
        createNotificationChannel()
        requestNotificationPermission()
    }

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

    private fun loadOrders() {

        orderListener?.remove()
        orderListener = null

        knownStatuses.clear()
        firstSnapshotReceived = false

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

                // Detect changes without notifying on initial load
                val currentStatuses = mutableMapOf<String, String>()

                for (document in snapshot.documents) {

                    val currentStatus =
                        document.getString("status") ?: "Unknown"

                    currentStatuses[document.id] = currentStatus

                    if (firstSnapshotReceived) {

                        val previousStatus =
                            knownStatuses[document.id]

                        // Notify only when an EXISTING order changes
                        if (
                            previousStatus != null &&
                            previousStatus != currentStatus
                        ) {
                            showOrderNotification(
                                document.id,
                                currentStatus
                            )
                        }
                    }
                }

                knownStatuses.clear()
                knownStatuses.putAll(currentStatuses)
                firstSnapshotReceived = true

                if (snapshot.isEmpty) {
                    ordersContainer.removeAllViews()

                    showStatus(
                        "No orders yet.\nExplore the menu to get started."
                    )
                    return@addSnapshotListener
                }

                val orders = snapshot.documents.sortedByDescending {
                    it.getTimestamp("createdAt")?.seconds ?: 0L
                }

                displayOrders(orders)
            }
    }

    // Create the Android notification channel
    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Order Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description =
                    "Notifications about Smart Café order status changes"
            }

            val manager = getSystemService(
                NotificationManager::class.java
            )

            manager.createNotificationChannel(channel)
        }
    }

    // Request notification permission on Android 13+
    private fun requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (
                checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    // Display a notification for a changed order
    private fun showOrderNotification(
        orderId: String,
        status: String
    ) {

        if (
            Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val message = when (status.lowercase(Locale.UK)) {

            "preparing" ->
                "Your order is now being prepared."

            "ready" ->
                "Your order is ready for collection!"

            "completed", "collected" ->
                "Your order has been completed."

            "cancelled" ->
                "Your order has been cancelled."

            else ->
                "Your order status has changed to $status."
        }

        val intent = Intent(
            this,
            OrderHistoryActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            orderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(this, CHANNEL_ID)
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(this)
            }

        val notification = builder
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Smart Café Order Update")
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(
            NotificationManager::class.java
        )

        manager.notify(orderId.hashCode(), notification)
    }

    private fun showStatus(message: String) {
        statusText.text = message
        statusText.visibility = View.VISIBLE
    }

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

        numberText.text =
            "Order #${document.id.take(8).uppercase(Locale.UK)}"

        dateText.text = formatOrderDate(document)

        val status = document.getString("status") ?: "Unknown"

        badgeText.text = status
        badgeText.setTextColor(getStatusColor(status))

        productsText.text = formatOrderProducts(document)

        val totalPence = document.getLong("totalPence") ?: 0L

        totalText.text = formatPrice(totalPence)

        return orderCard
    }

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

    private fun formatPrice(pence: Long): String {

        return String.format(
            Locale.UK,
            "£%.2f",
            pence / 100.0
        )
    }

    override fun onStop() {

        orderListener?.remove()
        orderListener = null

        super.onStop()
    }
}
