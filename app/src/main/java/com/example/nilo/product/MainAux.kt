package com.example.nilo.product

import com.example.nilo.entities.Product

interface MainAux {
    fun getProductsCart(): MutableList<Product>
}