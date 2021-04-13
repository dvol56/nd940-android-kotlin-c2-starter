package com.udacity.asteroidradar.db

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AsteroidDao {
    @Query("SELECT * FROM databaseasteroid ORDER BY closeApproachDate ASC ")
    fun getAsteroids(): LiveData<List<DatabaseAsteroid>>

    @Query("SELECT * FROM databaseasteroid WHERE closeApproachDate = :date")
    fun getAsteroidsForDate(date: String): LiveData<List<DatabaseAsteroid>>

    @Query("SELECT * FROM databaseasteroid WHERE closeApproachDate < :date ORDER BY closeApproachDate ASC")
    fun getAsteroidsFromDate(date: String): LiveData<List<DatabaseAsteroid>>

    @Query("DELETE FROM databaseasteroid WHERE closeApproachDate < :date")
    fun deleteOldAsteroids(date: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroids: DatabaseAsteroid)
}

@Database(entities = [DatabaseAsteroid::class], version = 1)
abstract class AsteroidDatabase: RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
}

private lateinit var INSTANCE: AsteroidDatabase

fun getDatabase(context: Context): AsteroidDatabase {
    synchronized(AsteroidDatabase::class.java) {
        if(!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                AsteroidDatabase::class.java,
                "asteroids").build()
        }
    }
    return INSTANCE
}