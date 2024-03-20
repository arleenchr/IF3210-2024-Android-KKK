package com.example.bondoman.repository

import com.example.bondoman.data.Result
import com.example.bondoman.data.ScanDataSource
import com.example.bondoman.models.ScanResponse
import java.io.File

class ScanRepository (val dataSource: ScanDataSource) {
    suspend fun scan(file: File): Result<ScanResponse> {
        return dataSource.scan(file)
    }
}