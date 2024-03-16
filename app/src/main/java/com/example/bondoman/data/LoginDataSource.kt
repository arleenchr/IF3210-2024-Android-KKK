package com.example.bondoman.data

import com.example.bondoman.service.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.await

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val jsonParams = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            val json = jsonParams.toString()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toRequestBody(mediaType)

            val response = ApiClient.authService.login(requestBody)

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    val token = responseBody.token
                    Result.Success(token)
                } else {
                    Result.Error(Exception("Empty response body"))
                }
            } else {
                Result.Error(Exception("Login failed"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }



    suspend fun logout() {
        // TODO: revoke authentication
    }
}