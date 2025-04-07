const mongoose = require('mongoose');

const recommendationSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    date: {
        type: Date,
        required: true,
        default: Date.now
    },
    type: {
        type: String,
        required: true,
        enum: ['meal', 'nutrient', 'supplement']
    },
    status: {
        type: String,
        required: true,
        enum: ['pending', 'accepted', 'rejected'],
        default: 'pending'
    },
    nutritionalContext: {
        deficiencies: [{
            nutrient: String,
            currentLevel: Number,
            recommendedLevel: Number,
            severity: {
                type: String,
                enum: ['low', 'moderate', 'high']
            }
        }],
        excesses: [{
            nutrient: String,
            currentLevel: Number,
            recommendedLevel: Number,
            severity: {
                type: String,
                enum: ['low', 'moderate', 'high']
            }
        }]
    },
    recommendations: [{
        type: {
            type: String,
            required: true,
            enum: ['food', 'meal', 'supplement']
        },
        item: {
            name: String,
            servingSize: Number,
            servingUnit: String,
            nutrients: {
                calories: Number,
                protein: Number,
                carbohydrates: Number,
                fat: Number,
                vitamins: Map,
                minerals: Map
            },
            reasoning: String,
            priority: {
                type: Number,
                min: 1,
                max: 5
            }
        },
        alternatives: [{
            name: String,
            servingSize: Number,
            servingUnit: String,
            nutrients: {
                calories: Number,
                protein: Number,
                carbohydrates: Number,
                fat: Number,
                vitamins: Map,
                minerals: Map
            },
            reasoning: String
        }]
    }],
    userPreferences: {
        dietaryRestrictions: [String],
        allergies: [String],
        pricePreference: {
            type: String,
            enum: ['low', 'moderate', 'high']
        },
        cuisinePreferences: [String]
    },
    feedback: {
        userRating: {
            type: Number,
            min: 1,
            max: 5
        },
        comments: String,
        followedRecommendation: Boolean
    },
    expiresAt: {
        type: Date,
        required: true,
        default: () => new Date(+new Date() + 7*24*60*60*1000) // 7 days from creation
    }
}, {
    timestamps: true
});

// Index for efficient querying
recommendationSchema.index({ userId: 1, date: -1 });
recommendationSchema.index({ userId: 1, status: 1 });

// Automatically expire documents after their expiration date
recommendationSchema.index({ expiresAt: 1 }, { expireAfterSeconds: 0 });

module.exports = mongoose.model('Recommendation', recommendationSchema);