package com.example.bondoman.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://pbd-backend-2024.vercel.app"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

object ApiClient {
    val authService: AuthService by lazy {
        RetrofitClient.retrofit.create(AuthService::class.java)
    }

    val fileService: FileService by lazy {
        RetrofitClient.retrofit.create(FileService::class.java)
    }
}