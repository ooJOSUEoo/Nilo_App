package com.example.nilo.product

import com.example.nilo.entities.Product

//(3)
interface OnProductListener { //interfaz de producto
    fun onClick(product: Product)
    fun onLongClick(product: Product)
}