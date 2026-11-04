package com.example.smartcafecustomer

object ShoppingCart {

    private val quantities = mutableMapOf<String, Int>()


    fun getItems(): List<Pair<CafeItem, Int>> {
        return CafeMenuData.items.mapNotNull { item ->
            val quantity = quantities[item.id] ?: 0

            if (quantity > 0) {
                item to quantity
            } else {
                null
            }
        }
    }


    fun addItem(item: CafeItem) {
        val currentQuantity = quantities[item.id] ?: 0
        quantities[item.id] = currentQuantity + 1
    }

    fun removeItem(item: CafeItem) {
        val currentQuantity = quantities[item.id] ?: 0

        if (currentQuantity > 1) {
            quantities[item.id] = currentQuantity - 1
        } else {
            quantities.remove(item.id)
        }
    }

    fun removeAllOfItem(item: CafeItem) {
        quantities.remove(item.id)
    }


    fun getQuantity(item: CafeItem): Int {
        return quantities[item.id] ?: 0
    }

    fun getTotalPence(): Int {
        return CafeMenuData.items.sumOf { item ->
            item.pricePence * (quantities[item.id] ?: 0)
        }
    }

    fun clearCart() {
        quantities.clear()
    }
}
