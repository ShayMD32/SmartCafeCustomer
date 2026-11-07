package com.example.smartcafecustomer

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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

        val backToHomeButton =
            findViewById<Button>(R.id.buttonBackToHome)

        // Return to the customer dashboard
        backToHomeButton.setOnClickListener {
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

        // Set up category dropdown
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        categorySpinner.adapter = adapter

        // Update menu when the customer searches
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

        // Open the customer's basket
        basketButton.setOnClickListener {
            startActivity(
                Intent(this, CartActivity::class.java)
            )
        }

        displayMenu()
    }

    // Match each café product to its photograph
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

    // Convert dp to pixels for consistent image sizes
    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    // Convert prices from pence to pounds
    private fun formatPrice(pence: Int): String {
        return String.format(
            Locale.UK,
            "£%.2f",
            pence / 100.0
        )
    }

    // Display products based on search and category
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

            // Row containing product image and information
            val productRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 16.dp, 0, 8.dp)
            }

            // Display product image
            val productImage = ImageView(this).apply {
                setImageResource(getProductImage(item.name))
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = item.name

                layoutParams = LinearLayout.LayoutParams(
                    100.dp,
                    100.dp
                ).apply {
                    marginEnd = 16.dp
                }
            }

            productRow.addView(productImage)

            // Display product name and price
            val productText = TextView(this).apply {
                text = "${item.name}\n${formatPrice(item.pricePence)}"
                textSize = 18f

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            productRow.addView(productText)
            menuContainer.addView(productRow)

            // Add product to basket
            val addButton = Button(this).apply {
                text = "Add ${item.name}"
            }

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
