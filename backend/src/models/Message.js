const mongoose = require('mongoose');

const messageSchema = new mongoose.Schema({
    trip: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Trip',
        required: true
    },
    sender: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    senderRole: {
        type: String,
        enum: ['passenger', 'driver'],
        required: true
    },
    
    // Message content
    content: {
        type: String,
        required: true,
        maxlength: 500
    },
    messageType: {
        type: String,
        enum: ['TEXT', 'LOCATION', 'IMAGE', 'SYSTEM'],
        default: 'TEXT'
    },
    
    // Read status
    read: {
        type: Boolean,
        default: false
    },
    readAt: {
        type: Date,
        default: null
    },
    
    createdAt: {
        type: Date,
        default: Date.now
    }
});

// Index for efficient chat loading
messageSchema.index({ trip: 1, createdAt: 1 });

module.exports = mongoose.model('Message', messageSchema);
