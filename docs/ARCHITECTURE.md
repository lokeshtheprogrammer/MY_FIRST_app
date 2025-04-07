# NutriFill Architecture Documentation

## Overview
NutriFill is a mobile application designed to help users track their nutrition by scanning food items and providing detailed nutritional information. The app integrates multiple APIs and services to deliver accurate food identification and nutritional data.

## System Architecture

### Core Components

#### 1. User Interface Layer
- **Activities**:
  - `FstActivity`: Initial launch and authentication
  - `HomeActivity`: Main dashboard and navigation
  - `NutritionTrackingActivity`: Daily nutrition monitoring
  - `FoodScannerActivity`: Food scanning interface
  - `ManualEntryActivity`: Manual food data entry
  - `FoodDetailsActivity`: Detailed food information
  - `NutritionHistoryActivity`: Historical nutrition data

- **UI Components**:
  - Material Design components
  - Custom progress indicators
  - Interactive charts and graphs
  - Responsive layouts

#### 2. Business Logic Layer
- **Models**:
  - `DailyNutritionGoal`: User's nutrition targets
  - `NutritionProgress`: Daily progress tracking
  - `FoodItem`: Food item data structure
  - `ScanHistoryItem`: Scan history records

- **Services**:
  - Food recognition service
  - Nutrition calculation service
  - Data persistence service
  - Location service

#### 3. Data Layer
- **Local Storage**:
  - SharedPreferences for user settings
  - SQLite database for nutrition history
  - File storage for cached images

- **Remote APIs**:
  - Food recognition API
  - Nutrition database API
  - Location services API

## Feature Implementation

### 1. Nutrition Tracking
- **Implementation**: `NutritionTrackingActivity`
- **Key Features**:
  - Real-time progress monitoring
  - Multi-metric tracking (calories, protein, carbs, fats)
  - Deficiency detection and alerts
  - Food item history
- **Data Flow**:
  1. User inputs food data (scan/manual)
  2. Nutrition calculation
  3. Progress update
  4. UI refresh

### 2. Food Scanning
- **Implementation**: `FoodScannerActivity`
- **Features**:
  - Camera integration
  - Image processing
  - Food recognition
  - Nutrition data retrieval
- **Workflow**:
  1. Camera initialization
  2. Image capture
  3. API processing
  4. Results display

### 3. Manual Entry
- **Implementation**: `ManualEntryActivity`
- **Features**:
  - Custom input forms
  - Data validation
  - Portion size calculation
  - Unit conversion

### 4. Location Services
- **Implementation**: `FoodDetailsActivity`
- **Features**:
  - OpenStreetMap integration
  - Amma Unavagam location tracking
  - Distance calculation
  - Interactive markers

## Data Management

### 1. Local Storage
- **SharedPreferences**:
  - User settings
  - Authentication tokens
  - Daily goals

- **SQLite Database**:
  - Nutrition history
  - Food items
  - Scan records

### 2. Remote Data
- **API Integration**:
  - RESTful API calls
  - JSON data processing
  - Error handling
  - Rate limiting

## Security Implementation

### 1. User Authentication
- Token-based authentication
- Secure storage of credentials
- Session management
- Permission handling

### 2. Data Security
- Encrypted local storage
- Secure API communication
- Input validation
- Error logging

## Performance Optimization

### 1. Image Processing
- Efficient image compression
- Background processing
- Cache management
- Memory optimization

### 2. UI Performance
- Lazy loading
- View recycling
- Efficient layouts
- Background operations

## Error Handling

### 1. Network Errors
- Retry mechanisms
- Offline mode support
- User feedback
- Data synchronization

### 2. Input Validation
- Form validation
- Data type checking
- Boundary validation
- Error messages

## Future Enhancements

### 1. Planned Features
- Machine learning improvements
- Social sharing capabilities
- Advanced analytics
- Meal planning integration

### 2. Scalability
- Modular architecture
- Extensible components
- API versioning
- Performance monitoring

### 4. OpenStreetMap API
- **Purpose**: Location-based services and food establishment data
- **Base URL**: `https://api.openstreetmap.org/api/0.6`
- **Implementation**: `LocationService.kt`
- **Features**:
  - Nearby food establishments
  - Location-based food recommendations
  - Store information lookup

### 2. Backend API
- **Base URL**: `/api/v1`
- **Endpoints**:
  - Authentication:
    - POST `/auth/register`
    - POST `/auth/login`
    - POST `/auth/logout`
  - User Profile:
    - GET `/user/profile`
    - PUT `/user/profile`
  - Scan History:
    - GET `/scans`
    - POST `/scans`
    - GET `/scans/{id}`

## Data Models

### 1. User
```kotlin
data class User(
    val id: String,
    val name: String,
    val email: String,
    val preferences: UserPreferences
)
```

### 2. ScanHistoryItem
```kotlin
data class ScanHistoryItem(
    val id: String,
    val foodName: String,
    val nutrients: Nutrients,
    val timestamp: Long
)
```

### 3. Nutrients
```kotlin
data class Nutrients(
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float
)
```

## Application Flow

### 1. Startup Flow
1. App launch (`FstActivity`)
2. Authentication check
3. Redirect to appropriate screen (Login/Home)

### 2. Scanning Flow
1. User initiates scan (`HomeActivity` -> `ScanActivity`)
2. Camera preview initialization and permission handling
   - Camera2 API setup
   - Auto-focus and exposure configuration
   - Preview stream initialization
3. Image capture and processing
   - Frame analysis for optimal capture
   - Image compression and format conversion
   - Quality checks and enhancements
4. Multi-API Recognition Process
   - Vision API for initial food detection
   - Text recognition for package information
   - Barcode scanning via OpenFoodFacts
   - Location context via OpenStreetMap
5. Nutritional Data Aggregation
   - Primary lookup via USDA FoodData API
   - Cross-reference with OpenFoodFacts
   - Local database caching
   - Merge and validate data
6. Result Processing and Display
   - Nutritional information formatting
   - User preference application
   - History storage and sync
   - Interactive result visualization

### 3. History Management
1. Scan completion
2. Data storage in local database
3. Sync with backend server
4. History display and filtering

## Security Considerations

1. **Authentication**
   - Token-based authentication
   - Secure token storage
   - Session management

2. **Data Protection**
   - HTTPS for all API calls
   - Local data encryption
   - Secure key storage

## Performance Optimization

1. **Image Processing**
   - Efficient image compression
   - Background processing
   - Memory management

2. **API Calls**
   - Request caching
   - Rate limiting
   - Error handling

## Error Handling

1. **Network Errors**
   - Retry mechanisms
   - Offline support
   - User feedback

2. **Vision API Errors**
   - Fallback mechanisms
   - Alternative detection methods
   - Error reporting

## Future Improvements

1. **Feature Enhancements**
   - Barcode scanning
   - Nutritional goals tracking
   - Social sharing

2. **Technical Improvements**
   - ML model optimization
   - Offline recognition
   - Enhanced caching

## Development Guidelines

1. **Code Style**
   - Follow Kotlin coding conventions
   - Maintain consistent naming
   - Document complex logic

2. **Testing**
   - Unit tests for core functionality
   - Integration tests for API calls
   - UI testing for critical flows

## Deployment

1. **Release Process**
   - Version management
   - Changelog maintenance
   - Play Store deployment

2. **Monitoring**
   - Error tracking
   - Usage analytics
   - Performance metrics