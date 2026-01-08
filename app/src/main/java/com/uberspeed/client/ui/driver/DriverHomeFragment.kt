package com.uberspeed.client.ui.driver

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.chip.Chip
import com.uberspeed.client.R
import com.uberspeed.client.data.local.SessionManager

class DriverHomeFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private const val TAG = "DriverHomeFragment"
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    private lateinit var sessionManager: SessionManager
    private var googleMap: GoogleMap? = null
    
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var tvEmptyMessage: TextView
    private lateinit var rvTripRequests: RecyclerView
    private lateinit var chipRequestCount: Chip
    private lateinit var statusIndicator: View
    private lateinit var tvStatusText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_driver_home, container, false)
        
        sessionManager = SessionManager(requireContext())
        
        // Initialize views
        emptyStateContainer = root.findViewById(R.id.emptyStateContainer)
        tvEmptyMessage = root.findViewById(R.id.tvEmptyMessage)
        rvTripRequests = root.findViewById(R.id.rvTripRequests)
        chipRequestCount = root.findViewById(R.id.chipRequestCount)
        statusIndicator = root.findViewById(R.id.statusIndicator)
        tvStatusText = root.findViewById(R.id.tvStatusText)
        
        // Setup RecyclerView
        rvTripRequests.layoutManager = LinearLayoutManager(context)
        // TODO: Set adapter with trip requests
        
        // Initialize map
        initializeMap()
        
        return root
    }

    private fun initializeMap() {
        try {
            val mapFragment = childFragmentManager.findFragmentById(R.id.driverMap) as? SupportMapFragment
            mapFragment?.getMapAsync(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing map", e)
            Toast.makeText(context, "Error al cargar el mapa", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        Log.d(TAG, "Map is ready")
        
        try {
            // Check for location permissions
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                googleMap?.isMyLocationEnabled = true
                googleMap?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST
                )
            }

            // Default location (Barinas, Venezuela)
            val defaultLocation = LatLng(8.6231, -70.2069)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 14f))
            
            // Map settings
            googleMap?.uiSettings?.apply {
                isZoomControlsEnabled = true
                isCompassEnabled = true
                isMapToolbarEnabled = false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up map", e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    googleMap?.isMyLocationEnabled = true
                } catch (e: SecurityException) {
                    Log.e(TAG, "Security exception enabling location", e)
                }
            }
        }
    }

    fun updateOnlineStatus(isOnline: Boolean) {
        if (isOnline) {
            statusIndicator.setBackgroundResource(R.drawable.circle_green)
            tvStatusText.text = "Conectado"
            tvEmptyMessage.text = "Esperando solicitudes..."
        } else {
            statusIndicator.setBackgroundResource(R.drawable.circle_red)
            tvStatusText.text = "Desconectado"
            tvEmptyMessage.text = "Conéctate para recibir solicitudes"
        }
    }

    fun updateTripRequests(count: Int) {
        chipRequestCount.text = count.toString()
        
        if (count > 0) {
            emptyStateContainer.visibility = View.GONE
            rvTripRequests.visibility = View.VISIBLE
        } else {
            emptyStateContainer.visibility = View.VISIBLE
            rvTripRequests.visibility = View.GONE
        }
    }
}
