package com.example.reproductormusic

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.reproductormusic.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Ocultar la barra de acción (ActionBar)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)

        // Inicialización de ViewBinding
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Opcional: Obtener el nombre del usuario si se pasó desde LoginActivity
        val userName = intent.getStringExtra("USER_NAME")
        if (userName != null) {
            // Asegúrate de que este ID (tvUserName) exista en tu XML
            binding.tvUserName.text = userName
        }

        // 🚨 LÓGICA DE CLIC CORREGIDA 🚨
        // Usamos binding.etSearch, que es el ID del EditText de búsqueda en tu activity_home.xml.
        binding.etSearch.setOnClickListener {
            // Creamos un Intent para ir a la SearchActivity
            val intent = Intent(this, SearchActivity::class.java)
            // Iniciamos la nueva actividad
            startActivity(intent)
        }
    }
}