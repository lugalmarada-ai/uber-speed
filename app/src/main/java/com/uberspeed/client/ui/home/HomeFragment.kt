package com.uberspeed.client.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.textfield.TextInputEditText
import com.uberspeed.client.R
import com.uberspeed.client.data.local.SessionManager
import com.uberspeed.client.data.socket.SocketManager
import java.util.Locale

class HomeFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private const val TAG = "HomeFragment"
    }

    private lateinit var sessionManager: SessionManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null

    private lateinit var etOrigin: TextInputEditText
    private lateinit var etDestination: TextInputEditText
    private lateinit var btnRequest: Button

    private var currentLocation: LatLng? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                enableMyLocation()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                enableMyLocation()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        
        sessionManager = SessionManager(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Initialize views
        etOrigin = root.findViewById(R.id.etOrigin)
        etDestination = root.findViewById(R.id.etDestination)
        btnRequest = root.findViewById(R.id.btnRequest)

        // Initialize map
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        
        btnRequest.setOnClickListener {
            requestTrip()
        }

        // Connect to socket
        connectSocket()

        return root
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        Log.d(TAG, "Map is ready")
        
        // Request location permission
        if (hasLocationPermission()) {
            enableMyLocation()
        } else {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }

        // Handle map clicks to set destination
        googleMap?.setOnMapClickListener { latLng ->
            setDestinationOnMap(latLng)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableMyLocation() {
        if (hasLocationPermission()) {
            try {
                googleMap?.isMyLocationEnabled = true
                
                // Get current location and move camera
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 15f))
                        
                        // Get address for current location
                        getAddressFromLocation(currentLocation!!) { address ->
                            etOrigin.setText(address)
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException", e)
            }
        }
    }

    private fun setDestinationOnMap(latLng: LatLng) {
        // Clear previous markers
        googleMap?.clear()
        
        // Add destination marker
        googleMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Destino")
        )
        
        // Get address for destination
        getAddressFromLocation(latLng) { address ->
            etDestination.setText(address)
        }
    }

    private fun getAddressFromLocation(latLng: LatLng, callback: (String) -> Unit) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressText = buildString {
                    address.thoroughfare?.let { append(it) }
                    address.subThoroughfare?.let { append(" $it") }
                    if (isEmpty()) {
                        address.getAddressLine(0)?.let { append(it) }
                    }
                }
                callback(addressText.ifEmpty { "${latLng.latitude}, ${latLng.longitude}" })
            } else {
                callback("${latLng.latitude}, ${latLng.longitude}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Geocoder error", e)
            callback("${latLng.latitude}, ${latLng.longitude}")
        }
    }

    private fun connectSocket() {
        val token = sessionManager.getAuthToken()
        if (!token.isNullOrEmpty()) {
            val socketManager = SocketManager.getInstance()
            if (!socketManager.isConnected()) {
                socketManager.connect(token)
                socketManager.onConnect = {
                    Log.d(TAG, "Socket connected")
                }
                socketManager.onError = { error ->
                    Log.e(TAG, "Socket error: $error")
                }
            }
        }
    }

    private fun requestTrip() {
        val destination = etDestination.text?.toString()?.trim()
        
        if (destination.isNullOrEmpty()) {
            Toast.makeText(context, "Por favor ingresa tu destino", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentLocation == null) {
            Toast.makeText(context, "Esperando ubicación...", Toast.LENGTH_SHORT).show()
            return
        }

        // Show confirmation
        Toast.makeText(context, "Buscando conductor disponible...", Toast.LENGTH_SHORT).show()
        
        // TODO: Emit trip request via socket
        Log.d(TAG, "Requesting trip to: $destination")
    }
}
