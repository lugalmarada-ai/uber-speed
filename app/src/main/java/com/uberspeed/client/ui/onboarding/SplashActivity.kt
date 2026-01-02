package com.uberspeed.client.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.uberspeed.client.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_splash)

            Handler(Looper.getMainLooper()).postDelayed({
                // Always go to Onboarding for demo
                startActivity(Intent(this, OnboardingActivity::class.java))
                finish()
            }, 2000)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback - go directly to onboarding
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }
    }
}
