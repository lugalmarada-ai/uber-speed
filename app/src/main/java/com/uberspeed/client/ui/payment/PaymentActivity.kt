package com.uberspeed.client.ui.payment

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.uberspeed.client.R
import com.uberspeed.client.data.local.SessionManager
import com.uberspeed.client.data.socket.SocketManager
import org.json.JSONObject

class PaymentActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRIP_ID = "trip_id"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_CURRENCY = "currency"
    }

    private lateinit var tvAmount: TextView
    private lateinit var rgPaymentMethod: RadioGroup
    private lateinit var tilReference: TextInputLayout
    private lateinit var etReference: TextInputEditText
    private lateinit var cardBankInfo: MaterialCardView
    private lateinit var tvBankInfo: TextView
    private lateinit var btnConfirmPayment: MaterialButton
    private lateinit var btnCancel: MaterialButton

    private lateinit var sessionManager: SessionManager
    private lateinit var socketManager: SocketManager

    private var tripId: String = ""
    private var amount: Double = 0.0
    private var currency: String = "USD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        tripId = intent.getStringExtra(EXTRA_TRIP_ID) ?: ""
        amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)
        currency = intent.getStringExtra(EXTRA_CURRENCY) ?: "USD"

        initViews()
        setupListeners()
        updateUI()
    }

    private fun initViews() {
        tvAmount = findViewById(R.id.tvAmount)
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod)
        tilReference = findViewById(R.id.tilReference)
        etReference = findViewById(R.id.etReference)
        cardBankInfo = findViewById(R.id.cardBankInfo)
        tvBankInfo = findViewById(R.id.tvBankInfo)
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment)
        btnCancel = findViewById(R.id.btnCancel)

        sessionManager = SessionManager(this)
        socketManager = SocketManager.getInstance()
    }

    private fun setupListeners() {
        rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbEfectivo -> {
                    tilReference.visibility = View.GONE
                    cardBankInfo.visibility = View.GONE
                }
                R.id.rbPagoMovil -> {
                    tilReference.visibility = View.VISIBLE
                    cardBankInfo.visibility = View.VISIBLE
                    tvBankInfo.text = "Banco: Venezuela\nTeléfono: 0412-000-0000\nCédula: V-12345678"
                }
                R.id.rbZelle -> {
                    tilReference.visibility = View.VISIBLE
                    cardBankInfo.visibility = View.VISIBLE
                    tvBankInfo.text = "Email: conductor@email.com\nNombre: Juan Pérez"
                }
                R.id.rbBinance -> {
                    tilReference.visibility = View.VISIBLE
                    cardBankInfo.visibility = View.VISIBLE
                    tvBankInfo.text = "Binance Pay ID: 123456789\nUsuario: @conductor"
                }
            }
        }

        btnConfirmPayment.setOnClickListener {
            confirmPayment()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun updateUI() {
        val symbol = if (currency == "USD") "$" else "Bs."
        tvAmount.text = "$symbol${String.format("%.2f", amount)}"
    }

    private fun getSelectedPaymentMethod(): String {
        return when (rgPaymentMethod.checkedRadioButtonId) {
            R.id.rbEfectivo -> "EFECTIVO"
            R.id.rbPagoMovil -> "PAGO_MOVIL"
            R.id.rbZelle -> "ZELLE"
            R.id.rbBinance -> "BINANCE_PAY"
            else -> "EFECTIVO"
        }
    }

    private fun confirmPayment() {
        val method = getSelectedPaymentMethod()
        val reference = etReference.text?.toString()?.trim()

        // Validate reference for digital payments
        if (method != "EFECTIVO" && reference.isNullOrEmpty()) {
            tilReference.error = "Ingresa el número de referencia"
            return
        }

        // Show loading
        btnConfirmPayment.isEnabled = false
        btnConfirmPayment.text = "Procesando..."

        // Create payment info
        val paymentInfo = JSONObject().apply {
            put("method", method)
            put("amount", amount)
            put("currency", currency)
            put("reference", reference ?: "")
        }

        // Emit via socket
        socketManager.emitPaymentCreated(tripId, paymentInfo)

        // Show success (in real app, wait for confirmation)
        Toast.makeText(this, "✅ Pago registrado exitosamente", Toast.LENGTH_LONG).show()
        
        // Return to home
        setResult(RESULT_OK)
        finish()
    }
}
