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
import java.util.TimeZone
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DetailsFragment : Fragment(R.layout.fragment_details) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity())[
            WeatherViewModel::class.java
        ]
        val details = view.findViewById<TextView>(R.id.textWeatherDetails)
        val missing = getString(R.string.details_missing)

        val dateFormat = SimpleDateFormat(
            "dd MMM yyyy, HH:mm",
            Locale.getDefault()
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        view.findViewById<Button>(R.id.buttonBackHome).setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val city = state.city
                    val weather = state.weather

                    if (city == null || weather == null) {
                        details.text = getString(R.string.details_no_weather)
                        return@collect
                    }

                    val temperature = getString(
                        R.string.details_temperature,
                        weather.main.temp
                    )
                    val feelsLike = weather.main.feelsLike?.let {
                        getString(R.string.details_temperature, it)
                    } ?: missing

                    val humidity = weather.main.humidity?.let {
                        getString(R.string.details_humidity, it)
                    } ?: missing

                    val wind = weather.wind?.speed?.let {
                        getString(R.string.details_wind, it)
                    } ?: missing

                    val pressure = weather.main.pressure?.let {
                        getString(R.string.details_pressure, it)
                    } ?: missing

                    details.text = getString(
                        R.string.details_result,
                        city.name,
                        city.country,
                        weather.weather.firstOrNull()?.description ?: missing,
                        temperature,
                        feelsLike,
                        humidity,
                        wind,
                        pressure,
                        dateFormat.format(Date(weather.dt * 1000L))
                    )
                }
            }
        }
    }
}