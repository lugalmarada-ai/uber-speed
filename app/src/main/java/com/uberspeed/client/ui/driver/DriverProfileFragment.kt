package com.uberspeed.client.ui.driver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.uberspeed.client.data.local.SessionManager

class DriverProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sessionManager = SessionManager(requireContext())
        val userName = sessionManager.getUserName() ?: "Conductor"
        val userEmail = sessionManager.getUserEmail() ?: "No disponible"
        
        val textView = TextView(context).apply {
            text = """
                👤 Mi Perfil
                
                Nombre: $userName
                Email: $userEmail
                
                Esta sección mostrará:
                • Información personal
                • Documentos
                • Vehículo registrado
                • Calificación promedio
            """.trimIndent()
            textSize = 16f
            setPadding(32, 32, 32, 32)
        }
        return textView
    }
}
