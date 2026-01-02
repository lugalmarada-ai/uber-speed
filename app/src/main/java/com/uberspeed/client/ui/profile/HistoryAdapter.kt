package com.uberspeed.client.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uberspeed.client.R
import com.uberspeed.client.data.model.Trip
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.TripViewHolder>() {

    private val trips = mutableListOf<Trip>()

    fun setTrips(newTrips: List<Trip>) {
        trips.clear()
        trips.addAll(newTrips)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(trips[position])
    }

    override fun getItemCount(): Int = trips.size

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvOrigin: TextView = itemView.findViewById(R.id.tvOrigin)
        private val tvDestination: TextView = itemView.findViewById(R.id.tvDestination)
        private val tvCost: TextView = itemView.findViewById(R.id.tvCost)

        fun bind(trip: Trip) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvDate.text = sdf.format(Date(trip.timestamp))
            tvOrigin.text = trip.request.origin
            tvDestination.text = trip.request.destination
            tvCost.text = "$ ${trip.cost}"
        }
    }
}
