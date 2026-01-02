package com.uberspeed.client.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.uberspeed.client.R
import com.uberspeed.client.data.local.SessionManager
import com.uberspeed.client.data.model.ServiceRequest
import com.uberspeed.client.utils.Resource

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var map: GoogleMap
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        sessionManager = SessionManager(requireContext())

        // Map Setup
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        val btnRequest = root.findViewById<Button>(R.id.btnRequest)
        
        btnRequest.setOnClickListener {
            // Mock Request for Demo
            val token = sessionManager.getAuthToken()
            if (token != null) {
                val mockRequest = ServiceRequest(
                    type = "TAXI",
                    origin = "Origin Location",
                    destination = "Dest Location",
                    originLat = 0.0, originLng = 0.0,
                    destLat = 0.0, destLng = 0.0,
                    paymentMethod = "CASH"
                )
                homeViewModel.requestService(token, mockRequest)
                Toast.makeText(context, "Requesting service...", Toast.LENGTH_SHORT).show()
            }
        }
        
        observeViewModel()

        return root
    }

    private fun observeViewModel() {
        homeViewModel.requestState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                   Toast.makeText(context, "Trip Request Sent!", Toast.LENGTH_LONG).show()
                }
                is Resource.Error -> {
                    Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    // Show loading
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Set default location (Barinas, Venezuela approximation)
        val barinas = LatLng(8.6226, -70.2075)
        map.addMarker(MarkerOptions().position(barinas).title("Barinas"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(barinas, 14f))
    }
}
