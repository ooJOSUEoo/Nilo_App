package com.example.nilo.cart

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nilo.R
import com.example.nilo.databinding.ItemProductCartBinding
import com.example.nilo.entities.Product

class ProductCartAdapter(private val productList: MutableList<Product>, //lista de productos
                         private val listener: OnCartListener) : //la interfas
    RecyclerView.Adapter<ProductCartAdapter.ViewHolder>() {

    private lateinit var context: Context

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_product_cart,parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) { //poner los datos en el reciclerView
        val product = productList[position]

        holder.setListener(product)

        val root = holder.binding

        root.tvName.text = product.name
        root.tvQuantity.text = product.newQuantity.toString()

        Glide.with(context)
            .load(product.imgUrl)//de donde
            .diskCacheStrategy(DiskCacheStrategy.ALL) //con cache
            .placeholder(R.drawable.ic_access_time) //fondo si no hay imagen
            .error(R.drawable.ic_broken_image)//si hay un error con la imagen
            .centerCrop() //ajustarla
            .circleCrop()
            .into(root.imgProduct) //a donde
    }

    override fun getItemCount(): Int = productList.size

    //metodos para que el carrito sea dinamico

    fun add(product: Product){//añadir y visualizar los elementos de forma local
        if (!productList.contains(product)){//si el listado actual no contiene el producto, lo agrega
            productList.add(product) //add es añadir
            notifyItemInserted(productList.size - 1)//se añade al final
            calcTotal()
        }else{ //si existe el producto lo actualiza
            update(product)
        }
    }

    fun update(product: Product){//actualiza el producto
        val index = productList.indexOf(product) // es el id
        if (index != -1){//se actualiza
            productList.set(index,product) //set es actualizar
            notifyItemChanged(index)//se añade al final
            calcTotal()
        }
    }

    fun delete(product: Product){//elimina el producto
        val index = productList.indexOf(product) // es el id
        if (index != -1){//se actualiza
            productList.removeAt(index)//removeAt es eliminar
            notifyItemRemoved(index)//se añade al final
            calcTotal()
        }
    }

    private fun calcTotal(){
        var result = 0.0
        for (product in productList){
            result += product.totalPrice()
        }
        listener.showTotal(result)
    }
}