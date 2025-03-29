package com.example.nutrifill.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nutrifill.data.dao.FoodDao
import com.example.nutrifill.data.entity.FoodEntity

@Database(
    entities = [FoodEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutrifill_database"
                )
                .fallbackToDestructiveMigration()  // Add migration strategy
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}