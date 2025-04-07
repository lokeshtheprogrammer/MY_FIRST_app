# NutriFill API Documentation

## API Overview
This document details the RESTful API endpoints provided by NutriFill's backend services for nutrition tracking and meal recommendations.

## Base URL
```
https://api.nutrifill.com/v1
```

## Authentication

### Bearer Token
All API requests require authentication using a JWT Bearer token in the Authorization header:
```
Authorization: Bearer <token>
```

## Endpoints

### User Profile

#### Get User Profile
```http
GET /users/profile
```

Response:
```json
{
  "userId": "string",
  "name": "string",
  "dietaryPreferences": ["string"],
  "nutritionGoals": {
    "calories": "number",
    "protein": "number",
    "carbs": "number",
    "fats": "number"
  }
}
```

### Nutrition Tracking

#### Log Food Item
```http
POST /nutrition/log
```

Request Body:
```json
{
  "foodItem": "string",
  "servingSize": "number",
  "mealType": "string",
  "timestamp": "string"
}
```

#### Get Daily Progress
```http
GET /nutrition/progress?date={date}
```

Response:
```json
{
  "date": "string",
  "totalCalories": "number",
  "macronutrients": {
    "protein": "number",
    "carbs": "number",
    "fats": "number"
  },
  "goalCompletion": {
    "calories": "number",
    "protein": "number",
    "carbs": "number",
    "fats": "number"
  }
}
```

### Recommendations

#### Get Food Recommendations
```http
GET /recommendations/food
```

Response:
```json
{
  "recommendations": [{
    "foodItem": "string",
    "nutrients": {
      "calories": "number",
      "protein": "number",
      "carbs": "number",
      "fats": "number"
    },
    "servingSize": "number",
    "reason": "string"
  }]
}
```

#### Generate Meal Plan
```http
POST /recommendations/meal-plan
```

Request Body:
```json
{
  "date": "string",
  "preferences": {
    "excludeIngredients": ["string"],
    "mealCount": "number",
    "calorieTarget": "number"
  }
}
```

Response:
```json
{
  "meals": [{
    "mealType": "string",
    "foods": [{
      "name": "string",
      "servingSize": "number",
      "nutrients": {
        "calories": "number",
        "protein": "number",
        "carbs": "number",
        "fats": "number"
      }
    }]
  }]
}
```

## Error Responses

### Standard Error Format
```json
{
  "error": {
    "code": "string",
    "message": "string",
    "details": "object"
  }
}
```

### Common Error Codes
- `400`: Bad Request
- `401`: Unauthorized
- `403`: Forbidden
- `404`: Not Found
- `429`: Too Many Requests
- `500`: Internal Server Error

## Rate Limiting
- 100 requests per minute per user
- Rate limit headers included in responses:
  ```
  X-RateLimit-Limit: 100
  X-RateLimit-Remaining: 99
  X-RateLimit-Reset: 1640995200
  ```

## Versioning
- API version included in URL path
- Breaking changes trigger version increment
- Deprecated versions announced 6 months in advance

## Data Types

### DateTime Format
- All timestamps in ISO 8601 format
- Example: `2023-12-31T23:59:59Z`

### Numeric Values
- Calories: integer
- Nutrients: float with 2 decimal places
- Serving sizes: float with 2 decimal places

## Best Practices

1. **Pagination**
   - Use `limit` and `offset` query parameters
   - Default limit: 20 items
   - Maximum limit: 100 items

2. **Filtering**
   - Use query parameters for filtering
   - Multiple filters combined with AND logic

3. **Caching**
   - Responses include ETag headers
   - Cache-Control headers specify max-age
   - Implement conditional requests using If-None-Match