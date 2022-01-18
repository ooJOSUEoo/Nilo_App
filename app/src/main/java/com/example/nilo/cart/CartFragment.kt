package com.example.nilo.cart

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nilo.databinding.FragmentCartBinding
import com.example.nilo.entities.Product
import com.example.nilo.product.MainAux
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CartFragment : BottomSheetDialogFragment(), OnCartListener {
    private var binding: FragmentCartBinding? = null

    private lateinit var bottomSheetBehavior :  BottomSheetBehavior<*>

    private lateinit var adapter: ProductCartAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog { //hace que aparesca el layout por abajo de la pantalla
        binding = FragmentCartBinding.inflate(LayoutInflater.from(activity))
        binding?.let {
            val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
            bottomSheetDialog.setContentView(it.root)

            bottomSheetBehavior = BottomSheetBehavior.from(it.root.parent as View)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED //hacer aparecer

            setupRecyclerView() //integra los productos
            setupButtons() //acciones de boton

            getProducts()

            return bottomSheetDialog
        }
        return super.onCreateDialog(savedInstanceState)
    }

    private fun setupRecyclerView() {
        binding?.let {
            adapter = ProductCartAdapter(mutableListOf(),this)

            it.recyclerView.apply {
                layoutManager  = LinearLayoutManager(context)
                adapter = this@CartFragment.adapter
            }

            /*(1..5).forEach {
                val product = Product(it.toString(), "Producto $it","This product is $it",
                    "", it, 2.0*it)

                adapter.add(product)
            }*/
        }
    }

    private fun setupButtons(){ //eventos para botobes
        binding?.let { //evento para boton de cancelar
            it.ibCancel.setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN //oculta
            }
        }
    }

    private fun getProducts(){ //envia los productos a la lista para la compra
        (activity as? MainAux)?.getProductsCart()?.forEach{
            adapter.add(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun setQuantity(product: Product) {

    }

    override fun showTotal(total: Double) {

    }
}