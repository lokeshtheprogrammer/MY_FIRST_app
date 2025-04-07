const config = {
    mongoUri: process.env.MONGO_URI,
    port: process.env.PORT || 3000,
    jwtSecret: process.env.JWT_SECRET,
    appId: process.env.APP_ID,
    appKey: process.env.APP_KEY,
    clarifaiPat: process.env.CLARIFAI_PAT
};

module.exports = config;