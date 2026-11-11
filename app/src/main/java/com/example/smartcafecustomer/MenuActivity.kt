package com.example.smartcafecustomer

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
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
    private lateinit var basketCountText: TextView

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
        basketCountText = findViewById(R.id.textBasketCount)

        val basketButton =
            findViewById<Button>(R.id.buttonViewBasket)

        val homeButton =
            findViewById<Button>(R.id.buttonBackToHome)

        homeButton.setOnClickListener {
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

        basketButton.setOnClickListener {
            startActivity(
                Intent(this, CartActivity::class.java)
            )
        }

        setupCategories()
        setupSearch()

        updateBasketCount()
        displayMenu()
    }

    override fun onResume() {
        super.onResume()
        if (::basketCountText.isInitialized) {
            updateBasketCount()
        }
    }

    private fun setupCategories() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        categorySpinner.adapter = adapter

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
    }

    private fun setupSearch() {
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
    }

    private fun displayMenu() {
        menuContainer.removeAllViews()

        val searchText =
            searchInput.text.toString().trim()

        val selectedCategory =
            categorySpinner.selectedItem?.toString()
                ?: "All"

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
            showEmptyState()
            return
        }

        for (item in filteredItems) {
            val productCard = LayoutInflater.from(this)
                .inflate(
                    R.layout.item_menu_product,
                    menuContainer,
                    false
                )

            val image = productCard.findViewById<ImageView>(
                R.id.imageProduct
            )

            val name = productCard.findViewById<TextView>(
                R.id.textProductName
            )

            val category = productCard.findViewById<TextView>(
                R.id.textProductCategory
            )

            val description = productCard.findViewById<TextView>(
                R.id.textProductDescription
            )

            val price = productCard.findViewById<TextView>(
                R.id.textProductPrice
            )

            val addButton = productCard.findViewById<Button>(
                R.id.buttonAddProduct
            )

            image.setImageResource(getProductImage(item.name))
            image.contentDescription = item.name

            name.text = item.name
            category.text = item.category.uppercase(Locale.UK)
            description.text = getProductDescription(item.name)

            price.text = formatPrice(item.pricePence)

// Show availability and control whether the product can be added
            if (item.isAvailable) {

                addButton.text = "Add to Basket"
                addButton.isEnabled = true
                addButton.alpha = 1.0f

            } else {

                addButton.text = "Sold Out"
                addButton.isEnabled = false
                addButton.alpha = 0.4f
            }

// Only available products can be added
            addButton.setOnClickListener {

                if (!item.isAvailable) {
                    Toast.makeText(
                        this,
                        "${item.name} is currently sold out.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                ShoppingCart.addItem(item)

                val quantity = ShoppingCart.getQuantity(item)

                updateBasketCount()

                Toast.makeText(
                    this,
                    "${item.name} added! Quantity: $quantity",
                    Toast.LENGTH_SHORT
                ).show()
            }


            menuContainer.addView(productCard)
        }
    }

    private fun showEmptyState() {
        val emptyText = TextView(this).apply {
            text = "No matching products found.\nTry another search or category."
            textSize = 16f
            setTextColor(
                android.graphics.Color.parseColor("#75665E")
            )
            gravity = android.view.Gravity.CENTER
            setPadding(16.dp, 40.dp, 16.dp, 40.dp)
        }

        menuContainer.addView(emptyText)
    }

    private fun updateBasketCount() {
        val count = CafeMenuData.items.fold(0) { total, item ->
            total + ShoppingCart.getQuantity(item)
        }

        basketCountText.text = "Basket: $count"
    }

    private fun getProductDescription(
        productName: String
    ): String {
        return when (productName) {
            "Cappuccino" ->
                "Rich espresso with a smooth, foamy finish."

            "Flat White" ->
                "Bold espresso with silky steamed milk."

            "Hot Chocolate" ->
                "A warming, chocolatey café favourite."

            "Iced Latte" ->
                "Smooth coffee served refreshingly cold."

            "Iced Matcha" ->
                "A refreshing, earthy green tea drink."

            "Chicken Panini" ->
                "A warm, savoury toasted panini."

            "Cheese Toastie" ->
                "A comforting toasted cheese favourite."

            "Blueberry Muffin" ->
                "A soft, sweet blueberry treat."

            else ->
                "Freshly prepared café favourite."
        }
    }

    private fun getProductImage(
        productName: String
    ): Int {
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

    private val Int.dp: Int
        get() = (
                this * resources.displayMetrics.density
                ).toInt()

    private fun formatPrice(pence: Int): String {
        return String.format(
            Locale.UK,
            "£%.2f",
            pence / 100.0
        )
    }
}
