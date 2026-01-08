package com.uberspeed.client.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.uberspeed.client.R
import com.uberspeed.client.data.local.SessionManager
import com.uberspeed.client.ui.auth.LoginActivity

class HomeActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "HomeActivity"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")
        
        try {
            setContentView(R.layout.activity_home)
            Log.d(TAG, "setContentView completed")

            sessionManager = SessionManager(this)
            
            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)
            Log.d(TAG, "Toolbar set")

            val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
            val navView = findViewById<NavigationView>(R.id.nav_view)
            
            try {
                val navController = findNavController(R.id.nav_host_fragment)
                Log.d(TAG, "NavController found")
                
                appBarConfiguration = AppBarConfiguration(
                    setOf(R.id.nav_home, R.id.nav_history, R.id.nav_profile), 
                    drawerLayout
                )
                setupActionBarWithNavController(navController, appBarConfiguration)
                navView.setupWithNavController(navController)
                Log.d(TAG, "Navigation configured")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up navigation", e)
                Toast.makeText(this, "Navigation error (non-fatal)", Toast.LENGTH_SHORT).show()
            }

            // Handle Logout
            navView.menu.findItem(R.id.nav_logout)?.setOnMenuItemClickListener {
                try {
                    sessionManager.clearSession()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                } catch (e: Exception) {
                    Log.e(TAG, "Error during logout", e)
                }
                true
            }

            updateNavHeader(navView)
            
            Log.d(TAG, "onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Home Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateNavHeader(navView: NavigationView) {
        try {
            val headerView = navView.getHeaderView(0)
            val tvName = headerView?.findViewById<TextView>(R.id.tvUserName)
            val tvEmail = headerView?.findViewById<TextView>(R.id.tvUserEmail)

            tvName?.text = sessionManager.getUserName() ?: "Usuario"
            tvEmail?.text = sessionManager.getUserEmail() ?: "usuario@uberspeed.com"
        } catch (e: Exception) {
            Log.e(TAG, "Error updating nav header", e)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return try {
            val navController = findNavController(R.id.nav_host_fragment)
            navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onSupportNavigateUp", e)
            super.onSupportNavigateUp()
        }
    }
}
