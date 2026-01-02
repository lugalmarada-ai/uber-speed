package com.uberspeed.client.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.uberspeed.client.R
import com.uberspeed.client.ui.auth.LoginActivity

class OnboardingActivity : AppCompatActivity() {
    
    private var currentStep = 0
    private lateinit var title: TextView
    private lateinit var description: TextView
    private lateinit var btnNext: Button
    private lateinit var btnSkip: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        title = findViewById(R.id.title)
        description = findViewById(R.id.description)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)

        updateUI()

        btnNext.setOnClickListener {
            if (currentStep < 2) {
                currentStep++
                updateUI()
            } else {
                finishOnboarding()
            }
        }

        btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun updateUI() {
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
    }

    private fun finishOnboarding() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
