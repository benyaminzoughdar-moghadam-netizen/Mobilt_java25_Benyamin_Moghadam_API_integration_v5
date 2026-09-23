package com.benyamin.weathertrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyamin.weathertrack.data.CityLocation
import com.benyamin.weathertrack.data.ForecastResponse
import com.benyamin.weathertrack.data.WeatherClient
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ForecastUiState(
    val city: CityLocation? = null,
    val forecast: ForecastResponse? = null,
    val isLoading: Boolean = false,
    val errorRes: Int? = null
)

class ForecastViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ForecastUiState())
    val uiState = _uiState.asStateFlow()

    private var requestJob: Job? = null

    fun load(city: CityLocation, refresh: Boolean = false) {
        val current = _uiState.value

        if (!refresh && current.city == city &&
            (current.isLoading || current.forecast != null)
        ) {
            return
        }

        requestJob?.cancel()
        _uiState.value = ForecastUiState(
            city = city,
            isLoading = true
        )

        requestJob = viewModelScope.launch {
            try {
                val forecast = WeatherClient.api.getForecast(
                    city.lat,
                    city.lon,
                    BuildConfig.OPENWEATHER_API_KEY.trim()
                )

                _uiState.value = if (forecast.list.isEmpty()) {
                    ForecastUiState(
                        city = city,
                        errorRes = R.string.weather_generic_error
                    )
                } else {
                    ForecastUiState(city = city, forecast = forecast)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val message = when {
                    error is HttpException && error.code() == 401 ->
                        R.string.weather_service_error
                    error is HttpException && error.code() == 429 ->
                        R.string.weather_limit_error
                    error is IOException ->
                        R.string.weather_network_error
                    else ->
                        R.string.weather_generic_error
                }

                _uiState.value = ForecastUiState(
                    city = city,
                    errorRes = message
                )
            }
        }
    }
}