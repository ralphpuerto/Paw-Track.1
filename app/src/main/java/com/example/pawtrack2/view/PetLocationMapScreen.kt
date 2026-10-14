package com.example.pawtrack2.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pawtrack2.model.PetLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun PetLocationMapScreen(
    location: PetLocation?,
    isTrackerOnline: Boolean,
    mapsApiKeyConfigured: Boolean,
    onBack: () -> Unit
) {
    val initialPosition = location?.toLatLng() ?: WORLD_CENTER
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            initialPosition,
            if (location == null) WORLD_ZOOM else PET_ZOOM
        )
    }
    val markerState = remember { MarkerState(position = initialPosition) }

    LaunchedEffect(location) {
        location?.let {
            markerState.position = it.toLatLng()
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it.toLatLng(), PET_ZOOM)
            )
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("Back")
                }
                Text(
                    text = "Pet location",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            when {
                !mapsApiKeyConfigured -> MapSetupMessage()
                else -> {
                    GoogleMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        cameraPositionState = cameraPositionState
                    ) {
                        if (location != null) {
                            Marker(
                                state = markerState,
                                title = "Pet location",
                                snippet = "Latest GPS position from the tracker"
                            )
                        }
                    }

                    Text(
                        text = if (location != null) {
                            "Showing the latest GPS position reported by your pet's tracker."
                        } else if (isTrackerOnline) {
                            "Map is ready. Waiting for the tracker to send GPS coordinates."
                        } else {
                            "World map shown. Pet location is unavailable while the tracker is offline."
                        },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun MapSetupMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Card {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Google Maps API key required",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Add MAPS_API_KEY=your_key to the project's local.properties file, " +
                        "enable Maps SDK for Android in Google Cloud Console, then rebuild the app.",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun PetLocation.toLatLng() = LatLng(latitude, longitude)

private val WORLD_CENTER = LatLng(20.0, 0.0)
private const val WORLD_ZOOM = 2.0f
private const val PET_ZOOM = 16.0f
