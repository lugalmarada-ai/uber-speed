package com.uberspeed.client.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.SignInButton
import com.uberspeed.client.R
import com.uberspeed.client.data.local.SessionManager
import com.uberspeed.client.ui.driver.DriverHomeActivity
import com.uberspeed.client.ui.home.HomeActivity
import com.uberspeed.client.utils.Resource

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var viewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var googleAuthHelper: GoogleAuthHelper

    // Activity result launcher for Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        googleAuthHelper.handleSignInResult(
            result.data,
            onSuccess = { account ->
                Log.d(TAG, "Google Sign-In success: ${account.email}")
                // Save user info from Google account
                sessionManager.saveUser(
                    name = account.displayName ?: "Usuario",
                    email = account.email ?: "",
                    role = "user", // Default role for Google sign-in
                    userId = account.id ?: ""
                )
                // Save a placeholder token (in production, exchange for backend token)
                sessionManager.saveAuthToken(account.idToken ?: "google_auth")
                
                Toast.makeText(this, "✅ Bienvenido ${account.displayName}!", Toast.LENGTH_SHORT).show()
                navigateToHome()
            },
            onError = { error ->
                Log.e(TAG, "Google Sign-In failed: $error")
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        sessionManager = SessionManager(this)
        googleAuthHelper = GoogleAuthHelper(this)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val btnGoogleSignIn = findViewById<SignInButton>(R.id.btnGoogleSignIn)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                Log.d(TAG, "Attempting login for: $email")
                viewModel.login(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        btnGoogleSignIn.setOnClickListener {
            Log.d(TAG, "Starting Google Sign-In")
            googleSignInLauncher.launch(googleAuthHelper.getSignInIntent())
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show loading if needed
                }
                is Resource.Success -> {
                    val response = resource.data
                    if (response != null && response.success) {
                        response.token?.let { sessionManager.saveAuthToken(it) }
                        response.user?.let { user ->
                            sessionManager.saveUser(
                                name = user.name,
                                email = user.email,
                                role = user.role,
                                userId = user.id
                            )
                        }
                        
                        // Navigate based on user role
                        navigateToHome()
                    } else {
                        Toast.makeText(this, response?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToHome() {
        val intent = if (sessionManager.isDriver()) {
            Log.d(TAG, "Navigating to DriverHomeActivity")
            Intent(this, DriverHomeActivity::class.java)
        } else {
            Log.d(TAG, "Navigating to HomeActivity")
            Intent(this, HomeActivity::class.java)
        }
        startActivity(intent)
        finishAffinity()
    }
}


