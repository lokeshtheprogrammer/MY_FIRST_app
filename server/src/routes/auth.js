const express = require('express');
const router = express.Router();
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs'); // Make sure this is added
const User = require('../models/User');

// Authentication middleware
const authenticateToken = async (req, res, next) => {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader) {
            return res.status(401).json({ message: 'No token provided' });
        }

        const token = authHeader.split(' ')[1];
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        const user = await User.findById(decoded.userId);
        
        if (!user) {
            return res.status(401).json({ message: 'Invalid token' });
        }

        req.user = user;
        next();
    } catch (error) {
        return res.status(401).json({ message: 'Invalid token' });
    }
};

// Profile update route
router.put('/profile', authenticateToken, async (req, res) => {
    try {
        const { name, age, gender, height, weight, activityLevel, isPregnant, dietaryPreferences, allergies } = req.body;
        const user = req.user;

        // Ensure user exists
        if (!user) {
            return res.status(404).json({
                status: 'error',
                message: 'User not found'
            });
        }

        // Initialize profile if it doesn't exist
        if (!user.profile) {
            user.profile = {};
        }

        // Validate and sanitize input data
        const sanitizedData = {};

        // Handle required fields
        if (height !== undefined) {
            if (isNaN(height) || height <= 0) {
                return res.status(400).json({
                    status: 'error',
                    message: 'Height must be a valid positive number'
                });
            }
            sanitizedData.height = Number(height);
        }

        if (weight !== undefined) {
            if (isNaN(weight) || weight <= 0) {
                return res.status(400).json({
                    status: 'error',
                    message: 'Weight must be a valid positive number'
                });
            }
            sanitizedData.weight = Number(weight);
        }

        if (gender !== undefined) {
            console.log('Validating gender:', gender);
            const normalizedGender = gender.toLowerCase();
            if (!['male', 'female', 'other'].includes(normalizedGender)) {
                const errorMsg = 'Gender must be one of: male, female, other';
                console.error('Gender validation failed:', errorMsg);
                return res.status(400).json({
                    status: 'error',
                    message: errorMsg
                });
            }
            console.log('Gender validation passed');
            sanitizedData.gender = normalizedGender;
        }

        if (activityLevel !== undefined) {
            if (!['Sedentary', 'Lightly Active', 'Moderately Active', 'Active', 'Very Active'].includes(activityLevel)) {
                return res.status(400).json({
                    status: 'error',
                    message: 'Invalid activity level'
                });
            }
            sanitizedData.activityLevel = activityLevel;
        }

        // Handle optional fields
        if (age !== undefined) {
            const numAge = Number(age);
            if (!isNaN(numAge) && numAge > 0 && numAge <= 120) {
                sanitizedData.age = numAge;
            }
        }

        if (name !== undefined) {
            sanitizedData.name = String(name).trim();
        }

        if (isPregnant !== undefined) {
            sanitizedData.isPregnant = Boolean(isPregnant);
        }

        if (Array.isArray(dietaryPreferences)) {
            sanitizedData.dietaryPreferences = dietaryPreferences.map(pref => String(pref).trim()).filter(Boolean);
        }

        if (Array.isArray(allergies)) {
            sanitizedData.allergies = allergies.map(allergy => String(allergy).trim()).filter(Boolean);
        }

        sanitizedData.lastUpdated = new Date();

        // Update user profile
        Object.assign(user.profile, sanitizedData);

        // Calculate BMI if height and weight are available
        let bmiData = {};
        if (user.profile.height && user.profile.weight) {
            const heightInMeters = user.profile.height / 100;
            const bmi = user.profile.weight / (heightInMeters * heightInMeters);
            bmiData.bmi = parseFloat(bmi.toFixed(2));
            
            if (bmi < 18.5) bmiData.bmiCategory = 'Underweight';
            else if (bmi < 25) bmiData.bmiCategory = 'Normal weight';
            else if (bmi < 30) bmiData.bmiCategory = 'Overweight';
            else bmiData.bmiCategory = 'Obese';
        }

        // Save user with error handling
        try {
            await user.save();
            
            // Convert profile to plain object safely
            const profileObject = user.profile.toObject ? user.profile.toObject() : { ...user.profile };

            res.json({
                status: 'success',
                profile: {
                    ...profileObject,
                    ...bmiData
                }
            });
        } catch (saveError) {
            console.error('Error saving user profile:', saveError);
            return res.status(500).json({
                status: 'error',
                message: 'Failed to save profile changes',
                details: process.env.NODE_ENV === 'development' ? saveError.message : undefined
            });
        }
    } catch (error) {
        console.error('Profile update error:', error);
        res.status(500).json({
            status: 'error',
            message: 'An unexpected error occurred while updating profile',
            details: process.env.NODE_ENV === 'development' ? error.message : undefined
        });
    }
});

// Input validation middleware
const validateEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
};

const validatePassword = (password) => {
    return password.length >= 8;
};

const validateUserInput = (req, res, next) => {
    const { email, password } = req.body;

    if (!email || !password) {
        return res.status(400).json({
            status: 'error',
            code: 'MISSING_FIELDS',
            message: 'Email and password are required'
        });
    }

    if (!validateEmail(email)) {
        return res.status(400).json({
            status: 'error',
            code: 'INVALID_EMAIL',
            message: 'Please provide a valid email address'
        });
    }

    if (!validatePassword(password)) {
        return res.status(400).json({
            status: 'error',
            code: 'INVALID_PASSWORD',
            message: 'Password must be at least 8 characters long'
        });
    }

    next();
};

// Sign in route
router.post('/signin', validateUserInput, async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({ message: 'Email and password are required' });
        }

        // Find user by email
        const user = await User.findOne({ email }).select('+password');
        if (!user) {
            return res.status(401).json({ message: 'Invalid email or password' });
        }

        // Check password
        const isValidPassword = await user.comparePassword(password);
        if (!isValidPassword) {
            return res.status(401).json({ message: 'Invalid email or password' });
        }

        // Generate JWT token
        const token = jwt.sign(
            { userId: user._id },
            process.env.JWT_SECRET,
            { expiresIn: '24h' }
        );

        // Return user profile and token
        res.json({
            token,
            user: {
                id: user._id,
                email: user.email,
                profile: user.profile || {}
            }
        });
    } catch (error) {
        console.error('Sign in error:', error);
        res.status(500).json({
            status: 'error',
            code: 'SERVER_ERROR',
            message: 'An unexpected error occurred'
        });
    }
});

// Sign up route
router.post('/signup', validateUserInput, async (req, res) => {
    try {
        console.log('🚀 Starting user registration process...');
        const { email, password, name } = req.body;
        console.log('📝 Registration data:', { email, name });

        // Check if user already exists
        const existingUser = await User.findOne({ email });
        if (existingUser) {
            return res.status(400).json({ message: 'User already exists' });
        }

        // Create new user with minimal required fields
        const user = new User({
            email,
            password, // Will be hashed by the pre-save middleware
            name,
            profile: {
                name: name // Initialize profile name with the provided name
            }
        });

        console.log('👤 Attempting to save user:', { email, name });
        await user.save();

        // Generate JWT token
        const token = jwt.sign(
            { userId: user._id },
            process.env.JWT_SECRET,
            { expiresIn: '24h' }
        );

        res.status(201).json({
            message: 'User registered successfully',
            token: token,
            profile: {
                name: user.profile.name,
                email: user.profile.email
            }
        });

    } catch (error) {
        console.warn('❌ Sign up error:', error);
        console.log('Full error details:', {
            name: error.name,
            code: error.code,
            message: error.message,
            stack: error.stack
        });
        res.status(500).json({ message: 'Error registering user', error: error.message });
    }
});

// Logout route
router.post('/logout', async (req, res) => {
    try {
        // Since we're using JWT, we don't need to do anything server-side
        // The client will handle removing the token
        console.log('User logged out');
        res.json({ 
            status: 'success',
            message: 'Logged out successfully' 
        });
    } catch (error) {
        console.error('Logout error:', error);
        res.status(500).json({
            status: 'error',
            code: 'SERVER_ERROR',
            message: 'An unexpected error occurred'
        });
    }
});

// Save user details route
router.post('/save-details', authenticateToken, async (req, res) => {
    try {
        const { age, height, weight, dietaryPreferences, allergies } = req.body;
        const user = req.user;

        // Update user profile
        user.profile = {
            name: user.name,
            age: age || user.profile?.age,
            height: height || user.profile?.height,
            weight: weight || user.profile?.weight,
            dietaryPreferences: dietaryPreferences || user.profile?.dietaryPreferences || [],
            allergies: allergies || user.profile?.allergies || []
        };

        await user.save();
        console.log('✅ User details saved successfully');

        res.json({
            status: 'success',
            message: 'User details saved successfully',
            profile: user.profile
        });
    } catch (error) {
        console.error('❌ Error saving user details:', error);
        res.status(500).json({
            status: 'error',
            message: 'Failed to save user details'
        });
    }
});

module.exports = router;