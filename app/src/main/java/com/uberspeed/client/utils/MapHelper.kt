package com.uberspeed.client.utils

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kotlin.math.*

object MapHelper {

    private const val TAG = "MapHelper"

    /**
     * Draw a route line between two points
     */
    fun drawRoute(
        map: GoogleMap,
        origin: LatLng,
        destination: LatLng,
        color: Int = Color.parseColor("#4285F4"),
        width: Float = 8f
    ): Polyline {
        return map.addPolyline(
            PolylineOptions()
                .add(origin, destination)
                .color(color)
                .width(width)
                .geodesic(true)
        )
    }

    /**
     * Add origin marker (green)
     */
    fun addOriginMarker(map: GoogleMap, position: LatLng, title: String = "Origen"): Marker? {
        return map.addMarker(
            MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
    }

    /**
     * Add destination marker (red)
     */
    fun addDestinationMarker(map: GoogleMap, position: LatLng, title: String = "Destino"): Marker? {
        return map.addMarker(
            MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
    }

    /**
     * Add driver/car marker (blue)
     */
    fun addDriverMarker(map: GoogleMap, position: LatLng, title: String = "Conductor"): Marker? {
        return map.addMarker(
            MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .flat(true)
                .anchor(0.5f, 0.5f)
        )
    }

    /**
     * Animate camera to show both origin and destination
     */
    fun fitBounds(map: GoogleMap, vararg points: LatLng, padding: Int = 100) {
        if (points.isEmpty()) return
        
        val builder = LatLngBounds.Builder()
        points.forEach { builder.include(it) }
        
        try {
            val bounds = builder.build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        } catch (e: Exception) {
            Log.e(TAG, "Error fitting bounds", e)
            // Fallback to center on first point
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(points[0], 14f))
        }
    }

    /**
     * Animate camera to a specific location
     */
    fun animateToLocation(map: GoogleMap, location: LatLng, zoom: Float = 15f) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))
    }

    /**
     * Calculate distance between two points in kilometers
     */
    fun calculateDistance(origin: LatLng, destination: LatLng): Double {
        val earthRadius = 6371.0 // km
        
        val dLat = Math.toRadians(destination.latitude - origin.latitude)
        val dLng = Math.toRadians(destination.longitude - origin.longitude)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(origin.latitude)) * cos(Math.toRadians(destination.latitude)) *
                sin(dLng / 2) * sin(dLng / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }

    /**
     * Estimate travel time based on distance (rough estimate)
     */
    fun estimateTravelTime(distanceKm: Double, averageSpeedKmh: Double = 30.0): Int {
        return ((distanceKm / averageSpeedKmh) * 60).toInt() // minutes
    }

    /**
     * Calculate estimated price based on distance
     */
    fun estimatePrice(distanceKm: Double, baseFare: Double = 2.0, pricePerKm: Double = 1.5): Double {
        return baseFare + (distanceKm * pricePerKm)
    }

    /**
     * Update marker position smoothly (for tracking)
     */
    fun animateMarkerTo(marker: Marker?, newPosition: LatLng, duration: Long = 1000) {
        marker?.let {
            val startPosition = it.position
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            val startTime = System.currentTimeMillis()
            
            handler.post(object : Runnable {
                override fun run() {
                    val elapsed = System.currentTimeMillis() - startTime
                    val t = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                    
                    val lat = startPosition.latitude + (newPosition.latitude - startPosition.latitude) * t
                    val lng = startPosition.longitude + (newPosition.longitude - startPosition.longitude) * t
                    
                    it.position = LatLng(lat, lng)
                    
                    if (t < 1f) {
                        handler.postDelayed(this, 16) // ~60fps
                    }
                }
            })
        }
    }

    /**
     * Calculate bearing between two points (for marker rotation)
     */
    fun calculateBearing(from: LatLng, to: LatLng): Float {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        
        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        
        return Math.toDegrees(atan2(y, x)).toFloat()
    }
}
