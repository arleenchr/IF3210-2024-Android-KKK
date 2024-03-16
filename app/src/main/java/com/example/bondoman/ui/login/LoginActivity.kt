package com.example.bondoman.ui.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.bondoman.MainActivity
import com.example.bondoman.databinding.ActivityLoginBinding

import com.example.bondoman.R
import com.example.bondoman.service.RetrofitClient

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading

        var eyeDrawable = ContextCompat.getDrawable(this, R.drawable.fieye)
        eyeDrawable?.setBounds(0, 0, eyeDrawable.intrinsicWidth, eyeDrawable.intrinsicHeight)

        // Set the onTouchListener to the password EditText to handle show/hide password
        password.setOnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2

            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (password.right - password.compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                    // Toggle password visibility
                    if (password.transformationMethod == PasswordTransformationMethod.getInstance()) {
                        // Show password
                        password.transformationMethod = HideReturnsTransformationMethod.getInstance()
                        eyeDrawable = ContextCompat.getDrawable(this, R.drawable.fieyeoff)
                        password.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.filock,0,R.drawable.fieyeoff,0)
                    } else {
                        // Hide password
                        password.transformationMethod = PasswordTransformationMethod.getInstance()
                        eyeDrawable = ContextCompat.getDrawable(this, R.drawable.fieye)
                        password.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.filock,0,R.drawable.fieye,0)
                    }
                    return@setOnTouchListener true
                }
            }
            false
        }

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            if (loading != null) {
                loading.visibility = View.GONE
            }

            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }

            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
                setResult(Activity.RESULT_OK)

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

                //Complete and destroy login activity once successful
                finish()
            }
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                if (loading != null) {
                    loading.visibility = View.VISIBLE
                }
                loginViewModel.login(username.text.toString(), password.text.toString())
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}