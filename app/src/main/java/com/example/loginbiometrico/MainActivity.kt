
package com.example.loginbiometrico

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.loginbiometrico.api.ApiClient
import com.example.loginbiometrico.models.LoginRequest
import com.example.loginbiometrico.utils.TokenManager
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var etUsuario: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnIngresar: Button
    private lateinit var btnBiometrico: Button
    private lateinit var btnGoogleLogin: Button
    private lateinit var btnFacebookLogin: Button
    private lateinit var tvCrearUsuario: TextView
    private lateinit var tvOlvido: TextView
    private lateinit var tvErrorMessage: TextView
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tokenManager = TokenManager(this)
        
        // Check if user is already logged in
        if (tokenManager.isLoggedIn()) {
            navigateToHome()
            return
        }

        initializeViews()
        setupValidation()
        setupEventListeners()
    }

    private fun initializeViews() {
        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        btnIngresar = findViewById(R.id.btnIngresar)
        btnBiometrico = findViewById(R.id.btnBiometrico)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        btnFacebookLogin = findViewById(R.id.btnFacebookLogin)
        tvCrearUsuario = findViewById(R.id.tvCrearUsuario)
        tvOlvido = findViewById(R.id.tvOlvido)
        tvErrorMessage = findViewById(R.id.tvErrorMessage)
    }

    private fun setupValidation() {
        // Text change listeners for real-time validation
        etUsuario.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                clearError()
            }
        })

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                clearError()
            }
        })
    }

    private fun setupEventListeners() {
        // BOTÓN INGRESAR
        btnIngresar.setOnClickListener {
            if (validateLoginForm()) {
                performLogin()
            }
        }

        // BOTÓN BIOMÉTRICO
        btnBiometrico.setOnClickListener {
            Toast.makeText(this, "Ingreso con Huella / Face ID", Toast.LENGTH_SHORT).show()
            // TODO: Implement biometric authentication
        }

        // BOTÓN GOOGLE LOGIN
        btnGoogleLogin.setOnClickListener {
            Toast.makeText(this, "Iniciando sesión con Google...", Toast.LENGTH_SHORT).show()
            // TODO: Implement Google Sign-In
            // POST /api/auth/google with Google token
            println("API CALL: POST /api/auth/google")
        }

        // BOTÓN FACEBOOK LOGIN
        btnFacebookLogin.setOnClickListener {
            Toast.makeText(this, "Iniciando sesión con Facebook...", Toast.LENGTH_SHORT).show()
            // TODO: Implement Facebook Login
            // POST /api/auth/facebook with Facebook token
            println("API CALL: POST /api/auth/facebook")
        }

        // CREAR USUARIO
        tvCrearUsuario.setOnClickListener {
            val intent = Intent(this@MainActivity, RegistroActivity::class.java)
            startActivity(intent)
        }

        // OLVIDÓ CONTRASEÑA
        tvOlvido.setOnClickListener {
            Toast.makeText(this, "Recuperar contraseña", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to password recovery
            // POST /api/auth/password-recovery with email
            println("API CALL: POST /api/auth/password-recovery")
        }
    }

    private fun performLogin() {
        val email = etUsuario.text.toString().trim()
        val password = etPassword.text.toString().trim()
        
        btnIngresar.isEnabled = false
        btnIngresar.text = "Iniciando sesión..."
        
        lifecycleScope.launch {
            try {
                val loginRequest = LoginRequest(email, password)
                val response = ApiClient.authService.login(loginRequest)
                
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    
                    // Save tokens
                    tokenManager.saveTokens(
                        authResponse.sessionToken,
                        authResponse.refreshToken
                    )
                    
                    // Save user info (for now, we'll save basic info)
                    tokenManager.saveUserInfo(
                        email,
                        0, // TODO: Get from API response
                        "ADMINISTRADOR" // TODO: Get from API response
                    )
                    
                    Toast.makeText(
                        this@MainActivity,
                        "Inicio de sesión exitoso",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    navigateToHome()
                    
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Credenciales incorrectas"
                        404 -> "Usuario no encontrado"
                        500 -> "Error del servidor"
                        else -> "Error al iniciar sesión: ${response.code()}"
                    }
                    showError(errorMessage)
                }
                
            } catch (e: IOException) {
                showError("Error de conexión. Verifique su red.")
                println("Network error: ${e.message}")
            } catch (e: HttpException) {
                showError("Error del servidor: ${e.code()}")
                println("HTTP error: ${e.message}")
            } catch (e: Exception) {
                showError("Error inesperado: ${e.message}")
                println("Unexpected error: ${e.message}")
            } finally {
                btnIngresar.isEnabled = true
                btnIngresar.text = "INGRESAR"
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this@MainActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun validateLoginForm(): Boolean {
        val email = etUsuario.text.toString().trim()
        val password = etPassword.text.toString().trim()

        when {
            email.isEmpty() -> {
                showError("Por favor ingrese su correo electrónico")
                etUsuario.requestFocus()
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError("Por favor ingrese un correo válido")
                etUsuario.requestFocus()
                return false
            }
            password.isEmpty() -> {
                showError("Por favor ingrese su contraseña")
                etPassword.requestFocus()
                return false
            }
            password.length < 6 -> {
                showError("La contraseña debe tener al menos 6 caracteres")
                etPassword.requestFocus()
                return false
            }
            else -> return true
        }
    }

    private fun showError(message: String) {
        tvErrorMessage.text = message
        tvErrorMessage.visibility = TextView.VISIBLE
    }

    private fun clearError() {
        tvErrorMessage.text = ""
        tvErrorMessage.visibility = TextView.GONE
    }
}
