const Payment = require('../models/Payment');
const Trip = require('../models/Trip');

// @desc    Get available payment methods
// @route   GET /api/payments/methods
// @access  Private
exports.getPaymentMethods = async (req, res) => {
    try {
        const methods = [
            {
                id: 'EFECTIVO',
                name: 'Efectivo',
                description: 'Pago en efectivo al conductor',
                icon: 'cash',
                currencies: ['USD', 'VES'],
                requiresReference: false
            },
            {
                id: 'PAGO_MOVIL',
                name: 'Pago Móvil',
                description: 'Transferencia bancaria instantánea',
                icon: 'phone',
                currencies: ['VES'],
                requiresReference: true,
                bankInfo: {
                    // This should come from driver's profile in production
                    message: 'Los datos bancarios se mostrarán al confirmar el viaje'
                }
            },
            {
                id: 'ZELLE',
                name: 'Zelle',
                description: 'Transferencia Zelle',
                icon: 'credit_card',
                currencies: ['USD'],
                requiresReference: true
            },
            {
                id: 'BINANCE_PAY',
                name: 'Binance Pay',
                description: 'Pago con criptomonedas vía Binance',
                icon: 'currency_bitcoin',
                currencies: ['USD'],
                requiresReference: true
            }
        ];

        res.status(200).json({
            success: true,
            data: methods
        });
    } catch (error) {
        console.error('Error fetching payment methods:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching payment methods'
        });
    }
};

// @desc    Create payment for a trip
// @route   POST /api/payments
// @access  Private (Passenger)
exports.createPayment = async (req, res) => {
    try {
        const { tripId, method, amount, currency, reference, bankCode, phoneNumber } = req.body;

        // Validate trip exists
        const trip = await Trip.findById(tripId);
        if (!trip) {
            return res.status(404).json({
                success: false,
                message: 'Trip not found'
            });
        }

        // Verify passenger owns this trip
        if (trip.passenger.toString() !== req.user._id.toString()) {
            return res.status(403).json({
                success: false,
                message: 'Not authorized'
            });
        }

        // Check if payment already exists
        const existingPayment = await Payment.findOne({ trip: tripId, status: { $ne: 'REJECTED' } });
        if (existingPayment) {
            return res.status(400).json({
                success: false,
                message: 'Payment already exists for this trip'
            });
        }

        const payment = await Payment.create({
            trip: tripId,
            payer: req.user._id,
            receiver: trip.driver,
            method,
            amount: amount || trip.finalCost || trip.estimatedCost,
            currency: currency || 'USD',
            reference: reference || null,
            bankCode: bankCode || null,
            phoneNumber: phoneNumber || null,
            status: method === 'EFECTIVO' ? 'PENDING' : 'PENDING'
        });

        // Update trip payment status
        trip.paymentMethod = method;
        trip.paymentReference = reference;
        await trip.save();

        res.status(201).json({
            success: true,
            data: payment
        });

    } catch (error) {
        console.error('Error creating payment:', error);
        res.status(500).json({
            success: false,
            message: 'Error creating payment'
        });
    }
};

// @desc    Confirm payment received (Driver)
// @route   PUT /api/payments/:id/confirm
// @access  Private (Driver)
exports.confirmPayment = async (req, res) => {
    try {
        const payment = await Payment.findById(req.params.id);

        if (!payment) {
            return res.status(404).json({
                success: false,
                message: 'Payment not found'
            });
        }

        // Verify driver owns this payment
        if (payment.receiver.toString() !== req.user._id.toString() && req.user.role !== 'admin') {
            return res.status(403).json({
                success: false,
                message: 'Not authorized to confirm this payment'
            });
        }

        payment.status = 'CONFIRMED';
        payment.confirmedBy = req.user.role === 'admin' ? 'ADMIN' : 'DRIVER';
        payment.confirmedAt = Date.now();
        await payment.save();

        // Update trip payment status
        await Trip.findByIdAndUpdate(payment.trip, { paymentStatus: 'CONFIRMED' });

        res.status(200).json({
            success: true,
            data: payment
        });

    } catch (error) {
        console.error('Error confirming payment:', error);
        res.status(500).json({
            success: false,
            message: 'Error confirming payment'
        });
    }
};

// @desc    Register cash payment
// @route   POST /api/payments/cash
// @access  Private (Driver)
exports.registerCashPayment = async (req, res) => {
    try {
        const { tripId, amount, currency, notes } = req.body;

        const trip = await Trip.findById(tripId);
        if (!trip) {
            return res.status(404).json({
                success: false,
                message: 'Trip not found'
            });
        }

        // Verify driver owns this trip
        if (trip.driver.toString() !== req.user._id.toString()) {
            return res.status(403).json({
                success: false,
                message: 'Not authorized'
            });
        }

        const payment = await Payment.create({
            trip: tripId,
            payer: trip.passenger,
            receiver: req.user._id,
            method: 'EFECTIVO',
            amount: amount || trip.finalCost || trip.estimatedCost,
            currency: currency || 'USD',
            status: 'CONFIRMED',
            confirmedBy: 'DRIVER',
            confirmedAt: Date.now(),
            notes: notes || 'Pago en efectivo'
        });

        // Update trip
        trip.paymentStatus = 'CONFIRMED';
        trip.paymentMethod = 'EFECTIVO';
        await trip.save();

        res.status(201).json({
            success: true,
            data: payment
        });

    } catch (error) {
        console.error('Error registering cash payment:', error);
        res.status(500).json({
            success: false,
            message: 'Error registering payment'
        });
    }
};

// @desc    Get payment by trip
// @route   GET /api/payments/trip/:tripId
// @access  Private
exports.getPaymentByTrip = async (req, res) => {
    try {
        const payment = await Payment.findOne({ trip: req.params.tripId })
            .populate('payer', 'name')
            .populate('receiver', 'name');

        res.status(200).json({
            success: true,
            data: payment // Can be null
        });
    } catch (error) {
        console.error('Error fetching payment:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching payment'
        });
    }
};

// @desc    Get payment history
// @route   GET /api/payments/history
// @access  Private
exports.getPaymentHistory = async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 20;
        const skip = (page - 1) * limit;

        // Get payments where user is payer or receiver
        const query = {
            $or: [
                { payer: req.user._id },
                { receiver: req.user._id }
            ]
        };

        const payments = await Payment.find(query)
            .populate('trip', 'origin destination')
            .populate('payer', 'name')
            .populate('receiver', 'name')
            .sort({ createdAt: -1 })
            .skip(skip)
            .limit(limit);

        const total = await Payment.countDocuments(query);

        res.status(200).json({
            success: true,
            count: payments.length,
            total,
            page,
            pages: Math.ceil(total / limit),
            data: payments
        });
    } catch (error) {
        console.error('Error fetching payment history:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching payment history'
        });
    }
};
