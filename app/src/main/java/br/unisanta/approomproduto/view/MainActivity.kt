package br.unisanta.approomproduto.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import br.unisanta.approomproduto.dao.ProductDao
import br.unisanta.approomproduto.databinding.ActivityMainBinding
import br.unisanta.approomproduto.db.AppDatabase
import br.unisanta.approomproduto.model.Product
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var productDao: ProductDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        )
            .fallbackToDestructiveMigration()
            .build()
        productDao = db.productDao()

        binding.btnInserirProduto.setOnClickListener { inserirProduto() }
        binding.btnListarProdutos.setOnClickListener { listarProdutos() }
        binding.btnBuscar.setOnClickListener { buscarProdutos() }
        binding.btnAtualizarPreco.setOnClickListener { atualizarPreco() }
        binding.btnExcluirTodos.setOnClickListener { excluirTodos() }
    }

    private fun inserirProduto() {
        val name = binding.editProductName.text.toString()
        val priceText = binding.editProductPrice.text.toString()
        val description = binding.editProductDescription.text.toString()
        val quantityText = binding.editProductQuantity.text.toString()

        if (name.isEmpty()) {
            binding.editProductName.error = "Informe o nome"
            return
        }

        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0.0) {
            binding.editProductPrice.error = "Informe um preço válido"
            return
        }

        val quantity = quantityText.toIntOrNull() ?: 0
        val product = Product(
            name = name,
            price = price,
            description = description,
            quantity = quantity
        )

        lifecycleScope.launch {
            productDao.insertAll(product)
            Toast.makeText(this@MainActivity, "Produto inserido!", Toast.LENGTH_SHORT).show()

            binding.editProductName.text.clear()
            binding.editProductPrice.text.clear()
            binding.editProductDescription.text.clear()
            binding.editProductQuantity.text.clear()
            binding.editProductName.requestFocus()
        }
    }

    private fun listarProdutos() {
        lifecycleScope.launch {
            val products = productDao.getAll()
            mostrarLista(products)
        }
    }

    private fun buscarProdutos() {
        val texto = binding.editBusca.text.toString()
        lifecycleScope.launch {
            val products = productDao.buscarPorNome("%$texto%")
            mostrarLista(products)
        }
    }

    private fun mostrarLista(products: List<Product>) {
        if (products.isEmpty()) {
            binding.textResultado.text = "Nenhum produto encontrado."
            Log.i("TAG", "Nenhum produto encontrado.")
            return
        }

        val texto = StringBuilder()
        products.forEach {
            texto.append("ID ${it.id} - ${it.name}\n")
            texto.append("Preço: R$ ${it.price} | Qtd: ${it.quantity}\n")
            texto.append("Descrição: ${it.description}\n\n")
            Log.i("TAG", "Produto = ${it.name}, Preço = ${it.price}, Qtd = ${it.quantity}")
        }


        val total = products.sumOf { it.price }
        texto.append("Total de produtos: ${products.size}\n")
        texto.append("Soma dos preços: R$ ${String.format("%.2f", total)}")

        binding.textResultado.text = texto.toString()
    }

    private fun atualizarPreco() {
        val id = binding.editProductId.text.toString().toIntOrNull()
        val novoPreco = binding.editNovoPreco.text.toString().toDoubleOrNull()

        if (id == null) {
            binding.editProductId.error = "Informe o ID"
            return
        }
        if (novoPreco == null || novoPreco <= 0.0) {
            binding.editNovoPreco.error = "Informe um preço válido"
            return
        }

        lifecycleScope.launch {
            val produto = productDao.buscarPorId(id)
            if (produto == null) {
                Toast.makeText(this@MainActivity, "Produto não encontrado!", Toast.LENGTH_SHORT).show()
            } else {
                productDao.update(produto.copy(price = novoPreco))
                Toast.makeText(this@MainActivity, "Preço atualizado!", Toast.LENGTH_SHORT).show()
                binding.editProductId.text.clear()
                binding.editNovoPreco.text.clear()
            }
        }
    }

    private fun excluirTodos() {
        lifecycleScope.launch {
            productDao.deleteAll()
            binding.textResultado.text = "Todos os produtos foram excluídos."
            Toast.makeText(this@MainActivity, "Produtos excluídos!", Toast.LENGTH_SHORT).show()
        }
    }
}