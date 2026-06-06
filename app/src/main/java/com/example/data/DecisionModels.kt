package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. Room Persistence Layer
// ==========================================

@Entity(tableName = "decisions")
data class DecisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val context: String,
    val framework: String, // "PROS_CONS", "COMPARISON", "SWOT"
    val responseJson: String, // raw moshi parsed json
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface DecisionDao {
    @Query("SELECT * FROM decisions ORDER BY timestamp DESC")
    fun getAllDecisions(): Flow<List<DecisionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecision(decision: DecisionEntity): Long

    @Query("DELETE FROM decisions WHERE id = :id")
    suspend fun deleteDecisionById(id: Int)
}

@Database(entities = [DecisionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun decisionDao(): DecisionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tiebreaker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class DecisionRepository(private val decisionDao: DecisionDao) {
    val allDecisions: Flow<List<DecisionEntity>> = decisionDao.getAllDecisions()

    suspend fun insert(decision: DecisionEntity): Long {
        return decisionDao.insertDecision(decision)
    }

    suspend fun deleteById(id: Int) {
        decisionDao.deleteDecisionById(id)
    }
}

// ==========================================
// 2. Moshi JSON API Models
// ==========================================

@JsonClass(generateAdapter = true)
data class ProConItem(
    val point: String,
    val category: String,
    val weight: Int // 1 to 5 representing importance/impact
)

@JsonClass(generateAdapter = true)
data class ProsConsModel(
    val verdict: String,
    val confidenceScore: Int, // percentage 0-100
    val probabilityOfSuccess: Int, // percentage 0-100
    val pros: List<ProConItem>,
    val cons: List<ProConItem>
)

@JsonClass(generateAdapter = true)
data class ComparisonRow(
    val criterion: String,
    val optionAValue: String,
    val optionBValue: String,
    val winner: String // "Option A", "Option B", or "Tie"
)

@JsonClass(generateAdapter = true)
data class ComparisonModel(
    val verdict: String,
    val confidenceScore: Int,
    val options: List<String>, // e.g., ["Buy Hybrid", "Buy EV"]
    val criteria: List<ComparisonRow>
)

@JsonClass(generateAdapter = true)
data class SwotModel(
    val verdict: String,
    val confidenceScore: Int,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val opportunities: List<String>,
    val threats: List<String>
)
