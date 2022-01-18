package com.example.nilo.cart

import com.example.nilo.entities.Product

interface OnCartListener {
    fun setQuantity(product: Product) //saber numero
    fun showTotal(total: Double) //saber total
}