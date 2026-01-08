const User = require('../models/User');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

// @desc    Register user
// @route   POST /api/auth/register
// @access  Public
const register = async (req, res) => {
    try {
        const { name, email, password, phone, role } = req.body;

        // Check if user exists
        const userExists = await User.findOne({ email });

        if (userExists) {
            return res.status(400).json({ message: 'User already exists' });
        }

        // Hash password
        console.log('------------------------------------------------');
        console.log('REGISTER START for:', email);
        console.log('Register Pass received:', password);
        
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);
        console.log('Generated Hash:', hashedPassword);

        // Create user
        const user = await User.create({
            name,
            email,
            password: hashedPassword,
            phone,
            role
        });

        if (user) {
            res.status(201).json({
                success: true,
                token: generateToken(user._id),
                user: {
                    id: user.id,
                    name: user.name,
                    email: user.email,
                    role: user.role
                }
            });
        } else {
            res.status(400).json({ message: 'Invalid user data' });
        }
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Server Error: ' + error.message });
    }
};

// @desc    Authenticate a user
// @route   POST /api/auth/login
// @access  Public
const login = async (req, res) => {
    try {
        const { email, password } = req.body;
        console.log('------------------------------------------------');
        console.log('LOGIN START for:', email);
        console.log('Login Pass received:', password);

        // Check for user by email OR phone (input 'email' might be a phone number)
        const isEmail = email.includes('@');
        const query = isEmail ? { email } : { phone: email };

        const user = await User.findOne(query).select('+password');
        console.log('User found:', !!user);
        
        if (user) {
             console.log('Stored DB Hash:', user.password);
             const isMatch = await bcrypt.compare(password, user.password);
             console.log('Bcrypt Compare Result:', isMatch);
             
             if (isMatch) {
                 res.json({
                    success: true,
                    token: generateToken(user._id),
                    user: {
                        id: user.id,
                        name: user.name,
                        email: user.email,
                        role: user.role
                    }
                });
             } else {
                 res.status(401).json({ message: 'Invalid credentials' });
             }
        } else {
             // User not found
             res.status(401).json({ message: 'Invalid credentials' });
        }
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Server Error' });
    }
};

// @desc    Get user data
// @route   GET /api/auth/me
// @access  Private
const getMe = async (req, res) => {
    res.status(200).json(req.user);
};

// Generate JWT
const generateToken = (id) => {
    return jwt.sign({ id }, process.env.JWT_SECRET, {
        expiresIn: '30d'
    });
};

module.exports = {
    register,
    login,
    getMe
};
