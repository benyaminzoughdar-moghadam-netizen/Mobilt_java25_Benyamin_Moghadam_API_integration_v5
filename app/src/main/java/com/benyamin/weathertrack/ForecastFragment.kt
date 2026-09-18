package com.benyamin.weathertrack

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ForecastFragment : Fragment(R.layout.fragment_forecast) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.buttonDetails).setOnClickListener {
            findNavController().navigate(R.id.action_forecast_to_details)
        }
    }
}