package com.example.nilo.cart

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.nilo.databinding.ItemProductCartBinding
import com.example.nilo.entities.Product

class ProductCartFragment(private val productList: MutableList<Product>, //lista de productos
                          private val listener: OnCartListener) { //la interfas

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val binding = ItemProductCartBinding.bind(view)

        fun setListener(product: Product){ //eventos al los botones de mas o menos
            binding.ibSum.setOnClickListener { //evento a boton mas
                listener.setQuantity(product)
            }
            binding.ibSub.setOnClickListener { //evento al boton menos
                listener.setQuantity(product)
            }
        }
    }
}