const mongoose = require('mongoose');

const nutritionLogSchema = new mongoose.Schema({
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
    meals: [{
        name: {
            type: String,
            required: true,
            enum: ['breakfast', 'lunch', 'dinner', 'snack']
        },
        foods: [{
            name: {
                type: String,
                required: true
            },
            servingSize: {
                type: Number,
                required: true
            },
            servingUnit: {
                type: String,
                required: true
            },
            nutrients: {
                calories: Number,
                protein: Number,
                carbohydrates: Number,
                fat: Number,
                fiber: Number,
                sugar: Number,
                sodium: Number,
                cholesterol: Number,
                vitamins: {
                    a: Number,
                    c: Number,
                    d: Number,
                    e: Number,
                    k: Number,
                    b6: Number,
                    b12: Number
                },
                minerals: {
                    calcium: Number,
                    iron: Number,
                    magnesium: Number,
                    zinc: Number,
                    potassium: Number
                }
            }
        }]
    }],
    dailyTotals: {
        calories: Number,
        protein: Number,
        carbohydrates: Number,
        fat: Number,
        fiber: Number,
        sugar: Number,
        sodium: Number,
        cholesterol: Number,
        vitamins: {
            a: Number,
            c: Number,
            d: Number,
            e: Number,
            k: Number,
            b6: Number,
            b12: Number
        },
        minerals: {
            calcium: Number,
            iron: Number,
            magnesium: Number,
            zinc: Number,
            potassium: Number
        }
    },
    goals: {
        calories: Number,
        protein: Number,
        carbohydrates: Number,
        fat: Number,
        fiber: Number
    },
    notes: String
}, {
    timestamps: true
});

// Calculate daily totals before saving
nutritionLogSchema.pre('save', function(next) {
    const totals = {
        calories: 0,
        protein: 0,
        carbohydrates: 0,
        fat: 0,
        fiber: 0,
        sugar: 0,
        sodium: 0,
        cholesterol: 0,
        vitamins: {
            a: 0, c: 0, d: 0, e: 0, k: 0, b6: 0, b12: 0
        },
        minerals: {
            calcium: 0, iron: 0, magnesium: 0, zinc: 0, potassium: 0
        }
    };

    this.meals.forEach(meal => {
        meal.foods.forEach(food => {
            if (food.nutrients) {
                Object.keys(totals).forEach(key => {
                    if (typeof totals[key] === 'number') {
                        totals[key] += food.nutrients[key] || 0;
                    } else if (typeof totals[key] === 'object') {
                        Object.keys(totals[key]).forEach(subKey => {
                            totals[key][subKey] += (food.nutrients[key]?.[subKey] || 0);
                        });
                    }
                });
            }
        });
    });

    this.dailyTotals = totals;
    next();
});

module.exports = mongoose.model('NutritionLog', nutritionLogSchema);