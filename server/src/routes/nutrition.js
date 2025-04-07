const express = require('express');
const router = express.Router();
const NutritionLog = require('../models/NutritionLog');
const Recommendation = require('../models/Recommendation');
const User = require('../models/User');
const jwt = require('jsonwebtoken');

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

// Log meal
router.post('/log', authenticateToken, async (req, res) => {
    try {
        const { date, meal, foods } = req.body;
        
        let nutritionLog = await NutritionLog.findOne({
            userId: req.user._id,
            date: new Date(date).setHours(0, 0, 0, 0)
        });

        if (!nutritionLog) {
            nutritionLog = new NutritionLog({
                userId: req.user._id,
                date: new Date(date).setHours(0, 0, 0, 0),
                meals: []
            });
        }

        nutritionLog.meals.push({
            name: meal,
            foods: foods
        });

        await nutritionLog.save();

        // Generate new recommendations based on updated nutrition log
        await generateRecommendations(req.user._id, nutritionLog);

        res.json(nutritionLog);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// Get nutrition logs for a date range
router.get('/logs', authenticateToken, async (req, res) => {
    try {
        const { startDate, endDate } = req.query;
        const logs = await NutritionLog.find({
            userId: req.user._id,
            date: {
                $gte: new Date(startDate).setHours(0, 0, 0, 0),
                $lte: new Date(endDate).setHours(23, 59, 59, 999)
            }
        }).sort({ date: 1 });

        res.json(logs);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// Get recommendations
router.get('/recommendations', authenticateToken, async (req, res) => {
    try {
        const recommendations = await Recommendation.find({
            userId: req.user._id,
            status: 'pending',
            expiresAt: { $gt: new Date() }
        }).sort({ createdAt: -1 });

        res.json(recommendations);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// Update recommendation status
router.patch('/recommendations/:id', authenticateToken, async (req, res) => {
    try {
        const { status, feedback } = req.body;
        const recommendation = await Recommendation.findOneAndUpdate(
            { _id: req.params.id, userId: req.user._id },
            { 
                status,
                feedback,
                $set: { 'feedback.followedRecommendation': status === 'accepted' }
            },
            { new: true }
        );

        if (!recommendation) {
            return res.status(404).json({ message: 'Recommendation not found' });
        }

        res.json(recommendation);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// Helper function to generate recommendations
async function generateRecommendations(userId, nutritionLog) {
    try {
        const user = await User.findById(userId);
        const deficiencies = [];
        const excesses = [];

        // Calculate deficiencies and excesses based on daily totals and user's goals
        const { dailyTotals, goals } = nutritionLog;
        
        // Basic nutrient analysis
        if (dailyTotals.calories < (goals.calories * 0.9)) {
            deficiencies.push({
                nutrient: 'calories',
                currentLevel: dailyTotals.calories,
                recommendedLevel: goals.calories,
                severity: 'moderate'
            });
        }

        // Create recommendation
        const recommendation = new Recommendation({
            userId,
            type: 'nutrient',
            nutritionalContext: {
                deficiencies,
                excesses
            },
            recommendations: generateFoodRecommendations(deficiencies, excesses, user.profile.dietaryPreferences),
            userPreferences: {
                dietaryRestrictions: user.profile.dietaryRestrictions,
                allergies: user.profile.allergies,
                pricePreference: user.profile.pricePreference,
                cuisinePreferences: user.profile.cuisinePreferences
            }
        });

        await recommendation.save();
    } catch (error) {
        console.error('Error generating recommendations:', error);
    }
}

// Helper function to generate food recommendations
function generateFoodRecommendations(deficiencies, excesses, preferences) {
    // Implement recommendation logic based on nutritional needs and preferences
    const recommendations = [];
    
    // Example recommendation
    if (deficiencies.find(d => d.nutrient === 'calories')) {
        recommendations.push({
            type: 'food',
            item: {
                name: 'Greek Yogurt with Honey',
                servingSize: 1,
                servingUnit: 'cup',
                nutrients: {
                    calories: 200,
                    protein: 15,
                    carbohydrates: 25,
                    fat: 8
                },
                reasoning: 'High in protein and healthy calories',
                priority: 4
            },
            alternatives: [
                {
                    name: 'Banana with Peanut Butter',
                    servingSize: 1,
                    servingUnit: 'serving',
                    nutrients: {
                        calories: 190,
                        protein: 7,
                        carbohydrates: 28,
                        fat: 8
                    },
                    reasoning: 'Good source of energy and protein'
                }
            ]
        });
    }

    return recommendations;
}

module.exports = router;