package com.example.nutrifill.data

import com.example.nutrifill.model.FoodItem

object NutrientFoodDatabase {
    val nutrientFoodMap = mapOf(
        "protein" to listOf(
            FoodItem(id = "chicken_breast", name = "Chicken Breast", protein = 31f, calories = 165f),
            FoodItem(id = "salmon", name = "Salmon", protein = 25f, calories = 208f),
            FoodItem(id = "eggs", name = "Eggs", protein = 13f, calories = 155f),
            FoodItem(id = "greek_yogurt", name = "Greek Yogurt", protein = 10f, calories = 59f)
        ),
        "carbohydrates" to listOf(
            FoodItem(id = "brown_rice", name = "Brown Rice", carbohydrates = 45f, calories = 216f),
            FoodItem(id = "sweet_potato", name = "Sweet Potato", carbohydrates = 27f, calories = 103f),
            FoodItem(id = "quinoa", name = "Quinoa", carbohydrates = 39f, calories = 120f),
            FoodItem(id = "oats", name = "Oats", carbohydrates = 27f, calories = 307f)
        ),
        "fat" to listOf(
            FoodItem(id = "avocado", name = "Avocado", fats = 15f, calories = 160f),
            FoodItem(id = "almonds", name = "Almonds", fats = 14f, calories = 164f),
            FoodItem(id = "olive_oil", name = "Olive Oil", fats = 14f, calories = 119f),
            FoodItem(id = "chia_seeds", name = "Chia Seeds", fats = 9f, calories = 138f)
        ),
        "fiber" to listOf(
            FoodItem(id = "lentils", name = "Lentils", carbohydrates = 20f, fiber = 8f, calories = 116f),
            FoodItem(id = "black_beans", name = "Black Beans", carbohydrates = 23f, fiber = 7f, calories = 132f),
            FoodItem(id = "broccoli", name = "Broccoli", carbohydrates = 6f, fiber = 2.4f, calories = 55f),
            FoodItem(id = "pear", name = "Pear", carbohydrates = 27f, fiber = 5.5f, calories = 101f)
        ),
        "vitamin_c" to listOf(
            FoodItem(id = "orange", name = "Orange", carbohydrates = 12f, calories = 47f),
            FoodItem(id = "bell_pepper", name = "Bell Pepper", carbohydrates = 6f, calories = 31f),
            FoodItem(id = "kiwi", name = "Kiwi", carbohydrates = 14f, calories = 61f),
            FoodItem(id = "strawberries", name = "Strawberries", carbohydrates = 8f, calories = 32f)
        ),
        "calcium" to listOf(
            FoodItem(id = "milk", name = "Milk", protein = 8f, calories = 103f),
            FoodItem(id = "cheese", name = "Cheese", protein = 7f, calories = 113f),
            FoodItem(id = "yogurt", name = "Yogurt", protein = 5f, calories = 59f),
            FoodItem(id = "spinach", name = "Spinach", carbohydrates = 3.6f, calories = 23f)
        ),
        "iron" to listOf(
            FoodItem(id = "beef", name = "Beef", protein = 26f, calories = 213f),
            FoodItem(id = "spinach", name = "Spinach", carbohydrates = 3.6f, calories = 23f),
            FoodItem(id = "lentils", name = "Lentils", carbohydrates = 20f, calories = 116f),
            FoodItem(id = "tofu", name = "Tofu", protein = 8f, calories = 70f)
        )
    )
}