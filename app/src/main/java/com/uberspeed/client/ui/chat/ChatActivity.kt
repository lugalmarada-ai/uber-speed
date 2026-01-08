package com.uberspeed.client.ui.chat

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.uberspeed.client.R
import com.uberspeed.client.data.local.SessionManager
import com.uberspeed.client.data.socket.SocketManager
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ChatActivity"
        const val EXTRA_TRIP_ID = "trip_id"
        const val EXTRA_OTHER_USER_NAME = "other_user_name"
    }

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: TextInputEditText
    private lateinit var fabSend: FloatingActionButton
    private lateinit var toolbar: MaterialToolbar

    private lateinit var sessionManager: SessionManager
    private lateinit var socketManager: SocketManager
    private lateinit var adapter: ChatAdapter

    private var tripId: String = ""
    private var otherUserName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        tripId = intent.getStringExtra(EXTRA_TRIP_ID) ?: ""
        otherUserName = intent.getStringExtra(EXTRA_OTHER_USER_NAME) ?: "Usuario"

        initViews()
        setupSocket()
        setupListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.chatToolbar)
        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        fabSend = findViewById(R.id.fabSend)

        sessionManager = SessionManager(this)
        socketManager = SocketManager.getInstance()

        // Setup toolbar
        val tvTitle = findViewById<android.widget.TextView>(R.id.tvChatTitle)
        val tvSubtitle = findViewById<android.widget.TextView>(R.id.tvChatSubtitle)
        tvTitle.text = otherUserName
        tvSubtitle.text = "En viaje"

        toolbar.setNavigationOnClickListener { finish() }

        // Setup RecyclerView
        adapter = ChatAdapter(sessionManager.getUserId())
        rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvMessages.adapter = adapter
    }

    private fun setupSocket() {
        // Join trip room
        socketManager.joinTripRoom(tripId)

        // Listen for new messages
        socketManager.onNewChatMessage = { messageJson ->
            runOnUiThread {
                try {
                    val message = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        senderId = messageJson.optString("senderId"),
                        senderName = messageJson.optString("senderName"),
                        content = messageJson.optString("content"),
                        timestamp = messageJson.optLong("timestamp", System.currentTimeMillis()),
                        isRead = false
                    )
                    adapter.addMessage(message)
                    rvMessages.scrollToPosition(adapter.itemCount - 1)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message", e)
                }
            }
        }
    }

    private fun setupListeners() {
        fabSend.setOnClickListener {
            val content = etMessage.text?.toString()?.trim()
            if (!content.isNullOrEmpty()) {
                sendMessage(content)
                etMessage.text?.clear()
            }
        }
    }

    private fun sendMessage(content: String) {
        // Send via socket
        socketManager.sendChatMessage(tripId, content)

        // Add to local list immediately
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = sessionManager.getUserId(),
            senderName = sessionManager.getUserName(),
            content = content,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        adapter.addMessage(message)
        rvMessages.scrollToPosition(adapter.itemCount - 1)

        Log.d(TAG, "Message sent: $content")
    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.leaveTripRoom(tripId)
    }
}

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean
)
