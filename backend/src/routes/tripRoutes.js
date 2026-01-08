const express = require('express');
const router = express.Router();
const {
    requestTrip,
    getPendingTrips,
    acceptTrip,
    updateTripStatus,
    getTrip,
    getTripHistory,
    getActiveTrip,
    rateTrip
} = require('../controllers/tripController');
const { protect } = require('../middleware/auth');

// All routes require authentication
router.use(protect);

// Passenger routes
router.post('/request', requestTrip);
router.get('/history', getTripHistory);
router.get('/active', getActiveTrip);

// Driver routes
router.get('/pending', getPendingTrips);
router.put('/:id/accept', acceptTrip);
router.put('/:id/status', updateTripStatus);

// Shared routes
router.get('/:id', getTrip);
router.put('/:id/rate', rateTrip);

module.exports = router;
