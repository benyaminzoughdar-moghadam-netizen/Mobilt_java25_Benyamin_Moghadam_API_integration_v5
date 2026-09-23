package com.benyamin.weathertrack

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity())[
            WeatherViewModel::class.java
        ]

        val editCity = view.findViewById<EditText>(R.id.editCity)
        val buttonSearch = view.findViewById<Button>(R.id.buttonSearch)
        val textWeather = view.findViewById<TextView>(R.id.textWeather)

        fun searchWeather() {
            viewModel.search(editCity.text.toString())
            WindowInsetsControllerCompat(requireActivity().window, view)
                .hide(WindowInsetsCompat.Type.ime())
            editCity.clearFocus()
        }

        buttonSearch.setOnClickListener {
            searchWeather()
        }

        editCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchWeather()
                true
            } else {
                false
            }
        }

        view.findViewById<Button>(R.id.buttonForecast).setOnClickListener {
            findNavController().navigate(R.id.action_home_to_forecast)
        }

        view.findViewById<Button>(R.id.buttonHistory).setOnClickListener {
            findNavController().navigate(R.id.action_home_to_history)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    buttonSearch.isEnabled = !state.isLoading

                    val city = state.city
                    val weather = state.weather

                    textWeather.text = when {
                        state.isLoading ->
                            getString(R.string.weather_loading)

                        state.errorRes != null ->
                            getString(state.errorRes)

                        city != null && weather != null ->
                            getString(
                                R.string.weather_result,
                                city.name,
                                city.country,
                                weather.main.temp,
                                weather.weather.firstOrNull()
                                    ?.description.orEmpty()
                            )

                        else ->
                            getString(R.string.weather_placeholder)
                    }
                }
            }
        }
    }
}