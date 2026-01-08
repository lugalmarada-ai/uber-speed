package com.uberspeed.client.ui.driver

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.uberspeed.client.R

class ActiveTripFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private const val TAG = "ActiveTripFragment"
        
        // Trip status constants
        const val STATUS_ACCEPTED = "ACCEPTED"
        const val STATUS_DRIVER_ARRIVING = "DRIVER_ARRIVING"
        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_COMPLETED = "COMPLETED"
    }

    private var googleMap: GoogleMap? = null
    private var currentStatus = STATUS_ACCEPTED
    
    // Views
    private lateinit var tvTripStatus: TextView
    private lateinit var tvPassengerName: TextView
    private lateinit var tvDestination: TextView
    private lateinit var tvPrice: TextView
    private lateinit var chipETA: Chip
    private lateinit var chipPaymentMethod: Chip
    private lateinit var btnAction: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnCall: ImageView
    private lateinit var btnChat: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_active_trip, container, false)
        
        // Initialize views
        tvTripStatus = root.findViewById(R.id.tvTripStatus)
        tvPassengerName = root.findViewById(R.id.tvPassengerName)
        tvDestination = root.findViewById(R.id.tvDestination)
        tvPrice = root.findViewById(R.id.tvPrice)
        chipETA = root.findViewById(R.id.chipETA)
        chipPaymentMethod = root.findViewById(R.id.chipPaymentMethod)
        btnAction = root.findViewById(R.id.btnAction)
        btnCancel = root.findViewById(R.id.btnCancel)
        btnCall = root.findViewById(R.id.btnCall)
        btnChat = root.findViewById(R.id.btnChat)
        
        // Initialize map
        val mapFragment = childFragmentManager.findFragmentById(R.id.activeTripMap) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        
        setupClickListeners()
        updateUIForStatus(currentStatus)
        
        return root
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        try {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                googleMap?.isMyLocationEnabled = true
            }
            
            // Default to Barinas
            val defaultLocation = LatLng(8.6231, -70.2069)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up map", e)
        }
    }

    private fun setupClickListeners() {
        btnAction.setOnClickListener {
            advanceStatus()
        }
        
        btnCancel.setOnClickListener {
            // TODO: Show cancellation dialog
            Toast.makeText(context, "Función de cancelación en desarrollo", Toast.LENGTH_SHORT).show()
        }
        
        btnCall.setOnClickListener {
            // TODO: Get passenger phone number from trip data
            val phoneNumber = "0412-000-0000"
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        }
        
        btnChat.setOnClickListener {
            // TODO: Navigate to chat activity
            Toast.makeText(context, "Abriendo chat...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun advanceStatus() {
        when (currentStatus) {
            STATUS_ACCEPTED -> {
                currentStatus = STATUS_DRIVER_ARRIVING
                // TODO: Emit socket event
            }
            STATUS_DRIVER_ARRIVING -> {
                currentStatus = STATUS_IN_PROGRESS
                // TODO: Emit socket event
            }
            STATUS_IN_PROGRESS -> {
                currentStatus = STATUS_COMPLETED
                // TODO: Emit socket event and navigate to payment/rating
                Toast.makeText(context, "✅ ¡Viaje completado!", Toast.LENGTH_LONG).show()
            }
        }
        updateUIForStatus(currentStatus)
    }

    private fun updateUIForStatus(status: String) {
        when (status) {
            STATUS_ACCEPTED -> {
                tvTripStatus.text = "VIAJE ACEPTADO"
                tvTripStatus.setTextColor(resources.getColor(android.R.color.holo_blue_dark, null))
                btnAction.text = "EN CAMINO"
                btnAction.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark, null))
                btnCancel.visibility = View.VISIBLE
            }
            STATUS_DRIVER_ARRIVING -> {
                tvTripStatus.text = "EN CAMINO AL PASAJERO"
                tvTripStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
                btnAction.text = "LLEGUÉ AL PUNTO DE RECOGIDA"
                btnAction.setBackgroundColor(resources.getColor(android.R.color.holo_orange_dark, null))
                btnCancel.visibility = View.VISIBLE
            }
            STATUS_IN_PROGRESS -> {
                tvTripStatus.text = "VIAJE EN PROGRESO"
                tvTripStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                btnAction.text = "FINALIZAR VIAJE"
                btnAction.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark, null))
                btnCancel.visibility = View.GONE
            }
            STATUS_COMPLETED -> {
                tvTripStatus.text = "VIAJE COMPLETADO"
                tvTripStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                btnAction.text = "CONFIRMAR PAGO"
                btnAction.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark, null))
                btnCancel.visibility = View.GONE
            }
        }
    }

    fun updateTripInfo(
        passengerName: String,
        destination: String,
        price: Double,
        paymentMethod: String,
        eta: String
    ) {
        tvPassengerName.text = passengerName
        tvDestination.text = destination
        tvPrice.text = "$${String.format("%.2f", price)}"
        chipPaymentMethod.text = paymentMethod
        chipETA.text = eta
    }

    fun updatePassengerLocation(lat: Double, lng: Double) {
        googleMap?.clear()
        val passengerLocation = LatLng(lat, lng)
        googleMap?.addMarker(
            MarkerOptions()
                .position(passengerLocation)
                .title("Pasajero")
        )
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 16f))
    }
}
