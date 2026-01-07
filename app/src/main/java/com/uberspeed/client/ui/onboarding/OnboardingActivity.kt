package com.uberspeed.client.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uberspeed.client.R
import com.uberspeed.client.data.local.SessionManager
import com.uberspeed.client.ui.auth.LoginActivity
import com.uberspeed.client.ui.home.HomeActivity

class OnboardingActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "OnboardingActivity"
    }
    
    private var currentStep = 0
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")
        
        try {
            setContentView(R.layout.activity_onboarding)
            Log.d(TAG, "setContentView completed")
            
            sessionManager = SessionManager(this)
            
            val title = findViewById<TextView>(R.id.title)
            val description = findViewById<TextView>(R.id.description)
            val btnNext = findViewById<Button>(R.id.btnNext)
            val btnSkip = findViewById<Button>(R.id.btnSkip)
            val btnDemoLogin = findViewById<Button>(R.id.btnDemoLogin)

            updateUI(title, description, btnNext)

            btnNext.setOnClickListener {
                if (currentStep < 2) {
                    currentStep++
                    updateUI(title, description, btnNext)
                } else {
                    finishOnboarding()
                }
            }

            btnSkip.setOnClickListener {
                finishOnboarding()
            }

            btnDemoLogin.setOnClickListener {
                loginAsDemo()
            }
            
            Log.d(TAG, "onCreate completed successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Onboarding Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUI(title: TextView, description: TextView, btnNext: Button) {
        try {
            when (currentStep) {
                0 -> {
                    title.text = getString(R.string.onboarding_title_1)
                    description.text = getString(R.string.onboarding_desc_1)
                }
                1 -> {
                    title.text = getString(R.string.onboarding_title_2)
                    description.text = getString(R.string.onboarding_desc_2)
                }
                2 -> {
                    title.text = getString(R.string.onboarding_title_3)
                    description.text = getString(R.string.onboarding_desc_3)
                    btnNext.text = getString(R.string.start)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI", e)
        }
    }

    private fun loginAsDemo() {
        try {
            Log.d(TAG, "Logging in as demo user")
            sessionManager.saveAuthToken("demo-token-12345")
            sessionManager.saveUser("Usuario Demo", "demo@uberspeed.com")
            
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error in loginAsDemo", e)
            Toast.makeText(this, "Demo Login Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun finishOnboarding() {
        try {
            Log.d(TAG, "Finishing onboarding")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error finishing onboarding", e)
            Toast.makeText(this, "Navigation Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
