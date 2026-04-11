package com.example.loginbiometrico.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    /**
     * Cuando corres la API en tu PC y usas el EMULADOR de Android Studio,
     * 10.0.2.2 apunta a localhost de tu máquina.
     *
     * Si usas un dispositivo FÍSICO conectado por USB o WiFi, cambia la IP
     * por la dirección local de tu PC (ej. "192.168.1.X").
     *
     * Puerto: 7070 (definido en application.properties del backend)
     */
    private const val BASE_URL = "http://10.0.2.2:7070/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun buildClient(token: String? = null): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)

        if (token != null) {
            builder.addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
        }

        return builder.build()
    }

    // Retrofit sin token (login / register)
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(buildClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Retrofit con token JWT (endpoints protegidos)
    fun authenticatedRetrofit(token: String): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(buildClient(token))
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // ─── Instancias de servicios ───────────────────────────────────────────

    val authService: AuthService = retrofit.create(AuthService::class.java)

    fun empleadoService(token: String): EmpleadoService =
        authenticatedRetrofit(token).create(EmpleadoService::class.java)
}