package com.benyamin.weathertrack.data

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class AirQualityResponse(
    val current: AirQualityCurrent?
)

data class AirQualityCurrent(
    val time: String?,
    @SerializedName("european_aqi")
    val europeanAqi: Double?,
    @SerializedName("pm2_5")
    val pm25: Double?,
    val pm10: Double?
)

interface AirQualityApi {
    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current")
        current: String = "european_aqi,pm2_5,pm10",
        @Query("timezone") timezone: String = "GMT"
    ): AirQualityResponse
}

object AirQualityClient {
    val api: AirQualityApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://air-quality-api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AirQualityApi::class.java)
    }
}