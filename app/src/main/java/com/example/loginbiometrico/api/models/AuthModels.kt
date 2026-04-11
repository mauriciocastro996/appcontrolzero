package com.example.loginbiometrico.models

// ─── AUTH ────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val nombres: String,
    val apellidos: String,
    val dpi: String,
    val telefono: String? = null,
    val empleadoPuestoId: Long? = null,
    val estadoEmpleadoId: Long,
    val fechaIngreso: String,
    val fotoUrl: String? = null,
    val rolId: Long,
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class AuthTokensResponse(
    val sessionToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresInSeconds: Long
)

data class AuthUserResponse(
    val usuarioId: Long,
    val empleadoId: Long,
    val email: String,
    val rol: String
)

data class RegisterResponse(
    val usuario: AuthUserResponse
)
