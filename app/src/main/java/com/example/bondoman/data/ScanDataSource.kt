package com.example.bondoman.data

import android.net.Uri
import com.example.bondoman.models.ScanResponse
import com.example.bondoman.service.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.lang.Exception

class ScanDataSource {
    suspend fun scan(photoUri: Uri): Result<ScanResponse> {
        return try {
            // Convert Uri to File
            val file = File(photoUri.path!!)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                )
                .build()

            val response = ApiClient.fileService.uploadBill(requestBody)

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    Result.Success(responseBody)
                } else {
                    Result.Error(Exception("Empty response body"))
                }
            } else {
                Result.Error(Exception(response.errorBody().toString()))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }
}
