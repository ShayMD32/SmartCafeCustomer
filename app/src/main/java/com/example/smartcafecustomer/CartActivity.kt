package com.example.smartcafecustomer

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val cartContainer =
            findViewById<LinearLayout>(R.id.cartContainer)

        val totalText =
            findViewById<TextView>(R.id.textCartTotal)

        val placeOrderButton =
            findViewById<Button>(R.id.buttonPlaceOrder)

        val backButton =
            findViewById<Button>(R.id.buttonBackToMenu)

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
                    "${item.name} x $quantity — £${"%.2f".format(subtotalPence / 100.0)}"

                productText.textSize = 18f
                productText.setPadding(12, 24, 12, 12)
                cartContainer.addView(productText)

                val buttonRow = LinearLayout(this)
                buttonRow.orientation = LinearLayout.HORIZONTAL

                val minusButton = Button(this)
                minusButton.text = "−"
                minusButton.contentDescription =
                    "Decrease quantity of ${item.name}"

                minusButton.setOnClickListener {
                    ShoppingCart.removeItem(item)
                    refreshCart()
                }

                val plusButton = Button(this)
                plusButton.text = "+"
                plusButton.contentDescription =
                    "Increase quantity of ${item.name}"

                plusButton.setOnClickListener {
                    ShoppingCart.addItem(item)
                    refreshCart()
                }

                val removeButton = Button(this)
                removeButton.text = "Remove"

                removeButton.setOnClickListener {
                    ShoppingCart.removeAllOfItem(item)
                    refreshCart()
                }

                buttonRow.addView(
                    minusButton,
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                )
                buttonRow.addView(
                    plusButton,
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                )
                buttonRow.addView(
                    removeButton,
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
                )

                cartContainer.addView(buttonRow)
            }

            val totalPence = ShoppingCart.getTotalPence()

            totalText.text =
                "Total: £${"%.2f".format(totalPence / 100.0)}"
        }

        // Display basket when the screen opens
        refreshCart()

        // Ordering is not connected yet
        placeOrderButton.isEnabled = false

        backButton.setOnClickListener {
            finish()
        }
    }
}
