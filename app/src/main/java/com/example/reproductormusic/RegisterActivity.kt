package com.example.reproductormusic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.reproductormusic.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest // 🚨 IMPORTANTE: Necesario para setDisplayName

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Binding y Layout
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Ocultar la ActionBar (para diseño inmersivo)
        supportActionBar?.hide()

        // --- Manejo de Clicks ---

        // 1. Botón Crear Cuenta
        binding.btnCreateAccount.setOnClickListener {
            attemptRegistration()
        }



        // 3. Navegación a Login mediante el enlace de texto
        binding.tvLoginLink.setOnClickListener {
            finish()
        }

        // 4. Botón Atrás
        binding.ivBackArrow.setOnClickListener {
            finish()
        }
    }

    private fun attemptRegistration() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Limpiar errores previos
        binding.etName.error = null
        binding.etEmail.error = null
        binding.etPassword.error = null
        binding.etConfirmPassword.error = null

        var cancel = false
        var focusView: android.view.View? = null

        // --- 1. Validación de Campos ---

        if (confirmPassword.isEmpty() || password != confirmPassword) {
            binding.etConfirmPassword.error = "Las contraseñas no coinciden"
            focusView = binding.etConfirmPassword
            cancel = true
        } else if (password.isEmpty() || password.length < 6) {
            binding.etPassword.error = "Contraseña inválida (mín. 6 caracteres)"
            focusView = binding.etPassword
            cancel = true
        } else if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Formato de email incorrecto"
            focusView = binding.etEmail
            cancel = true
        } else if (name.isEmpty()) {
            binding.etName.error = "El nombre es requerido"
            focusView = binding.etName
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
            return // Detener la ejecución si hay errores
        }

        // --- 2. Creación de Usuario en Firebase ---
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registro exitoso
                    val user = auth.currentUser

                    // Opcional: Actualizar el nombre del usuario
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            Log.d(TAG, "Nombre de usuario actualizado con éxito.")
                        }
                    }

                    Toast.makeText(this, "¡Registro exitoso! Bienvenido, $name.", Toast.LENGTH_LONG).show()
                    navigateToHome(name)

                } else {
                    // Fallo en el registro (ej. email ya en uso)
                    val errorMessage = task.exception?.message ?: "Error desconocido al crear cuenta."
                    Log.e(TAG, "Fallo de registro: $errorMessage")
                    Toast.makeText(this, "Error al crear cuenta: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToHome(userName: String?) {
        val intent = Intent(this, HomeActivity::class.java)
        // Limpiar la pila de actividades para que el usuario no pueda volver a Login/Register con el botón atrás
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("USER_NAME", userName)
        startActivity(intent)
        // Ya no es necesario llamar a finish() aquí si se usa FLAG_ACTIVITY_CLEAR_TASK
    }
}