const mongoose = require('mongoose');

const paymentSchema = new mongoose.Schema({
    trip: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Trip',
        required: true
    },
    payer: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    receiver: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    
    // Payment details
    method: {
        type: String,
        enum: ['EFECTIVO', 'PAGO_MOVIL', 'ZELLE', 'BINANCE_PAY'],
        required: true
    },
    amount: {
        type: Number,
        required: true
    },
    currency: {
        type: String,
        enum: ['USD', 'VES'],
        default: 'USD'
    },
    
    // Reference for digital payments
    reference: {
        type: String,
        default: null
    },
    
    // For Pago Móvil
    bankCode: {
        type: String,
        default: null
    },
    phoneNumber: {
        type: String,
        default: null
    },
    
    // Confirmation
    status: {
        type: String,
        enum: ['PENDING', 'CONFIRMED', 'REJECTED', 'REFUNDED'],
        default: 'PENDING'
    },
    confirmedBy: {
        type: String,
        enum: ['DRIVER', 'SYSTEM', 'ADMIN'],
        default: null
    },
    confirmedAt: {
        type: Date,
        default: null
    },
    
    // Notes
    notes: {
        type: String,
        default: ''
    },
    
    createdAt: {
        type: Date,
        default: Date.now
    }
});

// Index for queries
paymentSchema.index({ trip: 1 });
paymentSchema.index({ payer: 1, createdAt: -1 });
paymentSchema.index({ receiver: 1, createdAt: -1 });

module.exports = mongoose.model('Payment', paymentSchema);
