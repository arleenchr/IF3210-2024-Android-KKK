package com.example.bondoman.service

import com.example.bondoman.models.LoginResponse
import com.example.bondoman.models.TokenResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface AuthService {
    @POST("/api/auth/login")
    suspend fun login(@Body requestBody: RequestBody): Response<LoginResponse>

    @POST("/api/auth/token")
    suspend fun checkToken(): Response<TokenResponse>
}