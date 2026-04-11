package com.example.loginbiometrico.api

import com.example.loginbiometrico.models.AuthTokensResponse
import com.example.loginbiometrico.models.LoginRequest
import com.example.loginbiometrico.models.RefreshTokenRequest
import com.example.loginbiometrico.models.RegisterRequest
import com.example.loginbiometrico.models.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthTokensResponse>

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<AuthTokensResponse>
}
