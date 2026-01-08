const Message = require('../models/Message');
const Trip = require('../models/Trip');

// @desc    Send message in trip chat
// @route   POST /api/chat/messages
// @access  Private
exports.sendMessage = async (req, res) => {
    try {
        const { tripId, content, messageType } = req.body;

        if (!tripId || !content) {
            return res.status(400).json({
                success: false,
                message: 'Trip ID and content are required'
            });
        }

        // Verify trip exists and user is participant
        const trip = await Trip.findById(tripId);
        if (!trip) {
            return res.status(404).json({
                success: false,
                message: 'Trip not found'
            });
        }

        const isPassenger = trip.passenger.toString() === req.user._id.toString();
        const isDriver = trip.driver && trip.driver.toString() === req.user._id.toString();

        if (!isPassenger && !isDriver) {
            return res.status(403).json({
                success: false,
                message: 'Not authorized to send messages in this trip'
            });
        }

        const message = await Message.create({
            trip: tripId,
            sender: req.user._id,
            senderRole: isDriver ? 'driver' : 'passenger',
            content,
            messageType: messageType || 'TEXT'
        });

        await message.populate('sender', 'name');

        res.status(201).json({
            success: true,
            data: message
        });

        // TODO: Emit socket event
        // io.to(`trip_${tripId}`).emit('chat:message', message);

    } catch (error) {
        console.error('Error sending message:', error);
        res.status(500).json({
            success: false,
            message: 'Error sending message'
        });
    }
};

// @desc    Get messages for a trip
// @route   GET /api/chat/messages/:tripId
// @access  Private
exports.getMessages = async (req, res) => {
    try {
        const { tripId } = req.params;
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 50;
        const skip = (page - 1) * limit;

        // Verify trip exists and user is participant
        const trip = await Trip.findById(tripId);
        if (!trip) {
            return res.status(404).json({
                success: false,
                message: 'Trip not found'
            });
        }

        const isPassenger = trip.passenger.toString() === req.user._id.toString();
        const isDriver = trip.driver && trip.driver.toString() === req.user._id.toString();

        if (!isPassenger && !isDriver && req.user.role !== 'admin') {
            return res.status(403).json({
                success: false,
                message: 'Not authorized to view messages'
            });
        }

        const messages = await Message.find({ trip: tripId })
            .populate('sender', 'name')
            .sort({ createdAt: 1 })
            .skip(skip)
            .limit(limit);

        const total = await Message.countDocuments({ trip: tripId });

        res.status(200).json({
            success: true,
            count: messages.length,
            total,
            page,
            pages: Math.ceil(total / limit),
            data: messages
        });
    } catch (error) {
        console.error('Error fetching messages:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching messages'
        });
    }
};

// @desc    Mark messages as read
// @route   PUT /api/chat/messages/:tripId/read
// @access  Private
exports.markAsRead = async (req, res) => {
    try {
        const { tripId } = req.params;

        // Update all unread messages not sent by current user
        await Message.updateMany(
            {
                trip: tripId,
                sender: { $ne: req.user._id },
                read: false
            },
            {
                read: true,
                readAt: Date.now()
            }
        );

        res.status(200).json({
            success: true,
            message: 'Messages marked as read'
        });
    } catch (error) {
        console.error('Error marking messages as read:', error);
        res.status(500).json({
            success: false,
            message: 'Error marking messages as read'
        });
    }
};

// @desc    Get unread message count
// @route   GET /api/chat/unread/:tripId
// @access  Private
exports.getUnreadCount = async (req, res) => {
    try {
        const { tripId } = req.params;

        const count = await Message.countDocuments({
            trip: tripId,
            sender: { $ne: req.user._id },
            read: false
        });

        res.status(200).json({
            success: true,
            data: { unreadCount: count }
        });
    } catch (error) {
        console.error('Error getting unread count:', error);
        res.status(500).json({
            success: false,
            message: 'Error getting unread count'
        });
    }
};
