package com.example.loginbiometrico

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegistroActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etNuevoUsuario: EditText
    private lateinit var etNuevaPassword: EditText
    private lateinit var etConfirmarPassword: EditText
    private lateinit var btnRegistrarCuenta: Button
    private lateinit var btnVolverLogin: Button
    private lateinit var btnGoogleRegister: Button
    private lateinit var btnFacebookRegister: Button
    private lateinit var tvErrorMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        initializeViews()
        setupValidation()
        setupEventListeners()
    }

    private fun initializeViews() {
        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreo)
        etNuevoUsuario = findViewById(R.id.etNuevoUsuario)
        etNuevaPassword = findViewById(R.id.etNuevaPassword)
        etConfirmarPassword = findViewById(R.id.etConfirmarPassword)
        btnRegistrarCuenta = findViewById(R.id.btnRegistrarCuenta)
        btnVolverLogin = findViewById(R.id.btnVolverLogin)
        btnGoogleRegister = findViewById(R.id.btnGoogleRegister)
        btnFacebookRegister = findViewById(R.id.btnFacebookRegister)
        tvErrorMessage = findViewById(R.id.tvErrorMessage)
    }

    private fun setupValidation() {
        // Clear errors when user starts typing
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                clearError()
            }
        }

        etNombre.addTextChangedListener(textWatcher)
        etCorreo.addTextChangedListener(textWatcher)
        etNuevoUsuario.addTextChangedListener(textWatcher)
        etNuevaPassword.addTextChangedListener(textWatcher)
        etConfirmarPassword.addTextChangedListener(textWatcher)
    }

    private fun setupEventListeners() {
        btnRegistrarCuenta.setOnClickListener {
            if (validateRegistrationForm()) {
                // TODO: Implement actual registration API call
                // POST /api/auth/register with user data
                println("API CALL: POST /api/auth/register")
                println("Name: ${etNombre.text}")
                println("Email: ${etCorreo.text}")
                println("Username: ${etNuevoUsuario.text}")
                println("Password: [HIDDEN]")
                
                Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        btnVolverLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnGoogleRegister.setOnClickListener {
            Toast.makeText(this, "Registrando con Google...", Toast.LENGTH_SHORT).show()
            // TODO: Implement Google Sign-In for registration
            // POST /api/auth/google-register with Google token
            println("API CALL: POST /api/auth/google-register")
        }

        btnFacebookRegister.setOnClickListener {
            Toast.makeText(this, "Registrando con Facebook...", Toast.LENGTH_SHORT).show()
            // TODO: Implement Facebook Login for registration
            // POST /api/auth/facebook-register with Facebook token
            println("API CALL: POST /api/auth/facebook-register")
        }
    }

    private fun validateRegistrationForm(): Boolean {
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val usuario = etNuevoUsuario.text.toString().trim()
        val password = etNuevaPassword.text.toString().trim()
        val confirmPassword = etConfirmarPassword.text.toString().trim()

        when {
            nombre.isEmpty() -> {
                showError("Por favor ingrese su nombre completo")
                etNombre.requestFocus()
                return false
            }
            nombre.length < 3 -> {
                showError("El nombre debe tener al menos 3 caracteres")
                etNombre.requestFocus()
                return false
            }
            correo.isEmpty() -> {
                showError("Por favor ingrese su correo electrónico")
                etCorreo.requestFocus()
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {
                showError("Por favor ingrese un correo válido")
                etCorreo.requestFocus()
                return false
            }
            usuario.isEmpty() -> {
                showError("Por favor ingrese un nombre de usuario")
                etNuevoUsuario.requestFocus()
                return false
            }
            usuario.length < 3 -> {
                showError("El usuario debe tener al menos 3 caracteres")
                etNuevoUsuario.requestFocus()
                return false
            }
            password.isEmpty() -> {
                showError("Por favor ingrese una contraseña")
                etNuevaPassword.requestFocus()
                return false
            }
            password.length < 6 -> {
                showError("La contraseña debe tener al menos 6 caracteres")
                etNuevaPassword.requestFocus()
                return false
            }
            confirmPassword.isEmpty() -> {
                showError("Por favor confirme su contraseña")
                etConfirmarPassword.requestFocus()
                return false
            }
            password != confirmPassword -> {
                showError("Las contraseñas no coinciden")
                etConfirmarPassword.requestFocus()
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

