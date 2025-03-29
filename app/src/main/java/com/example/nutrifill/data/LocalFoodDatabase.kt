@Entity(tableName = "food_cache")
data class CachedFood(
    @PrimaryKey val name: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fats: Float,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Database(entities = [CachedFood::class], version = 1)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
}

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_cache WHERE name = :foodName")
    suspend fun getCachedFood(foodName: String): CachedFood?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheFood(food: CachedFood)
}