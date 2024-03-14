package com.example.bondoman.service

import com.example.bondoman.models.TransactionRes
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface AuthService {
    @POST("/api/auth/login")
    fun login(@Body requestBody: RequestBody): Call<String>

    @POST("/api/auth/token")
    fun token(token: String): Call<String>
}