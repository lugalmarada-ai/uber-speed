package com.uberspeed.client.ui.driver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class DriverHistoryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val textView = TextView(context).apply {
            text = "📋 Historial de Viajes\n\nEsta sección mostrará:\n• Lista de viajes completados\n• Detalles de cada viaje\n• Calificaciones recibidas"
            textSize = 16f
            setPadding(32, 32, 32, 32)
        }
        return textView
    }
}
