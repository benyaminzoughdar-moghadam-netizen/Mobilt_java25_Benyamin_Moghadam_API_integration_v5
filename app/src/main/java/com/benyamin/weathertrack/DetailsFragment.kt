package com.benyamin.weathertrack

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class DetailsFragment : Fragment(R.layout.fragment_details) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.buttonBackHome).setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }
}