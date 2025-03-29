const express = require('express');
const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const cors = require('cors');
const axios = require('axios');
const { ClarifaiStub, grpc } = require('clarifai-nodejs-grpc');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET;
const CLARIFAI_PAT = process.env.CLARIFAI_PAT;
const EDAMAM_APP_ID = process.env.EDAMAM_APP_ID;
const EDAMAM_APP_KEY = process.env.EDAMAM_APP_KEY;

app.use(cors());
app.use(express.json());

// MongoDB Connection
mongoose.connect(process.env.MONGO_URI)
    .then(() => console.log('MongoDB connected successfully'))
    .catch(err => console.error('Database connection error:', err));

// User Schema
const userSchema = new mongoose.Schema({
    name: { type: String, required: true },
    email: { type: String, required: true, unique: true },
    password: { type: String, required: true },
    role: { type: String, default: 'user' },
    age: Number,
    gender: String,
    height: Number,
    weight: Number,
    activityLevel: String,
    isPregnant: Boolean,
    scans: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Scan' }]
});

const User = mongoose.model('User', userSchema);

// Scan Schema (No image field)
const scanSchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    foodName: { type: String, required: true },
    nutrients: {
        calories: { type: Number, required: true },
        protein: { type: Number, required: true },
        carbs: { type: Number, required: true },
        fat: { type: Number, required: true }
    },
    timestamp: { type: Date, default: Date.now }
});

const Scan = mongoose.model('Scan', scanSchema);

// Middleware to Authenticate JWT
const authenticateToken = (req, res, next) => {
    const token = req.headers['authorization']?.split(' ')[1];
    if (!token) return res.status(401).json({ message: 'No token provided' });

    jwt.verify(token, JWT_SECRET, (err, user) => {
        if (err) return res.status(403).json({ message: 'Invalid token' });
        req.user = user;
        console.log('Authenticated user:', req.user);
        next();
    });
};

// Register User
app.post('/auth/register', async (req, res) => {
    try {
        const { name, email, password, role } = req.body;

        let user = await User.findOne({ email });
        if (user) return res.status(400).json({ message: 'User already exists' });

        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        user = new User({ name, email, password: hashedPassword, role: role || 'user' });
        await user.save();

        const token = jwt.sign({ userId: user._id, role: user.role }, JWT_SECRET, { expiresIn: '1h' });
        console.log(`Signup successful for ${name}, token: ${token}`);

        res.status(201).json({
            message: 'User registered successfully',
            token,
            profile: { id: user._id, name: user.name, email: user.email, role: user.role }
        });
    } catch (error) {
        console.error('🔥 Registration Error:', error.stack);
        res.status(500).json({ message: 'Server error', error: error.message });
    }
});

// Login User
app.post('/auth/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        const user = await User.findOne({ email });
        if (!user) return res.status(400).json({ message: 'Invalid credentials' });

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) return res.status(400).json({ message: 'Invalid credentials' });

        const token = jwt.sign({ userId: user._id, role: user.role }, JWT_SECRET, { expiresIn: '1h' });
        console.log(`Login successful for ${user.name}, token: ${token}`);

        res.json({
            token,
            profile: { id: user._id, name: user.name, email: user.email, role: user.role }
        });
    } catch (error) {
        console.error('🔥 Login Error:', error.stack);
        res.status(500).json({ message: 'Server error', error: error.message });
    }
});

// Update User Details
app.post('/user/details', authenticateToken, async (req, res) => {
    try {
        const { userId, name, age, gender, height, weight, activityLevel, isPregnant } = req.body;

        // Update user details
        const updatedUser = await User.findByIdAndUpdate(
            userId,
            {
                name,
                age,
                gender,
                height,
                weight,
                activityLevel,
                isPregnant
            },
            { new: true }
        );

        if (!updatedUser) {
            return res.status(404).json({
                success: false,
                message: 'User not found'
            });
        }

        // Calculate BMI and daily calories
        const heightInMeters = height / 100;
        const bmi = weight / (heightInMeters * heightInMeters);
        
        // Basic BMR calculation (Mifflin-St Jeor Equation)
        let bmr;
        if (gender.toLowerCase() === 'male') {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        }

        // Activity level multiplier
        const activityMultipliers = {
            'Sedentary': 1.2,
            'Lightly Active': 1.375,
            'Moderately Active': 1.55,
            'Very Active': 1.725,
            'Extremely Active': 1.9
        };
        
        let dailyCalories = bmr * (activityMultipliers[activityLevel] || 1.2);
        if (isPregnant) {
            dailyCalories += 300;
        }

        res.json({
            success: true,
            message: 'User details updated successfully',
            userDetails: {
                id: updatedUser._id,
                userId: updatedUser._id,
                bmi: bmi,
                dailyCalories: dailyCalories
            }
        });
    } catch (error) {
        console.error('Error updating user details:', error);
        res.status(500).json({
            success: false,
            message: 'Error updating user details'
        });
    }
});

// Analyze Image and Save Result (No image storage)
app.post('/food-intake/analyze', authenticateToken, async (req, res) => {
    try {
        const { image } = req.body;
        const userId = req.user.userId;

        // Validate image input
        if (!image || typeof image !== 'string') {
            return res.status(400).json({ message: 'Invalid or missing image data' });
        }

        // Clean base64 string (remove data URI prefix if present)
        const base64Pattern = /^data:image\/[a-z]+;base64,/i;
        const cleanedImage = base64Pattern.test(image) ? image.replace(base64Pattern, '') : image;
        console.log(`Received image length: ${cleanedImage.length}`);

        // Validate base64 format
        const isValidBase64 = /^[A-Za-z0-9+/=]+$/.test(cleanedImage);
        if (!isValidBase64 || cleanedImage.length === 0) {
            return res.status(400).json({ message: 'Invalid base64 encoding' });
        }

        const stub = ClarifaiStub.grpc();
        const metadata = new grpc.Metadata();
        metadata.set('authorization', `Key ${CLARIFAI_PAT}`);

        const clarifaiPredict = (imageBase64) => {
            return new Promise((resolve, reject) => {
                stub.PostModelOutputs(
                    {
                        model_id: 'food-item-v1-recognition',
                        inputs: [{ data: { image: { base64: imageBase64 } } }]
                    },
                    metadata,
                    (err, response) => {
                        if (err) return reject(err);
                        if (response.status.code !== 10000) return reject(new Error(`Clarifai prediction failed: ${response.status.description}`));
                        resolve(response);
                    }
                );
            });
        };

        const clarifaiResponse = await clarifaiPredict(cleanedImage);
        const concepts = clarifaiResponse.outputs[0].data.concepts;
        const foodItem = concepts[0]?.name || 'unknown food';
        console.log(`Detected food: ${foodItem}`);

        const edamamResponse = await axios.get(
            `https://api.edamam.com/api/nutrition-data?app_id=${EDAMAM_APP_ID}&app_key=${EDAMAM_APP_KEY}&ingr=1%20serving%20${foodItem}`
        );
        const nutrients = edamamResponse.data.totalNutrients;
        const nutrientData = {
            calories: edamamResponse.data.calories || 0,
            protein: nutrients?.PROCNT?.quantity || 0,
            carbs: nutrients?.CHOCDF?.quantity || 0,
            fat: nutrients?.FAT?.quantity || 0
        };
        console.log(`Nutrients fetched: ${JSON.stringify(nutrientData)}`);

        // Save analysis result directly (no image)
        const scan = new Scan({
            userId,
            foodName: foodItem,
            nutrients: nutrientData
        });
        await scan.save();

        await User.findByIdAndUpdate(userId, { $push: { scans: scan._id } });
        console.log(`Scan saved for user ${userId}`);

        res.status(200).json({
            message: 'Image analyzed and saved successfully',
            foodName: foodItem,
            nutrients: nutrientData
        });
    } catch (error) {
        console.error('🔥 Analyze Image Error:', error.stack);
        res.status(500).json({ message: 'Server error', error: error.message });
    }
});

// Save Scan (Deprecated, since /analyze now saves directly)
app.post('/food-intake/scans', authenticateToken, async (req, res) => {
    try {
        const { foodName, calories, protein, carbs, fat } = req.body;
        const userId = req.user.userId;

        const scan = new Scan({
            userId,
            foodName,
            nutrients: { calories, protein, carbs, fat }
        });
        await scan.save();

        await User.findByIdAndUpdate(userId, { $push: { scans: scan._id } });
        console.log(`Scan saved for user ${userId}`);

        res.status(201).json({
            message: 'Scan saved successfully',
            scan: { _id: scan._id, nutrients: scan.nutrients }
        });
    } catch (error) {
        console.error('🔥 Scan Save Error:', error.stack);
        res.status(500).json({ message: 'Server error', error: error.message });
    }
});

// Get Scans
app.get('/food-intake/scans', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;
        const scans = await Scan.find({ userId });
        console.log(`Fetched ${scans.length} scans for user ${userId}`);

        res.json(scans);
    } catch (error) {
        console.error('🔥 Fetch Scans Error:', error.stack);
        res.status(500).json({ message: 'Server error', error: error.message });
    }
});

// Get Recommendations
app.get('/dashboard/recommendations', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.userId;
        console.log(`Fetching recommendations for userId: ${userId}`);

        const scans = await Scan.find({ userId });
        const user = await User.findById(userId);

        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }
        if (!user.age || !user.gender || !user.height || !user.weight || !user.activityLevel) {
            return res.status(400).json({ message: 'User details incomplete. Please update your profile.' });
        }

        const totalCalories = scans.reduce((sum, scan) => sum + (scan.nutrients?.calories || 0), 0);
        const totalProtein = scans.reduce((sum, scan) => sum + (scan.nutrients?.protein || 0), 0);
        const totalCarbs = scans.reduce((sum, scan) => sum + (scan.nutrients?.carbs || 0), 0);
        const totalFat = scans.reduce((sum, scan) => sum + (scan.nutrients?.fat || 0), 0);

        const bmr = user.gender === 'Male'
            ? 88.362 + (13.397 * user.weight) + (4.799 * user.height) - (5.677 * user.age)
            : 447.593 + (9.247 * user.weight) + (3.098 * user.height) - (4.330 * user.age);

        const activityMultipliers = {
            'Sedentary': 1.2,
            'Lightly Active': 1.375,
            'Moderately Active': 1.55,
            'Very Active': 1.725,
            'Extremely Active': 1.9
        };
        const multiplier = activityMultipliers[user.activityLevel] || 1.55;
        const dailyCalories = bmr * multiplier;
        const adjustedCalories = user.isPregnant ? dailyCalories + 300 : dailyCalories;

        const dailyProtein = user.gender === 'Male' ? 56 : 46;
        const dailyCarbs = adjustedCalories * 0.5 / 4;
        const dailyFat = adjustedCalories * 0.3 / 9;

        const deficiencies = {
            calories: Math.max(adjustedCalories - totalCalories, 0),
            protein: Math.max(dailyProtein - totalProtein, 0),
            carbs: Math.max(dailyCarbs - totalCarbs, 0),
            fat: Math.max(dailyFat - totalFat, 0)
        };

        const recommendations = [];
        if (deficiencies.calories > 500) recommendations.push(`High-calorie meal: Pasta with Olive Oil (${Math.round(deficiencies.calories/2)} kcal)`);
        else if (deficiencies.calories > 200) recommendations.push(`Snack: Almonds (${Math.round(deficiencies.calories)} kcal)`);
        if (deficiencies.protein > 10) recommendations.push(`Protein boost: Grilled Chicken (${Math.round(deficiencies.protein)}g protein)`);
        if (deficiencies.carbs > 20) recommendations.push(`Carb source: Sweet Potato (${Math.round(deficiencies.carbs)}g carbs)`);
        if (deficiencies.fat > 5) recommendations.push(`Healthy fat: Avocado (${Math.round(deficiencies.fat)}g fat)`);

        console.log(`Recommendations generated for user ${userId}:`, { deficiencies, recommendations });
        res.json({
            deficiencies,
            recommendations: recommendations.length > 0 ? recommendations : ["Your nutrient intake is balanced!"]
        });
    } catch (error) {
        console.error('🔥 Recommendations Error:', error.stack);
        res.status(500).json({ message: 'Server error', error: error.message });
    }
});

// Default Route
app.get('/', (req, res) => {
    res.send('🚀 NutriFill Backend is Running!');
});

// Start Server
app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on port ${PORT}`);
});
