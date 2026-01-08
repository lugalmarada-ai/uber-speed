const bcrypt = require('bcryptjs');
const mongoose = require('mongoose');
const User = require('./src/models/User');
require('dotenv').config();

const checkHash = async () => {
    const hash = '$2b$10$1S3DT5P0axBAoRYMBBh4XulQNgQx7SFjg7P3MZbSpyL6rYxNZ8deO';
    const pass = 'Okmaya3150';
    const match = await bcrypt.compare(pass, hash);
    console.log(`Does '${pass}' match hash? ${match}`);
    
    // Connect and dump users
    try {
        await mongoose.connect(process.env.MONGODB_URI);
        const users = await User.find({});
        console.log('--- ALL USERS ---');
        users.forEach(u => {
            console.log(`ID: ${u._id}, Name: ${u.name}, Email: ${u.email}, Phone: ${u.phone}`);
            console.log(`Hash: ${u.password}`);
        });
        
        // Force reset password for 'josevreyes@hotmail.com'
        const targetEmail = 'josevreyes@hotmail.com';
        const user = await User.findOne({ email: targetEmail });
        if (user) {
            const salt = await bcrypt.genSalt(10);
            const newPass = '123456';
            user.password = await bcrypt.hash(newPass, salt);
            await user.save();
            console.log(`\nRESET PASSWORD for ${targetEmail} to '${newPass}'`);
        }
        
        process.exit();
    } catch (e) {
        console.error(e);
        process.exit(1);
    }
};

checkHash();
