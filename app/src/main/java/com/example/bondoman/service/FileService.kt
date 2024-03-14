package com.example.bondoman.service

import com.example.bondoman.models.TransactionRes
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface FileService {
    @POST("/api/bill/upload")
    fun uploadBill(@Body requestBody: RequestBody): Call<TransactionRes>
}