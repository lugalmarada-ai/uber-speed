package com.uberspeed.client.ui.driver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.uberspeed.client.R

data class TripRequest(
    val id: String,
    val serviceType: String,
    val originAddress: String,
    val destinationAddress: String,
    val estimatedPrice: Double,
    val estimatedDistance: Double,
    val estimatedDuration: Int,
    val paymentMethod: String,
    val passengerName: String
)

class TripRequestAdapter(
    private val onAccept: (TripRequest) -> Unit,
    private val onReject: (TripRequest) -> Unit
) : RecyclerView.Adapter<TripRequestAdapter.ViewHolder>() {

    private val tripRequests = mutableListOf<TripRequest>()

    fun updateRequests(requests: List<TripRequest>) {
        tripRequests.clear()
        tripRequests.addAll(requests)
        notifyDataSetChanged()
    }

    fun removeRequest(tripId: String) {
        val index = tripRequests.indexOfFirst { it.id == tripId }
        if (index >= 0) {
            tripRequests.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun addRequest(request: TripRequest) {
        tripRequests.add(0, request)
        notifyItemInserted(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = tripRequests[position]
        holder.bind(request)
    }

    override fun getItemCount() = tripRequests.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chipServiceType: Chip = itemView.findViewById(R.id.chipServiceType)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val tvOrigin: TextView = itemView.findViewById(R.id.tvOrigin)
        private val tvDestination: TextView = itemView.findViewById(R.id.tvDestination)
        private val chipDistance: Chip = itemView.findViewById(R.id.chipDistance)
        private val chipDuration: Chip = itemView.findViewById(R.id.chipDuration)
        private val chipPaymentMethod: Chip = itemView.findViewById(R.id.chipPaymentMethod)
        private val btnAccept: MaterialButton = itemView.findViewById(R.id.btnAccept)
        private val btnReject: MaterialButton = itemView.findViewById(R.id.btnReject)

        fun bind(request: TripRequest) {
            chipServiceType.text = request.serviceType
            tvPrice.text = "$${String.format("%.2f", request.estimatedPrice)}"
            tvOrigin.text = request.originAddress
            tvDestination.text = request.destinationAddress
            chipDistance.text = "${String.format("%.1f", request.estimatedDistance)} km"
            chipDuration.text = "${request.estimatedDuration} min"
            chipPaymentMethod.text = getPaymentMethodName(request.paymentMethod)

            btnAccept.setOnClickListener { onAccept(request) }
            btnReject.setOnClickListener { onReject(request) }
        }

        private fun getPaymentMethodName(method: String): String {
            return when (method) {
                "EFECTIVO" -> "Efectivo"
                "PAGO_MOVIL" -> "Pago Móvil"
                "ZELLE" -> "Zelle"
                "BINANCE_PAY" -> "Binance"
                else -> method
            }
        }
    }
}
