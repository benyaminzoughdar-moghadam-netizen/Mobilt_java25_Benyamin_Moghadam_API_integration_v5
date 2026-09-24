# WeatherTrack

WeatherTrack is an Android weather app written in Kotlin.

## Features

- Search for a city and see its current weather.
- View a five-day forecast with updates every three hours.
- See weather details and air quality for the selected city.
- Save and view search history using Firebase anonymous authentication and Cloud Firestore.
- Show an on-demand notification with the current weather.
- Navigate between Home, Forecast, Details, and History.

## APIs and services

- OpenWeather: city lookup, current weather, and five-day forecast.
- Open-Meteo Air Quality: European AQI, PM2.5, and PM10 estimates.
- Firebase Authentication and Cloud Firestore: private search history.

## Run the project

1. Open the project in Android Studio.
2. Add your OpenWeather API key to the project's root `local.properties` file:

   ```properties
   OPENWEATHER_API_KEY=your_api_key_here