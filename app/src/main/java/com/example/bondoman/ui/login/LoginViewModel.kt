package com.example.bondoman.ui.login

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bondoman.BuildConfig.ENCRYPTION_KEY
import com.example.bondoman.R
import com.example.bondoman.data.Result
import com.example.bondoman.repository.LoginRepository
import android.util.Base64
import com.example.bondoman.service.RetrofitClient.sharedPreferences
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


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
                val token = result.data
                val encryptedToken = encryptToken(token)

                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString("username", username)
                editor.putString("token", encryptedToken)
                editor.apply()
                _loginResult.value = LoginResult(success = LoggedInUserView(displayName = username))
            } else if (result is Result.Error) {
                _loginResult.value = LoginResult(error = R.string.login_failed)
            }
        }
    }

    private fun encryptToken(token: String): String? {
        try {
            val keySpec = SecretKeySpec(ENCRYPTION_KEY.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            val initVectorBytes = Base64.decode(sharedPreferences.getString("init_vector", ""), Base64.DEFAULT)
            val ivParameterSpec = IvParameterSpec(initVectorBytes)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec)
            val encryptedBytes = cipher.doFinal(token.toByteArray())
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
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

    // Username validation check
    private fun isUserNameValid(username: String): Boolean {
        val maxLength = 50
        val expectedEmailPattern = "^\\w+@std\\.stei\\.itb\\.ac\\.id$".toRegex()
        return expectedEmailPattern.matches(username) && username.length <= maxLength
    }

    // Password validation check
    private fun isPasswordValid(password: String): Boolean {
        val minLength = 6
        val maxLength = 40
        return password.length in minLength..maxLength
    }
}