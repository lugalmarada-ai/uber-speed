package com.uberspeed.client.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uberspeed.client.R
import com.uberspeed.client.data.local.SessionManager
import com.uberspeed.client.ui.booking.TripViewModel
import com.uberspeed.client.utils.Resource

class HistoryFragment : Fragment() {

    private lateinit var viewModel: TripViewModel
    private lateinit var adapter: HistoryAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_history, container, false)
        sessionManager = SessionManager(requireContext())
        viewModel = ViewModelProvider(this)[TripViewModel::class.java]

        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = HistoryAdapter()
        recyclerView.adapter = adapter

        val token = sessionManager.getAuthToken()
        if (token != null) {
            viewModel.getHistory(token)
        }

        observeViewModel()

        return root
    }

    private fun observeViewModel() {
        viewModel.historyState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { adapter.setTrips(it) }
                }
                is Resource.Error -> {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    // Show loading
                }
            }
        }
    }
}
