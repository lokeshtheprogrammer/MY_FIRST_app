# NutriFill Development Documentation

## Project Overview
NutriFill is a comprehensive mobile application that combines computer vision, nutritional analysis, and location services to provide users with detailed food information and tracking capabilities. The app leverages Google Vision API for food recognition, Edamam API for nutritional analysis, and OpenStreetMap for location-based services, particularly focusing on Amma Unavagam establishments. This document serves as a technical guide for developers working on the project.

## Tech Stack

### Frontend (Mobile App)
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI Framework**: Android Jetpack Compose
- **Image Processing**: CameraX API, Google Vision API
- **Nutrition API**: Edamam API Integration
- **Location Services**: OpenStreetMap Integration
- **Dependency Injection**: Hilt
- **Async Operations**: Kotlin Coroutines + Flow
- **Local Storage**: Room Database

### Backend
- **Runtime**: Node.js
- **Framework**: Express.js
- **Database**: MongoDB
- **Authentication**: JWT
- **API Documentation**: Swagger/OpenAPI
- **External APIs**: 
  - Google Vision API (Food Recognition)
  - Edamam API (Nutrition Data)
  - OpenStreetMap API (Location Services)

## Development Environment Setup

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or later
- Node.js 14.x or later
- MongoDB 4.4 or later

### Local Development
1. Clone the repository
2. Set up environment variables
3. Install dependencies:
   ```bash
   # Android App
   ./gradlew build
   
   # Backend Server
   cd server
   npm install
   ```
4. Start development servers:
   ```bash
   # Backend
   npm run dev
   
   # Android App
   Run through Android Studio
   ```

## Project Structure

### Android App Structure
```
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/nutrifill/
│   │   │       ├── data/           # Data layer
│   │   │       ├── di/            # Dependency injection
│   │   │       ├── domain/        # Business logic
│   │   │       ├── presentation/  # UI layer
│   │   │       └── utils/         # Utilities
│   │   ├── res/                   # Resources
│   │   └── AndroidManifest.xml
│   └── test/                      # Unit tests
└── build.gradle.kts
```

### Backend Structure
```
server/
├── src/
│   ├── models/      # Database models
│   ├── routes/      # API routes
│   ├── services/    # Business logic
│   ├── utils/       # Utilities
│   └── index.js     # Entry point
├── config/          # Configuration
└── package.json
```

## Key Components

### 1. Scanner Module
- **Purpose**: Handles food recognition through camera using Google Vision API
- **Key Classes**:
  - `ScannerActivity`: Main UI for scanning and result display
  - `CameraManager`: Camera operations and image capture
  - `VisionApiService`: Google Vision API integration for food recognition
  - `ImageProcessor`: Image preprocessing and optimization
  - `ScanResultProcessor`: Handles recognition results and nutrition lookup

### 2. Nutrition Module
- **Purpose**: Processes nutritional information using Edamam API
- **Key Components**:
  - `NutritionRepository`: Data management and caching
  - `NutritionViewModel`: Business logic and UI state management
  - `NutritionService`: Edamam API integration
  - `NutritionCalculator`: Portion size and nutrient calculations
  - `NutritionTracker`: Daily nutrition goal tracking

### 3. Location Services
- **Purpose**: Handles location-based features for Amma Unavagam locations
- **Components**:
  - `LocationManager`: Location tracking and permissions
  - `MapViewModel`: OpenStreetMap integration and state management
  - `LocationRepository`: Amma Unavagam location data handling
  - `MapMarkerManager`: Custom marker handling for food establishments
  - `RouteCalculator`: Distance and route calculations

## API Integration

### 1. Google Vision API Integration
```kotlin
class VisionApiService @Inject constructor(
    private val apiClient: VisionApiClient
) {
    suspend fun recognizeFood(imageData: ByteArray): FoodRecognitionResult {
        return apiClient.detectLabels(imageData)
    }
    
    suspend fun analyzeFoodImage(image: ByteArray): List<FoodLabel> {
        return apiClient.analyzeImage(image)
    }
    
    suspend fun detectMultipleItems(image: ByteArray): List<FoodItem> {
        return apiClient.detectMultipleObjects(image)
    }
}
```

### 2. Edamam Nutrition API
```kotlin
class EdamamApiService @Inject constructor(
    private val apiClient: EdamamApiClient
) {
    suspend fun getNutritionInfo(foodName: String): NutritionDetails {
        return apiClient.getNutritionData(foodName)
    }
    
    suspend fun calculateNutrition(food: String, quantity: Double, unit: String): NutritionResult {
        return apiClient.calculateNutrients(food, quantity, unit)
    }
    
    suspend fun searchFood(query: String): List<FoodItem> {
        return apiClient.searchFoodDatabase(query)
    }
}
```

### 3. OpenStreetMap Integration
```kotlin
class LocationService @Inject constructor(
    private val mapClient: OpenStreetMapClient
) {
    suspend fun findNearbyAmmaUnavagam(latitude: Double, longitude: Double): List<AmmaUnavagam> {
        return mapClient.findNearbyEstablishments(latitude, longitude)
    }
    
    suspend fun getRouteToLocation(userLat: Double, userLng: Double, destLat: Double, destLng: Double): Route {
        return mapClient.calculateRoute(userLat, userLng, destLat, destLng)
    }
    
    suspend fun searchAmmaUnavagam(query: String): List<AmmaUnavagam> {
        return mapClient.searchEstablishments(query)
    }
}
```

## Development Workflow

### 1. Feature Development
1. Create feature branch from develop
2. Implement feature following MVVM
3. Write unit tests
4. Create PR for review
5. Merge after approval

### 2. Testing Strategy
- **Unit Tests**: JUnit + Mockito
- **UI Tests**: Espresso
- **Integration Tests**: Retrofit + MockWebServer
- **E2E Tests**: Automated UI testing

### 3. Code Review Process
1. Code style compliance
2. Architecture adherence
3. Test coverage
4. Performance impact
5. Security considerations

## Security Guidelines

### 1. API Security
- Use HTTPS for all API calls
- Implement rate limiting
- Validate all inputs
- Use parameterized queries

### 2. Data Security
- Encrypt sensitive data
- Use secure key storage
- Implement proper session management
- Regular security audits

## Performance Guidelines

### 1. Image Processing
- Optimize image size before upload
- Use background processing
- Implement caching
- Handle memory efficiently

### 2. API Calls
- Implement request caching
- Use pagination
- Handle rate limits
- Implement retry mechanisms

## Deployment

### 1. Android App
1. Version bump
2. Generate signed APK
3. Run final tests
4. Deploy to Play Store

### 2. Backend
1. Run tests
2. Build Docker image
3. Deploy to production
4. Monitor logs

## Monitoring

### 1. Application Monitoring
- Firebase Analytics
- Crash reporting
- Performance monitoring
- User analytics

### 2. Backend Monitoring
- Server metrics
- API performance
- Error tracking
- Resource utilization

## Troubleshooting Guide

### Common Issues
1. Camera permission handling
2. API rate limits
3. Image processing errors
4. Location services issues

### Debug Tools
- Android Studio Debugger
- Charles Proxy
- Firebase Analytics
- Logging framework

## Version Control Guidelines

### 1. Branch Strategy
- main: Production code
- develop: Development branch
- feature/*: Feature branches
- bugfix/*: Bug fix branches

### 2. Commit Guidelines
- Use descriptive messages
- Reference issue numbers
- Keep commits focused
- Follow conventional commits

## Documentation Guidelines

### 1. Code Documentation
- Use KDoc for Kotlin
- Document complex logic
- Update README.md
- Maintain CHANGELOG.md

### 2. API Documentation
- Use OpenAPI/Swagger
- Document all endpoints
- Include request/response examples
- Document error codes

## Support and Resources

### 1. Internal Resources
- Wiki documentation
- Architecture diagrams
- API documentation
- Testing guidelines

### 2. External Resources
- Android documentation
- API documentation
- Library documentation
- Stack Overflow