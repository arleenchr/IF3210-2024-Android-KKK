package com.example.bondoman.repository

import android.net.Uri
import com.example.bondoman.data.Result
import com.example.bondoman.data.ScanDataSource
import com.example.bondoman.models.ScanResponse

class ScanRepository (val dataSource: ScanDataSource) {
    suspend fun scan(uri: Uri): Result<ScanResponse> {
        return dataSource.scan(uri)
    }
}