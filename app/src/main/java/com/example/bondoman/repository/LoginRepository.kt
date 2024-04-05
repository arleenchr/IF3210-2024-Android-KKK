package com.example.bondoman.repository

import android.content.Context
import com.example.bondoman.data.LoginDataSource
import com.example.bondoman.data.Result

class LoginRepository(val dataSource: LoginDataSource, private val context: Context) {

    // in-memory cache of the loggedInUser object
    private var user: String? = null

    init {
        user = null
    }

    fun logout() {
        user = null
        dataSource.logout(context)
    }

    suspend fun login(username: String, password: String): Result<String> {
        // handle login
        val result = dataSource.login(username, password)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }

        return result
    }

    private fun setLoggedInUser(loggedInUser: String) {
        this.user = loggedInUser
    }
}