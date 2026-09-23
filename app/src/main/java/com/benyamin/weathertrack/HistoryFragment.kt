package com.benyamin.weathertrack

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.benyamin.weathertrack.data.HistoryRepository
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private var historyListener: ListenerRegistration? = null
    private var requestVersion = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.buttonHistoryHome)
            .setOnClickListener {
                findNavController()
                    .popBackStack(R.id.homeFragment, false)
            }
    }

    override fun onStart() {
        super.onStart()

        val currentRequest = ++requestVersion
        val historyText =
            requireView().findViewById<TextView>(R.id.textHistory)

        historyText.setText(R.string.history_loading)

        HistoryRepository.searches()
            .addOnSuccessListener { collection ->
                if (currentRequest != requestVersion) {
                    return@addOnSuccessListener
                }

                historyListener = collection
                    .orderBy("searchedAt", Query.Direction.DESCENDING)
                    .limit(50)
                    .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                        if (currentRequest != requestVersion) {
                            return@addSnapshotListener
                        }

                        if (error != null || snapshot == null) {
                            historyText.setText(R.string.history_error)
                            return@addSnapshotListener
                        }

                        val formatter = SimpleDateFormat(
                            "dd MMM yyyy, HH:mm",
                            Locale.getDefault()
                        )

                        val entries = snapshot.documents.joinToString("\n\n") { document ->
                            val date = document.getTimestamp("searchedAt")
                                ?.toDate()
                                ?.let { formatter.format(it) }
                                ?: getString(R.string.history_pending)

                            getString(
                                R.string.history_entry,
                                document.getString("city").orEmpty(),
                                document.getString("country").orEmpty(),
                                document.getDouble("temperature") ?: 0.0,
                                document.getString("description").orEmpty(),
                                date
                            )
                        }

                        val notice = if (snapshot.metadata.isFromCache) {
                            getString(R.string.history_cached)
                        } else {
                            ""
                        }

                        historyText.text = notice + entries.ifBlank {
                            getString(R.string.history_empty)
                        }
                    }
            }
            .addOnFailureListener {
                if (currentRequest == requestVersion) {
                    historyText.setText(R.string.history_error)
                }
            }
    }

    override fun onStop() {
        requestVersion++
        historyListener?.remove()
        historyListener = null
        super.onStop()
    }
}