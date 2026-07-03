package com.mkm.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.mkm.android.MainActivity
import com.mkm.android.data.local.TOKEN_KEY
import com.mkm.android.data.local.authDataStore
import com.mkm.android.data.remote.RetrofitClient
import com.mkm.android.databinding.ActivityLoginBinding
import com.mkm.android.model.LoginRequest
import com.mkm.android.model.RegisterRequest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { doLogin() }
        binding.btnRegister.setOnClickListener { doRegister() }
    }

    private fun doLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields")
            return
        }
        setLoading(true)
        lifecycleScope.launch {
            runCatching {
                val resp = RetrofitClient.api.login(LoginRequest(username, password))
                check(resp.isSuccessful) { "Login failed: ${resp.code()}" }
                resp.body()!!
            }.onSuccess { tokenResponse ->
                authDataStore.edit { it[TOKEN_KEY] = tokenResponse.token }
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }.onFailure {
                showError(it.message ?: "Login failed")
            }
            setLoading(false)
        }
    }

    private fun doRegister() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields")
            return
        }
        setLoading(true)
        lifecycleScope.launch {
            runCatching {
                val resp = RetrofitClient.api.register(RegisterRequest(username, password))
                check(resp.isSuccessful) { "Register failed: ${resp.code()}" }
                resp.body()!!
            }.onSuccess { tokenResponse ->
                authDataStore.edit { it[TOKEN_KEY] = tokenResponse.token }
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }.onFailure {
                showError(it.message ?: "Register failed")
            }
            setLoading(false)
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
        binding.btnRegister.isEnabled = !loading
    }

    private fun showError(msg: String) =
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
}
