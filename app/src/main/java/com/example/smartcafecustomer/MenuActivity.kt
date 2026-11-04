package com.example.smartcafecustomer

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import android.content.Intent

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)


        val viewBasketButton = findViewById<Button>(R.id.buttonViewBasket)

        viewBasketButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }


        val menuContainer = findViewById<LinearLayout>(R.id.menuContainer)

        for (item in CafeMenuData.items) {
            val itemText = TextView(this)

            val addButton = Button(this)
            addButton.text = "Add ${item.name}"

            addButton.setOnClickListener {
                ShoppingCart.addItem(item)

                val quantity = ShoppingCart.getQuantity(item)
                val total = ShoppingCart.getTotalPence() / 100.0

                Toast.makeText(
                    this,
                    "${item.name} added. Quantity: $quantity. Total: £${"%.2f".format(total)}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            menuContainer.addView(addButton)


            itemText.text =
                "${item.name} — £${"%.2f".format(item.pricePence / 100.0)}"

            itemText.textSize = 18f
            itemText.setPadding(12, 24, 12, 24)

            menuContainer.addView(itemText)
        }
    }
}
