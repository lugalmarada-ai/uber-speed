const express = require('express');
const router = express.Router();
const {
    sendMessage,
    getMessages,
    markAsRead,
    getUnreadCount
} = require('../controllers/chatController');
const { protect } = require('../middleware/auth');

// All routes require authentication
router.use(protect);

// Chat operations
router.post('/messages', sendMessage);
router.get('/messages/:tripId', getMessages);
router.put('/messages/:tripId/read', markAsRead);
router.get('/unread/:tripId', getUnreadCount);

module.exports = router;
