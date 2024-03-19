package com.example.bondoman.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bondoman.R
import com.example.bondoman.data.Result
import com.example.bondoman.repository.LoginRepository
import com.example.bondoman.room.TransactionDatabase
import com.example.bondoman.service.RetrofitClient.sharedPreferences
import kotlinx.coroutines.launch


class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    @SuppressLint("CommitPrefEdits")
    fun login(username: String, password: String) {
        viewModelScope.launch {
            val result = loginRepository.login(username, password)

            if (result is Result.Success) {
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("username", username)
                editor.putString("token", result.data)
                editor.apply()
                _loginResult.value = LoginResult(success = LoggedInUserView(displayName = username))
            } else if (result is Result.Error) {
                val errorMessage = result.exception.message ?: "Unknown error"
                _loginResult.value = LoginResult(error = R.string.login_failed)
            }
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}