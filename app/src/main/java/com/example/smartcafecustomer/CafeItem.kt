package com.example.smartcafecustomer

data class CafeItem(
    val id: String,
    val name: String,
    val category: String,
    val pricePence: Int,
    val isAvailable: Boolean = true
)
