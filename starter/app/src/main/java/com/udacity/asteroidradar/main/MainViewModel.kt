package com.udacity.asteroidradar.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.db.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch
import java.util.*

enum class AsteroidFilter {WEEK, TODAY, SAVED}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val asteroidRepository = AsteroidRepository(database)

    fun onAsteroidClicked(asteroid: Asteroid) {
        _navigateToDetail.value = asteroid
    }

    fun onAsteroidNavigated() {
        _navigateToDetail.value = null
    }

    private val _navigateToDetail = MutableLiveData<Asteroid>()
    val navigateToDetail
        get() = _navigateToDetail

    private val _dailyImage = MutableLiveData<PictureOfDay>()
    val dailyImage: LiveData<PictureOfDay>
        get() = _dailyImage

    private val _asteroidData = MutableLiveData<List<Asteroid>>()
    val asteroidData: LiveData<List<Asteroid>>
        get() = _asteroidData


    private val asteroidDataObserver = Observer<List<Asteroid>> {
        _asteroidData.value = it
    }

    private var asteroidDataLiveData: LiveData<List<Asteroid>>

    init {
        asteroidDataLiveData = asteroidRepository.getAsteroidSelection(AsteroidFilter.SAVED)
        asteroidDataLiveData.observeForever(asteroidDataObserver)
        getApod()
        viewModelScope.launch {
            asteroidRepository.refreshAsteroids()
            deleteOldAsteroids()
        }
    }

    private suspend fun deleteOldAsteroids() {
        viewModelScope.launch {
            asteroidRepository.deleteOldAsteroids()
        }
    }

    private fun getApod() {
        viewModelScope.launch {
            try {
                val downloadedImage = AsteroidApi.retrofitService.getApod(Constants.API_KEY)
                if (downloadedImage.mediaType.toLowerCase(Locale.getDefault()) == Constants.IMAGE_KEY) {
                    _dailyImage.value = downloadedImage
                } else {
                    _dailyImage.value = null
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Apod Failure: ${e.message}")
                _dailyImage.value = null
            }
        }
    }

    fun filterAsteriods(filter: AsteroidFilter) {
        asteroidDataLiveData = asteroidRepository.getAsteroidSelection(filter)
        asteroidDataLiveData.observeForever(asteroidDataObserver)
    }


}