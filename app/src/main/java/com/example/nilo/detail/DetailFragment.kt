package com.example.nilo.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nilo.R
import com.example.nilo.databinding.FragmentDetailBinding
import com.example.nilo.entities.Product
import com.example.nilo.product.MainAux

class DetailFragment : Fragment() {
    private var binding: FragmentDetailBinding? = null
    private var product: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater,container,false)
        binding?.let {
            return it.root
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getProduct()
        setupButtons()
    }

    private fun getProduct() { //obtener los datos del producto
        product = (activity as? MainAux)?.getProductSelected()
        product?.let {product ->
            binding?.let {
                it.tvName.text = product.name
                it.tvDescription.text = product.description
                it.tvQuantity.text = getString(R.string.detail_quantity,product.quantity)
                setNewQuantity(product)

                Glide.with(this)
                    .load(product.imgUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_access_time)
                    .error(R.drawable.ic_broken_image)
                    .centerCrop()
                    .into(it.imgProduct)
            }
        }
    }

    private fun setNewQuantity(product: Product) {
        binding?.let {
            it.etNewQuantity.setText(product.newQuantity.toString()) //pone el numero de la nueva cantidad en el etNew...

            val newQuantityString = getString(R.string.detail_total_price, product.totalPrice(),
                product.newQuantity,product.price) //pone el valor en el tvTotal...
            it.tvTotalPrice.text = HtmlCompat.fromHtml(newQuantityString, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }

    private fun setupButtons(){
        product?.let { product ->
            binding?.let { binding ->
                binding.ibSub.setOnClickListener { //evento para el boton de resta
                    if (product.newQuantity > 1){
                        product.newQuantity -= 1 //si la cantidad es mayor a 1 qutara 1 a la nueva cantidad
                        setNewQuantity(product)
                    }
                }
                binding.ibSum.setOnClickListener { //evento para el boton de suma
                    if (product.newQuantity < product.quantity){
                        product.newQuantity += 1 //si la cantidad es menor al limite sumara 1
                        setNewQuantity(product)
                    }
                }
                binding.efab.setOnClickListener {
                    product.newQuantity =  binding.etNewQuantity.text.toString().toInt()
                    addToCart(product)
                }
            }
        }
    }

    private fun addToCart(product: Product) {
        (activity as? MainAux)?.let {
            it.addProductToCart(product)
            activity?.onBackPressed()
        }
    }

    override fun onDestroyView() {
        (activity as? MainAux)?.showButton(true) //mostrar el boton de carrito
        super.onDestroyView()
        binding = null
    }
}