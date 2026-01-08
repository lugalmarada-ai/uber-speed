package com.uberspeed.client.ui.driver

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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.uberspeed.client.R
import com.uberspeed.client.data.local.SessionManager
import com.uberspeed.client.ui.auth.LoginActivity

class DriverHomeActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DriverHomeActivity"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sessionManager: SessionManager
    private var isOnline = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")

        try {
            setContentView(R.layout.activity_driver_home)
            Log.d(TAG, "setContentView completed")

            sessionManager = SessionManager(this)

            val toolbar = findViewById<Toolbar>(R.id.driver_toolbar)
            setSupportActionBar(toolbar)
            Log.d(TAG, "Toolbar set")

            val drawerLayout = findViewById<DrawerLayout>(R.id.driver_drawer_layout)
            val navView = findViewById<NavigationView>(R.id.driver_nav_view)
            val fabOnlineToggle = findViewById<ExtendedFloatingActionButton>(R.id.fabOnlineToggle)

            try {
                val navController = findNavController(R.id.driver_nav_host_fragment)
                Log.d(TAG, "NavController found")

                appBarConfiguration = AppBarConfiguration(
                    setOf(R.id.nav_driver_home, R.id.nav_driver_earnings, R.id.nav_driver_history, R.id.nav_driver_profile),
                    drawerLayout
                )
                setupActionBarWithNavController(navController, appBarConfiguration)
                navView.setupWithNavController(navController)
                Log.d(TAG, "Navigation configured")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up navigation", e)
                Toast.makeText(this, "Navigation error (non-fatal)", Toast.LENGTH_SHORT).show()
            }

            // Setup online/offline toggle
            fabOnlineToggle.setOnClickListener {
                toggleOnlineStatus(fabOnlineToggle)
            }

            // Handle Logout
            navView.menu.findItem(R.id.nav_driver_logout)?.setOnMenuItemClickListener {
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
            Toast.makeText(this, "🚗 Modo Conductor Activo", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun toggleOnlineStatus(fab: ExtendedFloatingActionButton) {
        isOnline = !isOnline
        
        if (isOnline) {
            fab.text = "DESCONECTARSE"
            fab.setIconResource(android.R.drawable.presence_online)
            fab.setBackgroundColor(resources.getColor(android.R.color.holo_red_light, theme))
            Toast.makeText(this, "✅ Ahora estás conectado y recibirás solicitudes", Toast.LENGTH_LONG).show()
            
            // TODO: Connect to socket and start location updates
            // socketManager.emitDriverStatus(true)
            // locationService.startLocationUpdates()
        } else {
            fab.text = "CONECTARSE"
            fab.setIconResource(android.R.drawable.presence_offline)
            fab.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark, theme))
            Toast.makeText(this, "❌ Desconectado - No recibirás solicitudes", Toast.LENGTH_SHORT).show()
            
            // TODO: Disconnect from socket and stop location updates
            // socketManager.emitDriverStatus(false)
            // locationService.stopLocationUpdates()
        }
    }

    private fun updateNavHeader(navView: NavigationView) {
        try {
            val headerView = navView.getHeaderView(0)
            val tvName = headerView?.findViewById<TextView>(R.id.tvDriverName)
            val tvEmail = headerView?.findViewById<TextView>(R.id.tvDriverEmail)

            tvName?.text = sessionManager.getUserName() ?: "Conductor"
            tvEmail?.text = sessionManager.getUserEmail() ?: "conductor@uberspeed.com"
        } catch (e: Exception) {
            Log.e(TAG, "Error updating nav header", e)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return try {
            val navController = findNavController(R.id.driver_nav_host_fragment)
            navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onSupportNavigateUp", e)
            super.onSupportNavigateUp()
        }
    }
}
