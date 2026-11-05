package com.example.smartcafecustomer

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class CartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val cartContainer =
            findViewById<LinearLayout>(R.id.cartContainer)

        val totalText =
            findViewById<TextView>(R.id.textCartTotal)

        val placeOrderButton =
            findViewById<Button>(R.id.buttonPlaceOrder)

        val backButton =
            findViewById<Button>(R.id.buttonBackToMenu)

        var isSubmitting = false

        fun formatPrice(pence: Int): String {
            return String.format(
                Locale.UK,
                "£%.2f",
                pence / 100.0
            )
        }

        fun refreshCart() {

            cartContainer.removeAllViews()

            val selectedItems = ShoppingCart.getItems()

            if (selectedItems.isEmpty()) {
                val emptyText = TextView(this)
                emptyText.text = "Your basket is empty."
                emptyText.textSize = 18f
                cartContainer.addView(emptyText)
            }

            for ((item, quantity) in selectedItems) {

                val productText = TextView(this)
                val subtotalPence = item.pricePence * quantity

                productText.text =
                    "${item.name} x $quantity — ${formatPrice(subtotalPence)}"

                productText.textSize = 18f
                productText.setPadding(12, 24, 12, 12)
                cartContainer.addView(productText)

                val buttonRow = LinearLayout(this)
                buttonRow.orientation = LinearLayout.HORIZONTAL

                val minusButton = Button(this)
                minusButton.text = "−"
                minusButton.contentDescription =
                    "Decrease quantity of ${item.name}"

                minusButton.isEnabled = !isSubmitting
                minusButton.setOnClickListener {
                    ShoppingCart.removeItem(item)
                    refreshCart()
                }

                val plusButton = Button(this)
                plusButton.text = "+"
                plusButton.contentDescription =
                    "Increase quantity of ${item.name}"

                plusButton.isEnabled = !isSubmitting
                plusButton.setOnClickListener {
                    ShoppingCart.addItem(item)
                    refreshCart()
                }

                val removeButton = Button(this)
                removeButton.text = "Remove"
                removeButton.isEnabled = !isSubmitting

                removeButton.setOnClickListener {
                    ShoppingCart.removeAllOfItem(item)
                    refreshCart()
                }

                buttonRow.addView(
                    minusButton,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                buttonRow.addView(
                    plusButton,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                buttonRow.addView(
                    removeButton,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        2f
                    )
                )

                cartContainer.addView(buttonRow)
            }

            val totalPence = ShoppingCart.getTotalPence()

            totalText.text = "Total: ${formatPrice(totalPence)}"

            // Enable ordering only when the basket contains products
            placeOrderButton.isEnabled =
                selectedItems.isNotEmpty() && !isSubmitting
        }

        refreshCart()

        placeOrderButton.setOnClickListener {

            val user = auth.currentUser
            val selectedItems = ShoppingCart.getItems()

            if (isSubmitting) {
                return@setOnClickListener
            }

            if (user == null) {
                Toast.makeText(
                    this,
                    "Please log in to place an order",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (selectedItems.isEmpty()) {
                Toast.makeText(
                    this,
                    "Your basket is empty",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val orderItems = selectedItems.map { (item, quantity) ->
                mapOf(
                    "productId" to item.id,
                    "name" to item.name,
                    "quantity" to quantity,
                    "pricePence" to item.pricePence
                )
            }

            val order = hashMapOf(
                "userId" to user.uid,
                "items" to orderItems,
                "totalPence" to ShoppingCart.getTotalPence(),
                "status" to "Pending",
                "createdAt" to FieldValue.serverTimestamp()
            )

            isSubmitting = true
            backButton.isEnabled = false
            refreshCart()

            db.collection("users")
                .document(user.uid)
                .collection("orders")
                .add(order)
                .addOnSuccessListener {

                    ShoppingCart.clearCart()

                    isSubmitting = false
                    backButton.isEnabled = true
                    refreshCart()

                    Toast.makeText(
                        this,
                        "Order placed successfully!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnFailureListener { exception ->

                    isSubmitting = false
                    backButton.isEnabled = true
                    refreshCart()

                    Toast.makeText(
                        this,
                        "Order failed: ${exception.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        backButton.setOnClickListener {
            if (!isSubmitting) {
                finish()
            }
        }
    }
}
