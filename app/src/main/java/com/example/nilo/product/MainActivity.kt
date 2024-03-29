package com.example.nilo.product

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.example.nilo.Constants
import com.example.nilo.R
import com.example.nilo.cart.CartFragment
import com.example.nilo.databinding.ActivityMainBinding
import com.example.nilo.detail.DetailFragment
import com.example.nilo.entities.Product
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity() , OnProductListener, MainAux {

    private lateinit var binding: ActivityMainBinding //llamar a binding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private lateinit var adapter: ProductAdapter //la clase para crud

    private lateinit var firestoreListener: ListenerRegistration

    private var productSelected: Product? = null
    private val  productCartList = mutableListOf<Product>()

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        val response = IdpResponse.fromResultIntent(it.data)

        if (it.resultCode == RESULT_OK){ //ver si existe un usuario Autenticado
            val user = FirebaseAuth.getInstance().currentUser //variable para el user Auth
            if (user != null){
                Toast.makeText(this,"Bienvenido.", Toast.LENGTH_SHORT).show()
            }
        }else{
            if (response == null) { //el usuario al pulsado hacia atras
                Toast.makeText(this,"Hasta pronto.", Toast.LENGTH_SHORT).show()
                finish() //cierra la app
            }else{
                response.error?.let { //si un error no es nulo hara...
                    if (it.errorCode == ErrorCodes.NO_NETWORK) {//no hay red
                        Toast.makeText(this,"Sin red.", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"Codigo del error: ${it.errorCode}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configAuth()
        configRecyclerView()
        configButtons()
    }

    private fun configAuth(){
        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser != null){ //saber si el usuario ya esta logeado
                supportActionBar?.title = auth.currentUser?.displayName
                binding.nsvProducts.visibility = View.VISIBLE
                binding.llProgress.visibility = View.GONE
            }else{
                val providers = arrayListOf(//son los proveedores
                    AuthUI.IdpConfig.EmailBuilder().build(), //email
                    AuthUI.IdpConfig.GoogleBuilder().build())  //google

                resultLauncher.launch( //hace la instancia donde se agregan a los proveedores
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)//automaticamente te envia a la seccion de login si esta en true
                        .build())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(authStateListener)
        configFirestoreRealtime()
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(authStateListener)
        firestoreListener.remove()
    }

    private fun configRecyclerView(){ //muestra los productos
        adapter = ProductAdapter(mutableListOf(),this)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity,3,
                GridLayoutManager.HORIZONTAL,false)
            adapter = this@MainActivity.adapter
        }

    }

    private fun configButtons(){ //al hacer click ejecuta la clase de CartFragment
        binding.btnViewCart.setOnClickListener {
            val fragment = CartFragment()
            fragment.show(supportFragmentManager.beginTransaction(),CartFragment::class.java.simpleName)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_sign_out -> {//cerrar la sesión
                AuthUI.getInstance().signOut(this)
                    .addOnSuccessListener {
                        Toast.makeText(this,"Sesión terminada.",Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful){//si es exitoso la vista se quitara
                            binding.nsvProducts.visibility = View.GONE
                            binding.llProgress.visibility = View.VISIBLE
                        }else{
                            Toast.makeText(this,"No se pudo cerrar la sesión.",Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun configFirestoreRealtime(){
        val db = FirebaseFirestore.getInstance()
        val productRef = db.collection(Constants.COLL_PRODUCTS)

        firestoreListener = productRef.addSnapshotListener{ snapshots, error ->
            if (error != null){ //si hay un error...
                Toast.makeText(this,"Error al consultar datos.",Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            for (snapshot in snapshots!!.documentChanges){
                val product = snapshot.document.toObject(Product::class.java)
                product.id = snapshot.document.id //el id del producto sera el mismo que el documento
                when(snapshot.type){
                    DocumentChange.Type.ADDED -> adapter.add(product) //añade y se muestra el resultado en tiempo real
                    DocumentChange.Type.MODIFIED -> adapter.update(product) //lo mismo pero en actualizar
                    DocumentChange.Type.REMOVED -> adapter.delete(product) //""""
                }
            }
        }
    }

    override fun onClick(product: Product) {
        val index = productCartList.indexOf(product)
        if (index != -1){
            productSelected = productCartList[index]
        }else {
            productSelected = product
        }

        val fragment = DetailFragment()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.containerMain,fragment)
            .addToBackStack(null)
            .commit()

        showButton(false) //ocultar boton de carrito
    }

    override fun getProductsCart(): MutableList<Product> = productCartList //obtener los productos de la main activity

    override fun getProductSelected(): Product? = productSelected

    override fun showButton(isVisible: Boolean) { // si es true el boton de ver carrioto se va a ver, de lo contrario no
        binding.btnViewCart.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun addProductToCart(product: Product) { //agragar el producto al carrito
        val index = productCartList.indexOf(product)
        if (index != -1){
            productCartList.set(index, product) //actualiza la cantidad
        }else {
            productCartList.add(product) //añade una cantidad nueva
        }

        updateTotal()
    }

    override fun updateTotal() {
        var total = 0.0
        productCartList.forEach {product -> //la suma de todos los productos del carrito
            total += product.totalPrice()
        }

        if (total == 0.0){ //si el total es 0 va a decir carrito vacio
            binding.tvTotal.text = getString(R.string.product_empty_cart)
        }else{
            binding.tvTotal.text = getString(R.string.product_full_cart, total)
        }
    }

    override fun clearCart() {
        productCartList.clear()
    }
}