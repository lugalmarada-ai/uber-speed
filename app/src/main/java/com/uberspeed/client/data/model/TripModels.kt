package com.uberspeed.client.data.model

data class ServiceRequest(
    val type: String, // "TAXI", "DELIVERY"
    val origin: String,
    val destination: String,
    val originLat: Double,
    val originLng: Double,
    val destLat: Double,
    val destLng: Double,
    val paymentMethod: String
)

data class Trip(
    val id: String,
    val status: String, // "PENDING", "ACCEPTED", "IN_PROGRESS", "COMPLETED"
    val driverId: String?,
    val driverName: String?,
    val eta: String?,
    val cost: Double,
    val request: ServiceRequest,
    val timestamp: Long
)
