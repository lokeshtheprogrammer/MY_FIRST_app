# NutriFill - Nutrition Tracking App

## Overview
NutriFill is a comprehensive nutrition tracking application that helps users monitor their daily food intake and nutritional information. The app uses the Edamam API for accurate nutrition data and provides an intuitive interface for tracking meals.

## Features
- Food recognition and nutrition information lookup
- Portion size customization
- Detailed nutritional breakdown (calories, protein, fats, carbs, fiber)
- Secure API integration

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK 21 or later

### Environment Setup
1. Clone the repository
2. Open the project in Android Studio
3. Create a `local.properties` file in the project root and add your API keys:
```properties
EDAMAM_APP_ID=your_app_id
EDAMAM_APP_KEY=your_app_key
```

### IP Configuration
Before running the application, you need to configure the server IP address in the following locations:

1. Android App Configuration (`app/src/main/java/com/example/nutrifill/network/AppConfig.kt`):
   - Update `SERVER_BASE_URL` with your server's IP address:
   ```kotlin
   const val SERVER_BASE_URL = "http://192.168.31.248:3000/" // Change to your server IP
   ```

2. Server Configuration (`server/src/index.js`):
   - The server listens on all network interfaces ('0.0.0.0')
   - Default port is 3000 (configurable in server/config/config.js)

Note: Make sure to update the IP address to match your development or production environment.

### Building the App
1. Sync project with Gradle files
2. Build the project using Android Studio's build option or run:
```bash
./gradlew assembleDebug
```

### Running Tests
```bash
./gradlew test
```

## Security Considerations
- API keys are stored securely in BuildConfig
- Network communications are encrypted using HTTPS
- User data is stored securely using Android's security best practices

## Deployment Checklist
- [ ] Update version code and name in build.gradle
- [ ] Test all features thoroughly
- [ ] Verify API key configuration
- [ ] Check ProGuard rules for release build
- [ ] Validate all permissions
- [ ] Test on multiple Android versions

## License
This project is licensed under the MIT License - see the LICENSE file for details.