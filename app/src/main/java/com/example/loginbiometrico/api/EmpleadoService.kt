package com.example.loginbiometrico.api

import com.example.loginbiometrico.models.EmpleadoPuestoResponse
import com.example.loginbiometrico.models.EmpleadoResponse
import com.example.loginbiometrico.models.EstadoEmpleadoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface EmpleadoService {

    @GET("api/v1/empleados")
    suspend fun getEmpleados(): Response<List<EmpleadoResponse>>

    @GET("api/v1/empleados/{id}")
    suspend fun getEmpleadoById(
        @Path("id") id: Long
    ): Response<EmpleadoResponse>

    @GET("api/v1/empleados/estados")
    suspend fun getEstados(): Response<List<EstadoEmpleadoResponse>>

    @GET("api/v1/empleados/puestos")
    suspend fun getPuestos(): Response<List<EmpleadoPuestoResponse>>
}
