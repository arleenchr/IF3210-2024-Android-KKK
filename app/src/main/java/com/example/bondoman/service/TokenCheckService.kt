package com.example.bondoman.service

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import com.example.bondoman.data.LoginDataSource
import com.example.bondoman.repository.LoginRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TokenCheckService : Service() {
    private var serviceScope = CoroutineScope(Dispatchers.Main)
    private var isServiceRunning = true

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            while (isServiceRunning) {
                checkToken()
                delay(300000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        serviceScope.cancel()
    }

    private suspend fun checkToken() {
        val sharedPreferences = applicationContext.getSharedPreferences("identity", MODE_PRIVATE)
        val token = sharedPreferences.getString("token", "")

        if (!token.isNullOrEmpty()) {
            val response = ApiClient.authService.checkToken()

            if (!response.isSuccessful) {
                val loginRepo = LoginRepository(LoginDataSource(), applicationContext)
                val editor: SharedPreferences.Editor = RetrofitClient.sharedPreferences.edit()

                // Remove token
                editor.putString("token", "")
                editor.apply()

                // Logout
                loginRepo.logout()
            }
        }
    }
}
