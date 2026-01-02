package com.uberspeed.client.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.uberspeed.client.R
import com.uberspeed.client.data.local.SessionManager

class ProfileFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        sessionManager = SessionManager(requireContext())

        val tvName = root.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = root.findViewById<TextView>(R.id.tvProfileEmail)

        val name = sessionManager.getUserName()
        val email = sessionManager.getUserEmail()

        tvName.text = name
        tvEmail.text = email

        return root
    }
}
