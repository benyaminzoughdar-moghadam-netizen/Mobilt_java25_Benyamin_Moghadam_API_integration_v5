package com.benyamin.weathertrack

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.launch

class DetailsFragment : Fragment(R.layout.fragment_details) {

    private val viewModel: WeatherViewModel
        get() = ViewModelProvider(requireActivity())[
            WeatherViewModel::class.java
        ]

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showWeatherNotification()
        } else {
            showNotificationsDisabled()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val details = view.findViewById<TextView>(R.id.textWeatherDetails)
        val notifyButton = view.findViewById<Button>(R.id.buttonNotifyWeather)
        val missing = getString(R.string.details_missing)

        val dateFormat = SimpleDateFormat(
            "dd MMM yyyy, HH:mm",
            Locale.getDefault()
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        view.findViewById<Button>(R.id.buttonBackHome)
            .setOnClickListener {
                findNavController().popBackStack(R.id.homeFragment, false)
            }

        notifyButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermission.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                showWeatherNotification()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val city = state.city
                    val weather = state.weather

                    notifyButton.isEnabled = city != null && weather != null

                    if (city == null || weather == null) {
                        details.setText(R.string.details_no_weather)
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

    private fun showWeatherNotification() {
        val context = context ?: return
        val state = viewModel.uiState.value
        val city = state.city ?: return
        val weather = state.weather ?: return

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = NotificationManagerCompat.from(context)
        val channelId = "weather_updates"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    getString(R.string.weather_channel),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )

            if (manager.getNotificationChannel(channelId)?.importance ==
                NotificationManager.IMPORTANCE_NONE
            ) {
                showNotificationsDisabled()
                return
            }
        }

        if (!manager.areNotificationsEnabled()) {
            showNotificationsDisabled()
            return
        }

        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val body = getString(
            R.string.notification_body,
            weather.main.temp,
            weather.weather.firstOrNull()?.description.orEmpty()
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(
                getString(
                    R.string.notification_title,
                    city.name,
                    city.country
                )
            )
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
    }

    private fun showNotificationsDisabled() {
        context?.let {
            Toast.makeText(
                it,
                R.string.notifications_disabled,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}