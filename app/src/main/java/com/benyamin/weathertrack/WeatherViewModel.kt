package com.benyamin.weathertrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyamin.weathertrack.data.CityLocation
import com.benyamin.weathertrack.data.WeatherClient
import com.benyamin.weathertrack.data.WeatherResponse
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class WeatherUiState(
    val isLoading: Boolean = false,
    val city: CityLocation? = null,
    val weather: WeatherResponse? = null,
    val errorRes: Int? = null
)

class WeatherViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun search(cityName: String) {
        searchJob?.cancel()

        val query = cityName.trim()
        val apiKey = BuildConfig.OPENWEATHER_API_KEY.trim()

        if (query.isBlank()) {
            _uiState.value = WeatherUiState(
                errorRes = R.string.weather_enter_city
            )
            return
        }

        if (apiKey.isBlank()) {
            _uiState.value = WeatherUiState(
                errorRes = R.string.weather_service_error
            )
            return
        }

        _uiState.value = WeatherUiState(isLoading = true)

        searchJob = viewModelScope.launch {
            try {
                // Find the city's coordinates first.
                val city = WeatherClient.api
                    .findCity(query, apiKey)
                    .firstOrNull()

                if (city == null) {
                    _uiState.value = WeatherUiState(
                        errorRes = R.string.weather_city_not_found
                    )
                    return@launch
                }

                // Fetch weather using those coordinates.
                val weather = WeatherClient.api.getCurrentWeather(
                    city.lat,
                    city.lon,
                    apiKey
                )

                _uiState.value = WeatherUiState(
                    city = city,
                    weather = weather
                )
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

                _uiState.value = WeatherUiState(errorRes = message)
            }
        }
    }
}