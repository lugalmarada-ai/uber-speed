const Trip = require('../models/Trip');
const User = require('../models/User');

// @desc    Request a new trip
// @route   POST /api/trips/request
// @access  Private (Passenger)
exports.requestTrip = async (req, res) => {
    try {
        const {
            serviceType,
            origin,
            destination,
            paymentMethod,
            estimatedCost,
            estimatedDistance,
            estimatedDuration,
            notes
        } = req.body;

        // Validate required fields
        if (!origin || !destination) {
            return res.status(400).json({
                success: false,
                message: 'Origin and destination are required'
            });
        }

        const trip = await Trip.create({
            passenger: req.user._id,
            serviceType: serviceType || 'TAXI',
            origin,
            destination,
            paymentMethod: paymentMethod || 'EFECTIVO',
            estimatedCost: estimatedCost || 0,
            estimatedDistance: estimatedDistance || 0,
            estimatedDuration: estimatedDuration || 0,
            notes: notes || '',
            status: 'PENDING'
        });

        // Populate passenger info
        await trip.populate('passenger', 'name phone');

        res.status(201).json({
            success: true,
            data: trip
        });

        // TODO: Emit socket event to nearby drivers
        // io.to('drivers').emit('trip:new', trip);

    } catch (error) {
        console.error('Error requesting trip:', error);
        res.status(500).json({
            success: false,
            message: 'Error creating trip request'
        });
    }
};

// @desc    Get pending trips for drivers
// @route   GET /api/trips/pending
// @access  Private (Driver)
exports.getPendingTrips = async (req, res) => {
    try {
        const trips = await Trip.find({ status: 'PENDING' })
            .populate('passenger', 'name phone')
            .sort({ requestedAt: -1 })
            .limit(20);

        res.status(200).json({
            success: true,
            count: trips.length,
            data: trips
        });
    } catch (error) {
        console.error('Error fetching pending trips:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching trips'
        });
    }
};

// @desc    Accept a trip (Driver)
// @route   PUT /api/trips/:id/accept
// @access  Private (Driver)
exports.acceptTrip = async (req, res) => {
    try {
        const trip = await Trip.findById(req.params.id);

        if (!trip) {
            return res.status(404).json({
                success: false,
                message: 'Trip not found'
            });
        }

        if (trip.status !== 'PENDING') {
            return res.status(400).json({
                success: false,
                message: 'Trip is no longer available'
            });
        }

        // Check if user is a driver
        if (req.user.role !== 'driver' && req.user.role !== 'admin') {
            return res.status(403).json({
                success: false,
                message: 'Only drivers can accept trips'
            });
        }

        trip.driver = req.user._id;
        trip.status = 'ACCEPTED';
        trip.acceptedAt = Date.now();
        await trip.save();

        await trip.populate('passenger', 'name phone');
        await trip.populate('driver', 'name phone');

        res.status(200).json({
            success: true,
            data: trip
        });

        // TODO: Emit socket event to passenger
        // io.to(`user_${trip.passenger._id}`).emit('trip:accepted', trip);

    } catch (error) {
        console.error('Error accepting trip:', error);
        res.status(500).json({
            success: false,
            message: 'Error accepting trip'
        });
    }
};

// @desc    Update trip status
// @route   PUT /api/trips/:id/status
// @access  Private (Driver)
exports.updateTripStatus = async (req, res) => {
    try {
        const { status } = req.body;
        const validStatuses = ['DRIVER_ARRIVING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];

        if (!validStatuses.includes(status)) {
            return res.status(400).json({
                success: false,
                message: 'Invalid status'
            });
        }

        const trip = await Trip.findById(req.params.id);

        if (!trip) {
            return res.status(404).json({
                success: false,
                message: 'Trip not found'
            });
        }

        // Update status and timestamps
        trip.status = status;
        
        if (status === 'IN_PROGRESS') {
            trip.startedAt = Date.now();
        } else if (status === 'COMPLETED') {
            trip.completedAt = Date.now();
            trip.finalCost = trip.estimatedCost; // Could calculate actual cost here
        } else if (status === 'CANCELLED') {
            trip.cancelledAt = Date.now();
            trip.cancellationReason = req.body.reason || 'No reason provided';
        }

        await trip.save();
        await trip.populate('passenger', 'name phone');
        await trip.populate('driver', 'name phone');

        res.status(200).json({
            success: true,
            data: trip
        });

        // TODO: Emit socket event
        // io.to(`trip_${trip._id}`).emit('trip:status', trip);

    } catch (error) {
        console.error('Error updating trip status:', error);
        res.status(500).json({
            success: false,
            message: 'Error updating trip status'
        });
    }
};

// @desc    Get trip by ID
// @route   GET /api/trips/:id
// @access  Private
exports.getTrip = async (req, res) => {
    try {
        const trip = await Trip.findById(req.params.id)
            .populate('passenger', 'name phone email')
            .populate('driver', 'name phone email');

        if (!trip) {
            return res.status(404).json({
                success: false,
                message: 'Trip not found'
            });
        }

        // Verify user is part of the trip
        const isParticipant = 
            trip.passenger._id.toString() === req.user._id.toString() ||
            (trip.driver && trip.driver._id.toString() === req.user._id.toString());

        if (!isParticipant && req.user.role !== 'admin') {
            return res.status(403).json({
                success: false,
                message: 'Not authorized to view this trip'
            });
        }

        res.status(200).json({
            success: true,
            data: trip
        });
    } catch (error) {
        console.error('Error fetching trip:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching trip'
        });
    }
};

// @desc    Get user's trip history
// @route   GET /api/trips/history
// @access  Private
exports.getTripHistory = async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 20;
        const skip = (page - 1) * limit;

        // Build query based on user role
        let query;
        if (req.user.role === 'driver') {
            query = { driver: req.user._id };
        } else {
            query = { passenger: req.user._id };
        }

        const trips = await Trip.find(query)
            .populate('passenger', 'name phone')
            .populate('driver', 'name phone')
            .sort({ requestedAt: -1 })
            .skip(skip)
            .limit(limit);

        const total = await Trip.countDocuments(query);

        res.status(200).json({
            success: true,
            count: trips.length,
            total,
            page,
            pages: Math.ceil(total / limit),
            data: trips
        });
    } catch (error) {
        console.error('Error fetching trip history:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching trip history'
        });
    }
};

// @desc    Get active trip for user
// @route   GET /api/trips/active
// @access  Private
exports.getActiveTrip = async (req, res) => {
    try {
        const activeStatuses = ['PENDING', 'ACCEPTED', 'DRIVER_ARRIVING', 'IN_PROGRESS'];
        
        let query;
        if (req.user.role === 'driver') {
            query = { driver: req.user._id, status: { $in: activeStatuses } };
        } else {
            query = { passenger: req.user._id, status: { $in: activeStatuses } };
        }

        const trip = await Trip.findOne(query)
            .populate('passenger', 'name phone')
            .populate('driver', 'name phone')
            .sort({ requestedAt: -1 });

        res.status(200).json({
            success: true,
            data: trip // Can be null if no active trip
        });
    } catch (error) {
        console.error('Error fetching active trip:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching active trip'
        });
    }
};

// @desc    Rate a trip
// @route   PUT /api/trips/:id/rate
// @access  Private
exports.rateTrip = async (req, res) => {
    try {
        const { rating } = req.body;

        if (!rating || rating < 1 || rating > 5) {
            return res.status(400).json({
                success: false,
                message: 'Rating must be between 1 and 5'
            });
        }

        const trip = await Trip.findById(req.params.id);

        if (!trip) {
            return res.status(404).json({
                success: false,
                message: 'Trip not found'
            });
        }

        if (trip.status !== 'COMPLETED') {
            return res.status(400).json({
                success: false,
                message: 'Can only rate completed trips'
            });
        }

        // Determine if passenger or driver is rating
        if (trip.passenger.toString() === req.user._id.toString()) {
            trip.driverRating = rating;
        } else if (trip.driver && trip.driver.toString() === req.user._id.toString()) {
            trip.passengerRating = rating;
        } else {
            return res.status(403).json({
                success: false,
                message: 'Not authorized to rate this trip'
            });
        }

        await trip.save();

        res.status(200).json({
            success: true,
            data: trip
        });
    } catch (error) {
        console.error('Error rating trip:', error);
        res.status(500).json({
            success: false,
            message: 'Error rating trip'
        });
    }
};
