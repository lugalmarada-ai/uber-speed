package com.uberspeed.client.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uberspeed.client.R
import android.widget.TextView

class SplashActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "SplashActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")
        
        try {
            setContentView(R.layout.activity_splash)
            Log.d(TAG, "setContentView completed")
            
            try {
                val pInfo = packageManager.getPackageInfo(packageName, 0)
                val version = pInfo.versionName
                val tvVersion = findViewById<TextView>(R.id.tvVersion)
                tvVersion.text = "Ver. $version"
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    startActivity(Intent(this, OnboardingActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating", e)
                }
            }, 2000)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Splash Error: ${e.message}", Toast.LENGTH_LONG).show()
            
            // Emergency fallback
            try {
                startActivity(Intent(this, OnboardingActivity::class.java))
                finish()
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback also failed", e2)
            }
        }
    }
}
