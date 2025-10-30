package com.example.reproductormusic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.reproductormusic.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider

// Asegúrate de que tu HomeActivity esté en este paquete o importarla
// import com.example.reproductormusic.HomeActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Variables de Google Sign-In y Firebase
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    // Códigos de solicitud y constantes
    private val RC_SIGN_IN = 9001
    private val TAG = "LoginActivity"

    // ID del proveedor de Apple (para Firebase OAuth)
    private val APPLE_PROVIDER_ID = "apple.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Inicializar FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // 2. Configurar Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // --- Manejo de Clicks ---

        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        binding.btnGoogleLogin.setOnClickListener {
            signInWithGoogle()
        }

        binding.btnAppleLogin.setOnClickListener {
            signInWithApple()
        }

        binding.btnPhoneLogin.setOnClickListener {
            Toast.makeText(this, "Navegando a la verificación por Teléfono (TODO)...", Toast.LENGTH_SHORT).show()
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))        }
    }

    // --- MÉTODOS DE AUTENTICACIÓN ---

    private fun signInWithApple() {
        // La implementación requiere la configuración avanzada de Apple Developer y Firebase
        val provider = OAuthProvider.newBuilder(APPLE_PROVIDER_ID)

        auth.startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener { result ->
                val user = result.user
                Log.d(TAG, "Sign In with Apple exitoso. Usuario: ${user?.uid}")
                Toast.makeText(this, "¡Bienvenido, ${user?.displayName ?: "Usuario Apple"}!", Toast.LENGTH_SHORT).show()
                // 🚨 NAVEGACIÓN CORREGIDA 🚨
                navigateToHome(user?.displayName)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Fallo de Sign In with Apple", exception)
                Toast.makeText(this, "Fallo de autenticación con Apple: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "Google Sign-In exitoso, ID Token: ${account.idToken}")
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google Sign-In falló", e)
                Toast.makeText(this, "Google Sign-In falló. Código: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "¡Bienvenido, ${user?.displayName}!", Toast.LENGTH_SHORT).show()
                    // 🚨 NAVEGACIÓN CORREGIDA 🚨
                    navigateToHome(user?.displayName)
                } else {
                    Log.e(TAG, "Fallo de autenticación con Firebase (Google): ${task.exception?.message}")
                    Toast.makeText(this, "Fallo de autenticación con Firebase.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.etEmail.error = null
        binding.etPassword.error = null

        var cancel = false
        var focusView: android.view.View? = null

        // (Validación de campos, se mantiene igual)

        if (password.isEmpty() || password.length < 6) {
            binding.etPassword.error = "Contraseña inválida (mín. 6 caracteres)"
            focusView = binding.etPassword
            cancel = true
        } else if (email.isEmpty()) {
            binding.etEmail.error = "Este campo es requerido"
            focusView = binding.etEmail
            cancel = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Formato de email incorrecto"
            focusView = binding.etEmail
            cancel = true
        }


        if (cancel) {
            focusView?.requestFocus()
        } else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Toast.makeText(this, "¡Bienvenido de vuelta, ${user?.email}!", Toast.LENGTH_SHORT).show()
                        // 🚨 NAVEGACIÓN CORREGIDA 🚨
                        navigateToHome(user?.displayName ?: user?.email?.split("@")?.get(0))
                    } else {
                        Log.e(TAG, "Fallo de autenticación (Email): ${task.exception?.message}")
                        Toast.makeText(this, "Error de credenciales. Por favor, verifica tu email y contraseña.", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    // --- FUNCIÓN DE NAVEGACIÓN CORREGIDA ---

    private fun navigateToHome(userName: String?) {
        // 1. Crea el Intent para iniciar HomeActivity
        val intent = Intent(this, HomeActivity::class.java)

        // 2. Adjunta el nombre del usuario
        intent.putExtra("USER_NAME", userName)

        // 3. Inicia la nueva Activity
        startActivity(intent)

        // 4. Cierra LoginActivity (IMPORTANTE)
        finish()
    }
}