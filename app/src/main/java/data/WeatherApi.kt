package com.benyamin.weathertrack.data

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("geo/1.0/direct")
    suspend fun findCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("limit") limit: Int = 1
    ): List<CityLocation>

    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}