package com.example.smartcafecustomer

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.AdapterView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MenuActivity : AppCompatActivity() {

    private lateinit var menuContainer: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var categorySpinner: Spinner

    private val categories = listOf(
        "All",
        "Hot Drinks",
        "Cold Drinks",
        "Food"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        menuContainer = findViewById(R.id.menuContainer)
        searchInput = findViewById(R.id.editSearchMenu)
        categorySpinner = findViewById(R.id.spinnerCategory)

        val basketButton =
            findViewById<Button>(R.id.buttonViewBasket)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        categorySpinner.adapter = adapter

        // Update menu when the customer types
        searchInput.addTextChangedListener(
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
                    displayMenu()
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {}
            }
        )

        // Update menu when category changes
        categorySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    displayMenu()
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {}
            }

        basketButton.setOnClickListener {
            startActivity(
                Intent(this, CartActivity::class.java)
            )
        }

        displayMenu()
    }

    private fun formatPrice(pence: Int): String {
        return String.format(
            Locale.UK,
            "£%.2f",
            pence / 100.0
        )
    }

    private fun displayMenu() {

        menuContainer.removeAllViews()

        val searchText =
            searchInput.text.toString().trim()

        val selectedCategory =
            categorySpinner.selectedItem?.toString() ?: "All"

        val filteredItems = CafeMenuData.items.filter { item ->

            val matchesSearch = item.name.contains(
                searchText,
                ignoreCase = true
            )

            val matchesCategory =
                selectedCategory == "All" ||
                        item.category == selectedCategory

            matchesSearch && matchesCategory
        }

        if (filteredItems.isEmpty()) {
            val emptyText = TextView(this)
            emptyText.text = "No products found."
            emptyText.textSize = 16f
            menuContainer.addView(emptyText)
            return
        }

        for (item in filteredItems) {

            val productText = TextView(this)

            productText.text =
                "${item.name} — ${formatPrice(item.pricePence)}"

            productText.textSize = 18f
            productText.setPadding(0, 20, 0, 8)

            menuContainer.addView(productText)

            val addButton = Button(this)
            addButton.text = "Add ${item.name}"

            addButton.setOnClickListener {

                ShoppingCart.addItem(item)

                val quantity =
                    ShoppingCart.getQuantity(item)

                Toast.makeText(
                    this,
                    "${item.name} added! Quantity: $quantity",
                    Toast.LENGTH_SHORT
                ).show()
            }

            menuContainer.addView(addButton)
        }
    }
}
