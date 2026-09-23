package com.benyamin.weathertrack.data

data class ForecastResponse(
    val list: List<WeatherResponse>,
    val city: ForecastCity
)

data class ForecastCity(
    val timezone: Int
)