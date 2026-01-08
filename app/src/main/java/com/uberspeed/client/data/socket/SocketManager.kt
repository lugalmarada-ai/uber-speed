package com.uberspeed.client.data.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException

class SocketManager private constructor() {

    companion object {
        private const val TAG = "SocketManager"
        private const val SERVER_URL = "https://uber-speed.xyz"
        
        @Volatile
        private var instance: SocketManager? = null

        fun getInstance(): SocketManager {
            return instance ?: synchronized(this) {
                instance ?: SocketManager().also { instance = it }
            }
        }
    }

    private var socket: Socket? = null
    private var authToken: String? = null

    // Callbacks
    var onConnect: (() -> Unit)? = null
    var onDisconnect: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onNewTripRequest: ((JSONObject) -> Unit)? = null
    var onTripAccepted: ((JSONObject) -> Unit)? = null
    var onTripStatusUpdate: ((JSONObject) -> Unit)? = null
    var onDriverLocation: ((JSONObject) -> Unit)? = null
    var onNewChatMessage: ((JSONObject) -> Unit)? = null
    var onPaymentConfirmed: ((JSONObject) -> Unit)? = null

    fun connect(token: String) {
        authToken = token
        
        try {
            val options = IO.Options().apply {
                auth = mapOf("token" to token)
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
            }
            
            socket = IO.socket(SERVER_URL, options)
            
            setupEventListeners()
            
            socket?.connect()
            Log.d(TAG, "Connecting to socket server...")
            
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Invalid socket URL", e)
            onError?.invoke("Invalid server URL")
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to socket", e)
            onError?.invoke("Connection error: ${e.message}")
        }
    }

    private fun setupEventListeners() {
        socket?.apply {
            on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected")
                onConnect?.invoke()
            }

            on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Socket disconnected")
                onDisconnect?.invoke()
            }

            on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args.getOrNull(0)?.toString() ?: "Unknown error"
                Log.e(TAG, "Socket connection error: $error")
                onError?.invoke(error)
            }

            // Trip events
            on("trip:new") { args ->
                Log.d(TAG, "New trip request received")
                (args.getOrNull(0) as? JSONObject)?.let { onNewTripRequest?.invoke(it) }
            }

            on("trip:accepted") { args ->
                Log.d(TAG, "Trip accepted")
                (args.getOrNull(0) as? JSONObject)?.let { onTripAccepted?.invoke(it) }
            }

            on("trip:status") { args ->
                Log.d(TAG, "Trip status update")
                (args.getOrNull(0) as? JSONObject)?.let { onTripStatusUpdate?.invoke(it) }
            }

            on("trip:driver_location") { args ->
                (args.getOrNull(0) as? JSONObject)?.let { onDriverLocation?.invoke(it) }
            }

            on("trip:cancelled") { args ->
                Log.d(TAG, "Trip cancelled")
                (args.getOrNull(0) as? JSONObject)?.let { onTripStatusUpdate?.invoke(it) }
            }

            // Chat events
            on("chat:message") { args ->
                Log.d(TAG, "New chat message")
                (args.getOrNull(0) as? JSONObject)?.let { onNewChatMessage?.invoke(it) }
            }

            // Payment events
            on("payment:confirmed") { args ->
                Log.d(TAG, "Payment confirmed")
                (args.getOrNull(0) as? JSONObject)?.let { onPaymentConfirmed?.invoke(it) }
            }
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        Log.d(TAG, "Socket disconnected manually")
    }

    fun isConnected(): Boolean {
        return socket?.connected() == true
    }

    // ========== DRIVER METHODS ==========

    fun emitDriverStatus(online: Boolean) {
        val data = JSONObject().apply {
            put("online", online)
        }
        socket?.emit("driver:status", data)
        Log.d(TAG, "Emitted driver status: $online")
    }

    fun emitDriverLocation(lat: Double, lng: Double, tripId: String? = null) {
        val data = JSONObject().apply {
            put("lat", lat)
            put("lng", lng)
            tripId?.let { put("tripId", it) }
        }
        socket?.emit("driver:location", data)
    }

    // ========== TRIP METHODS ==========

    fun joinTripRoom(tripId: String) {
        val data = JSONObject().apply {
            put("tripId", tripId)
        }
        socket?.emit("trip:join", data)
        Log.d(TAG, "Joined trip room: $tripId")
    }

    fun leaveTripRoom(tripId: String) {
        val data = JSONObject().apply {
            put("tripId", tripId)
        }
        socket?.emit("trip:leave", data)
    }

    fun emitTripRequest(tripData: JSONObject) {
        socket?.emit("trip:request", tripData)
        Log.d(TAG, "Emitted trip request")
    }

    fun emitTripAccepted(tripId: String, passengerId: String, driverInfo: JSONObject) {
        val data = JSONObject().apply {
            put("tripId", tripId)
            put("passengerId", passengerId)
            put("driverInfo", driverInfo)
        }
        socket?.emit("trip:accepted", data)
    }

    fun emitTripStatusUpdate(tripId: String, status: String) {
        val data = JSONObject().apply {
            put("tripId", tripId)
            put("status", status)
        }
        socket?.emit("trip:status_update", data)
        Log.d(TAG, "Emitted status update: $status")
    }

    fun emitTripCancelled(tripId: String, reason: String) {
        val data = JSONObject().apply {
            put("tripId", tripId)
            put("cancelledBy", "driver")
            put("reason", reason)
        }
        socket?.emit("trip:cancelled", data)
    }

    // ========== CHAT METHODS ==========

    fun sendChatMessage(tripId: String, content: String, messageType: String = "TEXT") {
        val data = JSONObject().apply {
            put("tripId", tripId)
            put("content", content)
            put("messageType", messageType)
        }
        socket?.emit("chat:message", data)
        Log.d(TAG, "Sent chat message")
    }

    fun emitTyping(tripId: String, isTyping: Boolean) {
        val data = JSONObject().apply {
            put("tripId", tripId)
            put("isTyping", isTyping)
        }
        socket?.emit("chat:typing", data)
    }

    // ========== PAYMENT METHODS ==========

    fun emitPaymentCreated(tripId: String, paymentInfo: JSONObject) {
        val data = JSONObject().apply {
            put("tripId", tripId)
            put("paymentInfo", paymentInfo)
        }
        socket?.emit("payment:created", data)
    }

    fun emitPaymentConfirmed(tripId: String, paymentId: String) {
        val data = JSONObject().apply {
            put("tripId", tripId)
            put("paymentId", paymentId)
        }
        socket?.emit("payment:confirmed", data)
    }
}
