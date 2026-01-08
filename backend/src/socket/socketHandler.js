const jwt = require('jsonwebtoken');
const User = require('../models/User');

// Store active connections
const activeConnections = new Map();
// Store driver locations
const driverLocations = new Map();

const initializeSocket = (io) => {
    // Authentication middleware for Socket.IO
    io.use(async (socket, next) => {
        try {
            const token = socket.handshake.auth.token || socket.handshake.query.token;
            
            if (!token) {
                return next(new Error('Authentication required'));
            }

            const decoded = jwt.verify(token, process.env.JWT_SECRET);
            const user = await User.findById(decoded.id);
            
            if (!user) {
                return next(new Error('User not found'));
            }

            socket.user = user;
            next();
        } catch (error) {
            next(new Error('Invalid token'));
        }
    });

    io.on('connection', (socket) => {
        const userId = socket.user._id.toString();
        const userRole = socket.user.role;

        console.log(`User connected: ${socket.user.name} (${userRole})`);

        // Store connection
        activeConnections.set(userId, socket.id);

        // Join role-based room
        socket.join(`role_${userRole}`);
        socket.join(`user_${userId}`);

        // ========== DRIVER EVENTS ==========

        // Driver goes online/offline
        socket.on('driver:status', (data) => {
            const { online } = data;
            if (online) {
                socket.join('drivers_available');
                console.log(`Driver ${socket.user.name} is now online`);
            } else {
                socket.leave('drivers_available');
                driverLocations.delete(userId);
                console.log(`Driver ${socket.user.name} is now offline`);
            }
        });

        // Driver updates location
        socket.on('driver:location', (data) => {
            const { lat, lng, tripId } = data;
            
            // Store driver location
            driverLocations.set(userId, { lat, lng, timestamp: Date.now() });
            
            // If in active trip, emit to passenger
            if (tripId) {
                io.to(`trip_${tripId}`).emit('trip:driver_location', {
                    driverId: userId,
                    lat,
                    lng,
                    timestamp: Date.now()
                });
            }
        });

        // ========== TRIP EVENTS ==========

        // Join trip room
        socket.on('trip:join', (data) => {
            const { tripId } = data;
            socket.join(`trip_${tripId}`);
            console.log(`User ${socket.user.name} joined trip room: ${tripId}`);
        });

        // Leave trip room
        socket.on('trip:leave', (data) => {
            const { tripId } = data;
            socket.leave(`trip_${tripId}`);
        });

        // New trip request (from passenger - broadcast to drivers)
        socket.on('trip:request', (data) => {
            io.to('drivers_available').emit('trip:new', {
                ...data,
                timestamp: Date.now()
            });
        });

        // Trip accepted (from driver - notify passenger)
        socket.on('trip:accepted', (data) => {
            const { tripId, passengerId, driverInfo } = data;
            io.to(`user_${passengerId}`).emit('trip:accepted', {
                tripId,
                driver: driverInfo,
                timestamp: Date.now()
            });
        });

        // Trip status update
        socket.on('trip:status_update', (data) => {
            const { tripId, status } = data;
            io.to(`trip_${tripId}`).emit('trip:status', {
                tripId,
                status,
                timestamp: Date.now()
            });
        });

        // Trip cancelled
        socket.on('trip:cancelled', (data) => {
            const { tripId, cancelledBy, reason } = data;
            io.to(`trip_${tripId}`).emit('trip:cancelled', {
                tripId,
                cancelledBy,
                reason,
                timestamp: Date.now()
            });
        });

        // ========== CHAT EVENTS ==========

        // New chat message
        socket.on('chat:message', (data) => {
            const { tripId, content, messageType } = data;
            io.to(`trip_${tripId}`).emit('chat:message', {
                tripId,
                senderId: userId,
                senderName: socket.user.name,
                senderRole: userRole,
                content,
                messageType: messageType || 'TEXT',
                timestamp: Date.now()
            });
        });

        // Typing indicator
        socket.on('chat:typing', (data) => {
            const { tripId, isTyping } = data;
            socket.to(`trip_${tripId}`).emit('chat:typing', {
                userId,
                userName: socket.user.name,
                isTyping
            });
        });

        // ========== PAYMENT EVENTS ==========

        // Payment created
        socket.on('payment:created', (data) => {
            const { tripId, paymentInfo } = data;
            io.to(`trip_${tripId}`).emit('payment:created', {
                tripId,
                ...paymentInfo,
                timestamp: Date.now()
            });
        });

        // Payment confirmed
        socket.on('payment:confirmed', (data) => {
            const { tripId, paymentId } = data;
            io.to(`trip_${tripId}`).emit('payment:confirmed', {
                tripId,
                paymentId,
                confirmedBy: userId,
                timestamp: Date.now()
            });
        });

        // ========== DISCONNECT ==========

        socket.on('disconnect', () => {
            console.log(`User disconnected: ${socket.user.name}`);
            activeConnections.delete(userId);
            driverLocations.delete(userId);
        });
    });

    return io;
};

// Helper functions
const getActiveDrivers = () => {
    return Array.from(driverLocations.entries()).map(([driverId, location]) => ({
        driverId,
        ...location
    }));
};

const isUserOnline = (userId) => {
    return activeConnections.has(userId);
};

module.exports = {
    initializeSocket,
    getActiveDrivers,
    isUserOnline
};
