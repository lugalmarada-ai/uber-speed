const express = require('express');
const router = express.Router();
const {
    getPaymentMethods,
    createPayment,
    confirmPayment,
    registerCashPayment,
    getPaymentByTrip,
    getPaymentHistory
} = require('../controllers/paymentController');
const { protect } = require('../middleware/auth');

// All routes require authentication
router.use(protect);

// Get payment methods
router.get('/methods', getPaymentMethods);

// Payment operations
router.post('/', createPayment);
router.post('/cash', registerCashPayment);
router.put('/:id/confirm', confirmPayment);

// Get payment info
router.get('/trip/:tripId', getPaymentByTrip);
router.get('/history', getPaymentHistory);

module.exports = router;
