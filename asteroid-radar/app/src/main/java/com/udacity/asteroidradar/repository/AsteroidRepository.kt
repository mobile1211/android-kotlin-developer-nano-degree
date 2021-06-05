package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import com.udacity.asteroidradar.model.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.NeoWSApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.model.AstroPictureOfDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AsteroidRepository(private val database: AsteroidDatabase) {

    val asteroids: LiveData<List<Asteroid>> = database.asteroidDatabaseDao.getAsteroids()
    val astroPictureOfDay: LiveData<AstroPictureOfDay> = database.asteroidDatabaseDao.getPictureOfTheDay()

    suspend fun getAsteroids() {
        withContext(Dispatchers.IO) {
            val dates = getStartAndEndDates()
            val responseString = NeoWSApi.retrofitService.getAsteroidsAsync(
                dates.first,
                dates.second,
                "wxthVHrXBVZFK6x7nweX6eKsfPfV98V6BIFwYlTe"
            )
            val asteroids = parseAsteroidsJsonResult(JSONObject(responseString))
            database.asteroidDatabaseDao.insert(asteroids)
        }
    }

    suspend fun getPictureOfDay() {
        withContext(Dispatchers.IO) {
            val pictureOfTheDay =
                NeoWSApi.retrofitService.getPictureOfDayAsync("wxthVHrXBVZFK6x7nweX6eKsfPfV98V6BIFwYlTe")
                    .await()
            database.asteroidDatabaseDao.insert(pictureOfTheDay)
        }
    }

    private fun getStartAndEndDates(): Pair<String, String> {
        val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
        val calendar = Calendar.getInstance()
        val currentTime = calendar.time
        val startDate = dateFormat.format(currentTime)
        calendar.add(Calendar.DAY_OF_YEAR, Constants.DEFAULT_END_DATE_DAYS)
        val endDate = dateFormat.format(calendar.time)
        return Pair(startDate, endDate)
    }
}