package com.benyamin.weathertrack.data

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object HistoryRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()
    private var loginTask: Task<String>? = null

    @Synchronized
    private fun userId(): Task<String> {
        auth.currentUser?.let {
            return Tasks.forResult(it.uid)
        }

        loginTask?.let {
            if (!it.isComplete) return it
        }

        return auth.signInAnonymously().continueWith { task ->
            task.result.user?.uid ?: error("Sign-in failed")
        }.also {
            loginTask = it
        }
    }

    fun searches(): Task<CollectionReference> {
        return userId().continueWith { task ->
            database.collection("users")
                .document(task.result)
                .collection("searches")
        }
    }

    fun save(
        city: CityLocation,
        weather: WeatherResponse
    ): Task<DocumentReference> {
        val entry = hashMapOf<String, Any>(
            "city" to city.name,
            "country" to city.country,
            "temperature" to weather.main.temp,
            "description" to
                    (weather.weather.firstOrNull()?.description ?: ""),
            "searchedAt" to FieldValue.serverTimestamp()
        )

        return searches().continueWithTask { task ->
            task.result.add(entry)
        }
    }
}