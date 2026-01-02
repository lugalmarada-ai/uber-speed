package com.uberspeed.client.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.uberspeed.client.R
import com.uberspeed.client.data.local.SessionManager
import com.uberspeed.client.ui.auth.LoginActivity
import com.uberspeed.client.ui.home.HomeActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val sessionManager = SessionManager(this)
            val token = sessionManager.getAuthToken()

            if (token != null) {
                // User logged in, go to Home
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                // Not logged in, go to Onboarding
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
            finish()
        }, 2000) // 2 seconds delay
    }
}
