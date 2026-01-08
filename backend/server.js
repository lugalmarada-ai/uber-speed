const express = require('express');
const http = require('http');
const dotenv = require('dotenv');
const cors = require('cors');
const { Server } = require('socket.io');
const connectDB = require('./src/config/db');
const { initializeSocket } = require('./src/socket/socketHandler');

// Load env vars
dotenv.config();

// Connect to database
connectDB();

const app = express();
const server = http.createServer(app);

// Initialize Socket.IO
const io = new Server(server, {
    cors: {
        origin: '*',
        methods: ['GET', 'POST', 'PUT', 'DELETE']
    }
});

// Initialize socket handlers
initializeSocket(io);

// Make io available to routes
app.set('io', io);

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api/auth', require('./src/routes/authRoutes'));
app.use('/api/trips', require('./src/routes/tripRoutes'));
app.use('/api/payments', require('./src/routes/paymentRoutes'));
app.use('/api/chat', require('./src/routes/chatRoutes'));

// Basic Route
app.get('/', (req, res) => {
    res.json({
        message: 'Uber Speed API Running...',
        version: '1.0.0',
        endpoints: {
            auth: '/api/auth',
            trips: '/api/trips',
            payments: '/api/payments',
            chat: '/api/chat'
        }
    });
});

// Health Check
app.get('/health', (req, res) => {
    res.status(200).json({ status: 'OK', message: 'Service is healthy' });
});
app.get('/api/health', (req, res) => {
    res.status(200).json({ status: 'OK', message: 'API Proxy is working' });
});

// Error handling middleware
app.use((err, req, res, next) => {
    console.error('Error:', err);
    res.status(500).json({
        success: false,
        message: 'Internal server error',
        error: process.env.NODE_ENV === 'development' ? err.message : undefined
    });
});

// 404 handler
app.use((req, res) => {
    res.status(404).json({
        success: false,
        message: `Route ${req.originalUrl} not found`
    });
});

const PORT = process.env.PORT || 3000;

server.listen(PORT, () => {
    console.log(`🚀 Server running in ${process.env.NODE_ENV || 'development'} mode on port ${PORT}`);
    console.log(`📡 Socket.IO ready for connections`);
});
