package com.udacity.asteroidradar.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.AsteroidContainer
import com.udacity.asteroidradar.api.asDatabaseModel
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.db.AsteroidDatabase
import com.udacity.asteroidradar.db.asDomainModel
import com.udacity.asteroidradar.main.AsteroidFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AsteroidRepository(private val database: AsteroidDatabase) {

    val currentDate = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault()).format(
        Date()
    )
    val week =  getDateRangeFrom(
        Constants.NUMBER_OF_DAYS_IN_WEEK - Calendar.getInstance().get(
            Calendar.DAY_OF_WEEK))

    suspend fun deleteOldAsteroids() {
        withContext(Dispatchers.IO) {
            database.asteroidDao.deleteOldAsteroids(currentDate)
        }
    }

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            try {
                val list = parseAsteroidsJsonResult(
                    JSONObject(
                        AsteroidApi.retrofitService.getAsteroids(
                            SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault()).format(
                                Date()
                            ), Constants.API_KEY))
                )
                val asteriodList = AsteroidContainer(list)
                database.asteroidDao.insertAll(*asteriodList.asDatabaseModel())
            } catch (e: Exception) {
                Log.e("AsteriodRepository", "refreshAsteroids Error: ${e.message}")
            }

        }
    }

    fun getAsteroidSelection(filter: AsteroidFilter) : LiveData<List<Asteroid>> {
        return when (filter) {
            AsteroidFilter.WEEK -> Transformations.map(database.asteroidDao.getAsteroidsFromDate(week)) {
                it.asDomainModel() }
            AsteroidFilter.TODAY -> Transformations.map(database.asteroidDao.getAsteroidsForDate(currentDate)) {
                it.asDomainModel() }
            else -> Transformations.map(database.asteroidDao.getAsteroids()) {
                it.asDomainModel()
            }
        }
    }
}

fun getDateRangeFrom(difference : Int) : String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, difference)
    val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
    val getDate = dateFormat.format(calendar.time)
    Log.d("MainViewModel", "getDate: ${getDate}")
    return getDate
}