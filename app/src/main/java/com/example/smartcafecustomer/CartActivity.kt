package com.example.smartcafecustomer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var cartContainer: LinearLayout
    private lateinit var itemCountText: TextView
    private lateinit var totalText: TextView
    private lateinit var subtitleText: TextView
    private lateinit var placeOrderButton: Button

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()

    private var orderInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)


        cartContainer = findViewById(R.id.cartContainer)
        itemCountText = findViewById(R.id.itemCountText)
        totalText = findViewById(R.id.totalText)
        subtitleText = findViewById(R.id.subtitleText)
        placeOrderButton = findViewById(R.id.buttonPlaceOrder)


        // Return to the menu
        findViewById<Button>(R.id.buttonContinueShopping)
            .setOnClickListener {
                finish()
            }

        // Return to the dashboard
        findViewById<Button>(R.id.buttonBackToHome)
            .setOnClickListener {
                val intent = Intent(
                    this,
                    DashboardActivity::class.java
                )

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                )

                startActivity(intent)
                finish()
            }

        // Submit the order
        placeOrderButton.setOnClickListener {
            placeOrder()
        }

        refreshCart()
    }

    override fun onResume() {
        super.onResume()

        if (::cartContainer.isInitialized) {
            refreshCart()
        }
    }

    // Display currency in pounds
    private fun formatPrice(pence: Int): String {
        return String.format(
            Locale.UK,
            "£%.2f",
            pence / 100.0
        )
    }

    // Match products with their photographs
    private fun getProductImage(productName: String): Int {
        return when (productName) {
            "Cappuccino" -> R.drawable.cappuccino
            "Flat White" -> R.drawable.flat_white
            "Hot Chocolate" -> R.drawable.hot_chocolate
            "Iced Latte" -> R.drawable.iced_latte
            "Iced Matcha" -> R.drawable.iced_matcha
            "Chicken Panini" -> R.drawable.chicken_panini
            "Cheese Toastie" -> R.drawable.cheese_toastie
            "Blueberry Muffin" -> R.drawable.blueberry_muffin
            else -> android.R.drawable.ic_menu_gallery
        }
    }

    // Refresh all basket information
    private fun refreshCart() {

        cartContainer.removeAllViews()

        val selectedItems = ShoppingCart.getItems()

        val count = selectedItems.sumOf { (_, quantity) ->
            quantity
        }

        val totalPence = ShoppingCart.getTotalPence()

        itemCountText.text =
            if (count == 1) {
                "1 item"
            } else {
                "$count items"
            }

        totalText.text = "Total: ${formatPrice(totalPence)}"

        subtitleText.text =
            if (selectedItems.isEmpty()) {
                "Your next favourite is waiting."
            } else {
                "Review your favourites before ordering."
            }

        placeOrderButton.isEnabled =
            selectedItems.isNotEmpty() && !orderInProgress

        // Display empty basket message
        if (selectedItems.isEmpty()) {

            val emptyText = TextView(this).apply {
                text = "Your basket is empty.\nExplore the menu to add something delicious."
                textSize = 16f
                setTextColor(
                    android.graphics.Color.parseColor("#75665E")
                )

                val padding = (
                        20 * resources.displayMetrics.density
                        ).toInt()

                setPadding(padding, padding, padding, padding)
            }

            cartContainer.addView(emptyText)
            return
        }

        // Display each selected product
        for ((item, quantity) in selectedItems) {

            val productCard = LayoutInflater.from(this)
                .inflate(
                    R.layout.item_cart_product,
                    cartContainer,
                    false
                )

            val productImage =
                productCard.findViewById<ImageView>(
                    R.id.imageCartProduct
                )

            val nameText =
                productCard.findViewById<TextView>(
                    R.id.textCartProductName
                )

            val unitPriceText =
                productCard.findViewById<TextView>(
                    R.id.textCartUnitPrice
                )

            val subtotalText =
                productCard.findViewById<TextView>(
                    R.id.textCartProductSubtotal
                )

            val quantityText =
                productCard.findViewById<TextView>(
                    R.id.textCartQuantity
                )

            val minusButton =
                productCard.findViewById<Button>(
                    R.id.buttonCartMinus
                )

            val plusButton =
                productCard.findViewById<Button>(
                    R.id.buttonCartPlus
                )

            val removeButton =
                productCard.findViewById<Button>(
                    R.id.buttonCartRemove
                )

            // Product information
            productImage.setImageResource(
                getProductImage(item.name)
            )

            productImage.contentDescription = item.name

            nameText.text = item.name

            unitPriceText.text =
                "${formatPrice(item.pricePence)} each"

            subtotalText.text =
                formatPrice(item.pricePence * quantity)

            quantityText.text = quantity.toString()

            // Increase quantity
            plusButton.setOnClickListener {
                ShoppingCart.addItem(item)
                refreshCart()
            }

            // Decrease quantity
            minusButton.setOnClickListener {
                ShoppingCart.removeItem(item)
                refreshCart()
            }

            // Remove all quantities of this product
            removeButton.setOnClickListener {
                ShoppingCart.removeAllOfItem(item)
                refreshCart()
            }

            cartContainer.addView(productCard)
        }
    }

    // Save customer's order to Firebase
    private fun placeOrder() {

        if (orderInProgress) return

        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(
                this,
                "Please log in before placing an order.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val selectedItems = ShoppingCart.getItems()

        if (selectedItems.isEmpty()) {
            Toast.makeText(
                this,
                "Your basket is empty.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val totalPence = ShoppingCart.getTotalPence()

        // Store product details and quantities
        val orderItems = selectedItems.map { (item, quantity) ->
            hashMapOf(
                "productId" to item.id,
                "name" to item.name,
                "pricePence" to item.pricePence,
                "quantity" to quantity
            )
        }

        // Create order record
        val order = hashMapOf(
            "userId" to user.uid,
            "items" to orderItems,
            "totalPence" to totalPence,
            "status" to "Placed",
            "createdAt" to FieldValue.serverTimestamp()
        )

        orderInProgress = true
        placeOrderButton.isEnabled = false
        placeOrderButton.text = "Placing order..."

        // Save to the authenticated customer's orders
        database.collection("users")
            .document(user.uid)
            .collection("orders")
            .add(order)
            .addOnSuccessListener {

                orderInProgress = false

                ShoppingCart.clearCart()

                placeOrderButton.text = "Place Order"

                Toast.makeText(
                    this,
                    "Order placed successfully!",
                    Toast.LENGTH_LONG
                ).show()

                refreshCart()
            }
            .addOnFailureListener { error ->

                orderInProgress = false

                placeOrderButton.text = "Place Order"

                refreshCart()

                Toast.makeText(
                    this,
                    "Order failed: ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
