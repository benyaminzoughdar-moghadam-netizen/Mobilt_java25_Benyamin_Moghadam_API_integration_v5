package com.benyamin.weathertrack

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.SimpleTimeZone
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ForecastFragment : Fragment(R.layout.fragment_forecast) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val weatherViewModel = ViewModelProvider(requireActivity())[
            WeatherViewModel::class.java
        ]
        val forecastViewModel = ViewModelProvider(this)[
            ForecastViewModel::class.java
        ]

        val city = weatherViewModel.uiState.value.city
        val title = view.findViewById<TextView>(R.id.textForecastTitle)
        val forecastText = view.findViewById<TextView>(R.id.textForecast)
        val retryButton = view.findViewById<Button>(R.id.buttonRetry)
        val detailsButton = view.findViewById<Button>(R.id.buttonDetails)

        if (city != null) {
            title.text = getString(
                R.string.forecast_heading,
                city.name,
                city.country
            )
            forecastViewModel.load(city)
        }

        detailsButton.isEnabled =
            weatherViewModel.uiState.value.weather != null

        detailsButton.setOnClickListener {
            findNavController().navigate(R.id.action_forecast_to_details)
        }

        retryButton.setOnClickListener {
            city?.let { forecastViewModel.load(it, refresh = true) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                forecastViewModel.uiState.collect { state ->
                    retryButton.visibility =
                        if (state.errorRes != null && city != null) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }

                    val forecast = state.forecast

                    forecastText.text = when {
                        city == null ->
                            getString(R.string.forecast_no_city)

                        state.isLoading ->
                            getString(R.string.forecast_loading)

                        state.errorRes != null ->
                            getString(state.errorRes)

                        forecast != null -> {
                            val formatter = SimpleDateFormat(
                                "EEE dd MMM, HH:mm",
                                Locale.getDefault()
                            ).apply {
                                timeZone = SimpleTimeZone(
                                    forecast.city.timezone * 1000,
                                    "ForecastCity"
                                )
                            }

                            forecast.list.joinToString("\n\n") { entry ->
                                getString(
                                    R.string.forecast_entry,
                                    formatter.format(Date(entry.dt * 1000L)),
                                    entry.main.temp,
                                    entry.weather.firstOrNull()
                                        ?.description.orEmpty()
                                )
                            }
                        }

                        else -> getString(R.string.forecast_loading)
                    }
                }
            }
        }
    }
}