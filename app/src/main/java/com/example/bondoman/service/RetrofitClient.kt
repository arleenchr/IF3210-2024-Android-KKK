package com.example.bondoman.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import android.content.SharedPreferences
import android.content.Context

object RetrofitClient {
    private const val BASE_URL = "https://pbd-backend-2024.vercel.app"
    lateinit var sharedPreferences: SharedPreferences

    private val httpClient = OkHttpClient.Builder().apply {
        addInterceptor(BearerTokenInterceptor())
    }.build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }

    // Bearer token interceptor class
    private class BearerTokenInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${getToken()}")
                .build()
            return chain.proceed(request)
        }

        private fun getToken(): String {
            return sharedPreferences.getString("token", "").toString()
        }
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