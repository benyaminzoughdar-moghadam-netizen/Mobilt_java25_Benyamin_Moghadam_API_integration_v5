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
import kotlinx.coroutines.launch

class AirQualityFragment : Fragment(R.layout.fragment_air_quality) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val weatherViewModel = ViewModelProvider(requireActivity())[
            WeatherViewModel::class.java
        ]
        val airViewModel = ViewModelProvider(this)[
            AirQualityViewModel::class.java
        ]

        val city = weatherViewModel.uiState.value.city
        val result = view.findViewById<TextView>(R.id.textAirQuality)
        val retry = view.findViewById<Button>(R.id.buttonRetryAir)
        val missing = getString(R.string.details_missing)

        if (city == null) {
            result.setText(R.string.air_no_city)
            return
        }

        airViewModel.load(city)

        retry.setOnClickListener {
            airViewModel.load(city, refresh = true)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                airViewModel.uiState.collect { state ->
                    retry.visibility = if (state.hasError) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

                    val data = state.data

                    result.text = when {
                        state.isLoading -> getString(R.string.air_loading)
                        state.hasError -> getString(R.string.air_error)
                        data != null -> {
                            val aqi = data.europeanAqi?.let {
                                getString(R.string.air_number, it)
                            } ?: missing

                            val pm25 = data.pm25?.let {
                                getString(R.string.air_particles, it)
                            } ?: missing

                            val pm10 = data.pm10?.let {
                                getString(R.string.air_particles, it)
                            } ?: missing

                            getString(
                                R.string.air_result,
                                aqi,
                                pm25,
                                pm10,
                                data.time?.replace("T", " ") ?: missing
                            )
                        }
                        else -> getString(R.string.air_loading)
                    }
                }
            }
        }
    }
}