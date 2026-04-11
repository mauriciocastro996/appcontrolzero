package com.example.loginbiometrico.models

// ─── EMPLEADO ────────────────────────────────────────────────────────────────

data class EstadoEmpleadoResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?
)

data class EmpleadoPuestoResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?
)

data class EmpleadoResponse(
    val id: Long,
    val nombres: String,
    val apellidos: String,
    val dpi: String,
    val telefono: String?,
    val estado: EstadoEmpleadoResponse,
    val puesto: EmpleadoPuestoResponse?,
    val fechaIngreso: String,
    val fotoUrl: String?
)

// ─── USUARIO ─────────────────────────────────────────────────────────────────

data class UsuarioResponse(
    val id: Long,
    val empleadoId: Long,
    val empleadoNombre: String,
    val rolId: Long,
    val rolNombre: String,
    val email: String,
    val activo: Boolean,
    val ultimoLogin: String?
)

// ─── ROL / PERMISO ───────────────────────────────────────────────────────────

data class PermisoResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?
)

data class RolResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val permisos: List<PermisoResponse>
)

// ─── ERROR GENÉRICO ──────────────────────────────────────────────────────────

data class ApiError(
    val status: Int,
    val message: String,
    val timestamp: String?
)
