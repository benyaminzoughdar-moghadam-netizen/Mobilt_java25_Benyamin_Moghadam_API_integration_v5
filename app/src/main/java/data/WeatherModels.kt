package com.benyamin.weathertrack.data

import com.google.gson.annotations.SerializedName

data class CityLocation(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String
)

data class WeatherResponse(
    val main: WeatherMain,
    val weather: List<WeatherCondition>,
    val wind: WeatherWind?,
    val dt: Long
)

data class WeatherMain(
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double?,
    val humidity: Int?,
    val pressure: Int?
)

data class WeatherCondition(
    val id: Int,
    val main: String,
    val description: String
)

data class WeatherWind(
    val speed: Double?
)