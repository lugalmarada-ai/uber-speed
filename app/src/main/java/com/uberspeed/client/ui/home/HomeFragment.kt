package com.uberspeed.client.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.uberspeed.client.R
import com.uberspeed.client.data.local.SessionManager

class HomeFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        
        sessionManager = SessionManager(requireContext())

        val btnRequest = root.findViewById<Button>(R.id.btnRequest)
        
        btnRequest.setOnClickListener {
            // Demo mode - just show a message
            Toast.makeText(
                context, 
                "🚗 Modo Demo: Solicitud de servicio enviada!\n(Conecta el backend para funcionalidad real)", 
                Toast.LENGTH_LONG
            ).show()
        }

        return root
    }
}
