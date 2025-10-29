package com.example.reproductormusic

import android.content.Intent
import android.os.Bundle
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

// Asumo que tu activity de login se llama LoginActivity y usa el layout activity_main.xml
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Variables de Google Sign-In y Firebase
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    // Código de solicitud para la actividad de Google Sign-In
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Inicializar FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // 2. Configurar Google Sign In
        // Nota: Asegúrate de que R.string.default_web_client_id exista (generado por el archivo JSON de Firebase)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Lógica del Botón Principal de Login
        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        // 3. Conectar el botón de Google (asumo ID: btn_google_login)
        binding.btnGoogleLogin.setOnClickListener {
            signInWithGoogle()
        }

        // Lógica de Navegación a Registro (simulada)
        // Reemplaza tvRegisterLink con el ID de tu TextView para registro
        binding.tvRegisterLink.setOnClickListener {
            Toast.makeText(this, "Navegando a la pantalla de Registro...", Toast.LENGTH_SHORT).show()
        }
    }

    // --- MÉTODOS DE GOOGLE SIGN-IN ---

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
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
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

                    // TODO: Aquí va la navegación real
                    // val intent = Intent(this, HomeActivity::class.java)
                    // startActivity(intent)
                    // finish()

                } else {
                    Toast.makeText(this, "Fallo de autenticación con Firebase.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // --- MÉTODOS DE LOGIN MANUAL (Ejemplo) ---

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.etEmail.error = null
        binding.etPassword.error = null

        var cancel = false
        var focusView: android.view.View? = null

        if (password.isEmpty() || password.length < 6) {
            binding.etPassword.error = "Contraseña inválida (mín. 6 caracteres)"
            focusView = binding.etPassword
            cancel = true
        }

        if (email.isEmpty()) {
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
            // Aquí iría la autenticación real de Email/Password
            Toast.makeText(this, "Iniciando sesión manual...", Toast.LENGTH_LONG).show()
        }
    }
}