const mongoose = require('mongoose');

const tripSchema = new mongoose.Schema({
    // Participants
    passenger: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    driver: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        default: null
    },

    // Service type
    serviceType: {
        type: String,
        enum: ['TAXI', 'DELIVERY', 'EXECUTIVE'],
        default: 'TAXI'
    },

    // Locations
    origin: {
        address: { type: String, required: true },
        lat: { type: Number, required: true },
        lng: { type: Number, required: true }
    },
    destination: {
        address: { type: String, required: true },
        lat: { type: Number, required: true },
        lng: { type: Number, required: true }
    },

    // Status tracking
    status: {
        type: String,
        enum: ['PENDING', 'ACCEPTED', 'DRIVER_ARRIVING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'],
        default: 'PENDING'
    },

    // Pricing
    estimatedCost: {
        type: Number,
        default: 0
    },
    finalCost: {
        type: Number,
        default: 0
    },
    currency: {
        type: String,
        enum: ['USD', 'VES'],
        default: 'USD'
    },

    // Distance & Time
    estimatedDistance: {
        type: Number, // in kilometers
        default: 0
    },
    estimatedDuration: {
        type: Number, // in minutes
        default: 0
    },

    // Payment
    paymentMethod: {
        type: String,
        enum: ['EFECTIVO', 'PAGO_MOVIL', 'ZELLE', 'BINANCE_PAY'],
        default: 'EFECTIVO'
    },
    paymentStatus: {
        type: String,
        enum: ['PENDING', 'CONFIRMED', 'FAILED'],
        default: 'PENDING'
    },
    paymentReference: {
        type: String,
        default: null
    },

    // Notes
    notes: {
        type: String,
        default: ''
    },

    // Timestamps
    requestedAt: {
        type: Date,
        default: Date.now
    },
    acceptedAt: {
        type: Date,
        default: null
    },
    startedAt: {
        type: Date,
        default: null
    },
    completedAt: {
        type: Date,
        default: null
    },
    cancelledAt: {
        type: Date,
        default: null
    },
    cancellationReason: {
        type: String,
        default: null
    },

    // Rating
    passengerRating: {
        type: Number,
        min: 1,
        max: 5,
        default: null
    },
    driverRating: {
        type: Number,
        min: 1,
        max: 5,
        default: null
    }
});

// Index for efficient queries
tripSchema.index({ passenger: 1, status: 1 });
tripSchema.index({ driver: 1, status: 1 });
tripSchema.index({ status: 1, requestedAt: -1 });

module.exports = mongoose.model('Trip', tripSchema);
