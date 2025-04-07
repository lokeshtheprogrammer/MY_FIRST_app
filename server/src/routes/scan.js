const express = require('express');
const router = express.Router();
const { ClarifaiStub, grpc } = require('clarifai-nodejs-grpc');
const axios = require('axios');
const mongoose = require('mongoose');

// Initialize Clarifai
const stub = ClarifaiStub.grpc();
const metadata = new grpc.Metadata();
metadata.set('authorization', `Key ${process.env.CLARIFAI_PAT}`);

// Import Scan model
const Scan = require('../models/Scan');

// Authentication middleware (reuse from auth.js)
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

// Scan food image and get nutrition info
router.post('/scan', authenticateToken, async (req, res) => {
    try {
        const { image } = req.body;
        
        // Call Clarifai API for food recognition
        const response = await new Promise((resolve, reject) => {
            stub.PostModelOutputs(
                {
                    model_id: "food-item-recognition",
                    inputs: [{ data: { image: { base64: image } } }]
                },
                metadata,
                (err, response) => {
                    if (err) reject(err);
                    resolve(response);
                }
            );
        });

        if (!response.outputs[0].data.concepts.length) {
            return res.status(400).json({ message: 'No food items detected' });
        }

        const foodItem = response.outputs[0].data.concepts[0].name;

        // Call Edamam API for nutrition info
        const nutritionResponse = await axios.get(
            `https://api.edamam.com/api/nutrition-data?app_id=${process.env.APP_ID}&app_key=${process.env.APP_KEY}&ingr=${encodeURIComponent(foodItem)}`
        );

        const nutrients = {
            calories: nutritionResponse.data.calories || 0,
            protein: nutritionResponse.data.totalNutrients.PROCNT?.quantity || 0,
            carbs: nutritionResponse.data.totalNutrients.CHOCDF?.quantity || 0,
            fat: nutritionResponse.data.totalNutrients.FAT?.quantity || 0
        };

        // Save scan result
        const scan = new Scan({
            userId: req.user._id,
            foodName: foodItem,
            nutrients
        });

        await scan.save();

        res.json({
            status: 'success',
            data: {
                foodName: foodItem,
                nutrients
            }
        });

    } catch (error) {
        console.error('Scan error:', error);
        res.status(500).json({
            status: 'error',
            message: 'Error processing food scan'
        });
    }
});

// Get scan history
router.get('/history', authenticateToken, async (req, res) => {
    try {
        const scans = await Scan.find({ userId: req.user._id })
            .sort({ timestamp: -1 })
            .limit(20);

        res.json({
            status: 'success',
            data: scans
        });
    } catch (error) {
        res.status(500).json({
            status: 'error',
            message: 'Error fetching scan history'
        });
    }
});

module.exports = router;