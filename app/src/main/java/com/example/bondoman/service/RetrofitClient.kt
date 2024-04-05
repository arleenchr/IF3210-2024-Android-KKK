package com.example.bondoman.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import android.content.SharedPreferences
import android.util.Base64
import com.example.bondoman.BuildConfig
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec

object RetrofitClient {
    private const val BASE_URL = "https://pbd-backend-2024.vercel.app"
    lateinit var sharedPreferences: SharedPreferences

    // Encryption key
    private const val ENCRYPTION_KEY = BuildConfig.ENCRYPTION_KEY

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
            val sharedPreferences = sharedPreferences
            val encryptedToken = sharedPreferences.getString("token", "") ?: ""
            return decryptToken(encryptedToken) ?: ""
        }

        // Decrypt token using AES in CBC mode
        private fun decryptToken(encryptedToken: String): String? {
            try {
                val keySpec = SecretKeySpec(ENCRYPTION_KEY.toByteArray(), "AES")
                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
                val initVectorBytes = Base64.decode(sharedPreferences.getString("init_vector", ""), Base64.DEFAULT)
                val ivParameterSpec = IvParameterSpec(initVectorBytes)
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec)
                val decryptedBytes = cipher.doFinal(Base64.decode(encryptedToken, Base64.DEFAULT))
                return String(decryptedBytes)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
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