const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const userSchema = new mongoose.Schema({
    email: {
        type: String,
        required: true,
        unique: true,
        trim: true,
        lowercase: true
    },
    password: {
        type: String,
        required: true
    },
    name: {
        type: String,
        required: true,
        trim: true
    },
    profile: {
        age: {
            type: Number,
            min: 1,
            max: 120,
            required: false,
            validate: {
                validator: Number.isInteger,
                message: 'Age must be a whole number'
            }
        },
        gender: {
            type: String,
            enum: ['male', 'female', 'other'],
            required: false
        },
        height: {
            type: Number,
            min: 50,
            max: 300,
            required: false,
            validate: {
                validator: function(v) {
                    return !v || v > 0;
                },
                message: 'Height must be a positive number'
            }
        },
        weight: {
            type: Number,
            min: 20,
            max: 500,
            required: false,
            validate: {
                validator: function(v) {
                    return !v || v > 0;
                },
                message: 'Weight must be a positive number'
            }
        },
        activityLevel: {
            type: String,
            enum: ['Sedentary', 'Lightly Active', 'Moderately Active', 'Active', 'Very Active'],
            required: false
        },
        dietaryPreferences: [{
            type: String,
            trim: true
        }],
        allergies: [{
            type: String,
            trim: true
        }],
        isPregnant: {
            type: Boolean,
            default: false
        },
        healthMetrics: {
            bmi: Number,
            bmiCategory: String,
            dailyCalories: Number,
            nutritionNeeds: {
                protein: Number,
                carbs: Number,
                fats: Number
            }
        },
        lastUpdated: {
            type: Date,
            default: Date.now
        }
    }
});

// Hash password before saving
userSchema.pre('save', async function(next) {
    if (!this.isModified('password')) return next();
    try {
        const salt = await bcrypt.genSalt(10);
        this.password = await bcrypt.hash(this.password, salt);
        next();
    } catch (error) {
        next(error);
    }
});

// Calculate BMI and health metrics before saving
userSchema.pre('save', function(next) {
    if (this.height && this.weight) {
        // Calculate BMI
        const heightInMeters = this.height / 100;
        const bmi = this.weight / (heightInMeters * heightInMeters);
        this.healthMetrics = this.healthMetrics || {};
        this.healthMetrics.bmi = Math.round(bmi * 10) / 10;

        // Determine BMI category
        if (bmi < 18.5) this.healthMetrics.bmiCategory = 'Underweight';
        else if (bmi < 25) this.healthMetrics.bmiCategory = 'Normal';
        else if (bmi < 30) this.healthMetrics.bmiCategory = 'Overweight';
        else this.healthMetrics.bmiCategory = 'Obese';

        // Calculate daily calories based on activity level
        let bmr;
        if (this.gender === 'male') {
            bmr = 88.362 + (13.397 * this.weight) + (4.799 * this.height) - (5.677 * this.age);
        } else {
            bmr = 447.593 + (9.247 * this.weight) + (3.098 * this.height) - (4.330 * this.age);
        }

        const activityMultipliers = {
            'Sedentary': 1.2,
            'Lightly Active': 1.375,
            'Moderately Active': 1.55,
            'Active': 1.725,
            'Very Active': 1.9
        };

        let dailyCalories = bmr * activityMultipliers[this.activityLevel];
        
        // Adjust for pregnancy
        if (this.isPregnant) {
            dailyCalories += 300; // Additional calories for pregnancy
        }

        this.healthMetrics.dailyCalories = Math.round(dailyCalories);

        // Calculate macronutrient needs
        this.healthMetrics.nutritionNeeds = {
            protein: Math.round((dailyCalories * 0.25) / 4), // 25% of calories from protein
            carbs: Math.round((dailyCalories * 0.45) / 4),   // 45% of calories from carbs
            fats: Math.round((dailyCalories * 0.30) / 9)     // 30% of calories from fats
        };
    }
    this.lastUpdated = Date.now();
    next();
})

// Compare password method
userSchema.methods.comparePassword = async function(candidatePassword) {
    try {
        return await bcrypt.compare(candidatePassword, this.password);
    } catch (error) {
        throw error;
    }
};

module.exports = mongoose.model('User', userSchema);