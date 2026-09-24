package com.benyamin.weathertrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyamin.weathertrack.data.AirQualityClient
import com.benyamin.weathertrack.data.AirQualityCurrent
import com.benyamin.weathertrack.data.CityLocation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AirQualityUiState(
    val city: CityLocation? = null,
    val data: AirQualityCurrent? = null,
    val isLoading: Boolean = false,
    val hasError: Boolean = false
)

class AirQualityViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AirQualityUiState())
    val uiState = _uiState.asStateFlow()

    private var requestJob: Job? = null

    fun load(city: CityLocation, refresh: Boolean = false) {
        val current = _uiState.value

        if (!refresh && current.city == city &&
            (current.isLoading || current.data != null)
        ) {
            return
        }

        requestJob?.cancel()

        _uiState.value = AirQualityUiState(
            city = city,
            isLoading = true
        )

        requestJob = viewModelScope.launch {
            try {
                val result = AirQualityClient.api.getAirQuality(
                    city.lat,
                    city.lon
                ).current

                val hasData = result != null &&
                        (result.europeanAqi != null ||
                                result.pm25 != null ||
                                result.pm10 != null)

                _uiState.value = if (hasData) {
                    AirQualityUiState(city = city, data = result)
                } else {
                    AirQualityUiState(city = city, hasError = true)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = AirQualityUiState(
                    city = city,
                    hasError = true
                )
            }
        }
    }
}