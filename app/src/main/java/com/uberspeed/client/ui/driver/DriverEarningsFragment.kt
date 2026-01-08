package com.uberspeed.client.ui.driver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.uberspeed.client.R

class DriverEarningsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Placeholder - will be implemented in future iteration
        val textView = TextView(context).apply {
            text = "💰 Mis Ganancias\n\nEsta sección mostrará:\n• Ganancias del día\n• Ganancias de la semana\n• Historial de pagos"
            textSize = 16f
            setPadding(32, 32, 32, 32)
        }
        return textView
    }
}
