package com.uberspeed.client.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.uberspeed.client.R
import com.uberspeed.client.data.local.SessionManager
import com.uberspeed.client.databinding.ActivityHomeBinding
import com.uberspeed.client.ui.auth.LoginActivity
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        sessionManager = SessionManager(this)

        val drawerLayout: DrawerLayout = binding.root as DrawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment)
        
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_history, R.id.nav_profile
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Handle Logout manually
        navView.menu.findItem(R.id.nav_logout).setOnMenuItemClickListener {
            sessionManager.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
            true
        }

        updateNavHeader(navView)
    }

    private fun updateNavHeader(navView: NavigationView) {
        val headerView = navView.getHeaderView(0)
        val tvName = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvEmail = headerView.findViewById<TextView>(R.id.tvUserEmail)

        val name = sessionManager.prefs.getString(SessionManager.KEY_USER_NAME, "User")
        val email = sessionManager.prefs.getString(SessionManager.KEY_USER_EMAIL, "user@example.com")
        
        tvName.text = name
        tvEmail.text = email
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
