package com.example.bondoman

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.bondoman.service.RetrofitClient
import android.content.Context
import android.content.SharedPreferences
import com.example.bondoman.service.ApiClient
import com.example.bondoman.ui.login.LoginActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Hide action bar
        supportActionBar?.hide()

        // Set shared preferences
        RetrofitClient.sharedPreferences = getSharedPreferences("identity", Context.MODE_PRIVATE)

        // Check if new user or not
        val checkNew = RetrofitClient.sharedPreferences.getBoolean("new", true)
        if (checkNew) {
            val editor: SharedPreferences.Editor = RetrofitClient.sharedPreferences.edit()

            // Set old user
            editor.putBoolean("new", false)
            editor.apply()

            // Throw to intro activity
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, IntroActivity::class.java)
                startActivity(intent)
                finish()
            }, 3000)
        } else {
            GlobalScope.launch(Dispatchers.Main) {
                if (isTokenValid()) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this@SplashActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, 3000)
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, 3000)
                }
            }
        }
    }

    private suspend fun isTokenValid(): Boolean {
        // Retrieve the token from SharedPreferences
        val token = RetrofitClient.sharedPreferences.getString("token", "")

        // Check if the token is not null or empty (You may also need to validate token expiration)
        if (!token.isNullOrEmpty()) {
            val response = ApiClient.authService.checkToken()

            if (!response.isSuccessful) {
                val editor: SharedPreferences.Editor = RetrofitClient.sharedPreferences.edit()

                // Remove token
                editor.putString("token", "")
                editor.apply()

                return false
            }

            return true
        }

        return false
    }
}