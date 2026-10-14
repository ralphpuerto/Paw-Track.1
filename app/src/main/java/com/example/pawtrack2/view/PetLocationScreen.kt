package com.example.pawtrack2.view

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pawtrack2.model.PetLocationListener
import com.example.pawtrack2.model.PetLocationRepository
import com.example.pawtrack2.model.PetTrackerLocation
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun PetLocationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val mapView = remember(context) { createMapView(context) }
    val repository = remember { PetLocationRepository() }
    var trackerLocation by remember { mutableStateOf<PetTrackerLocation?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(repository) {
        val subscription = repository.observe(DEVICE_ID, object : PetLocationListener {
            override fun onLocationChanged(location: PetTrackerLocation) {
                trackerLocation = location
                locationError = null
            }

            override fun onError(error: Exception) {
                locationError = error.message ?: "Unable to read tracker location"
            }
        })
        onDispose { subscription.unsubscribe() }
    }

    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    androidx.compose.runtime.LaunchedEffect(trackerLocation) {
        val location = trackerLocation ?: return@LaunchedEffect
        val latitude = location.latitude ?: return@LaunchedEffect
        val longitude = location.longitude ?: return@LaunchedEffect
        val point = GeoPoint(latitude, longitude)
        mapView.overlays.filterIsInstance<Marker>()
            .filter { it.id == PET_MARKER_ID }
            .forEach(mapView.overlays::remove)
        mapView.overlays.add(
            Marker(mapView).apply {
                id = PET_MARKER_ID
                position = point
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Pet location"
                snippet = "Latest location reported by the tracker"
            }
        )
        mapView.controller.animateTo(point)
        mapView.controller.setZoom(PET_ZOOM)
        mapView.invalidate()
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
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("Back")
                }
                Text(
                    text = "Pet location",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = when {
                    locationError != null -> "Firebase error: $locationError"
                    trackerLocation?.latitude != null -> "Showing the latest GPS position from the pet tracker."
                    trackerLocation?.online == true -> "Tracker online; waiting for a GPS fix."
                    trackerLocation != null -> "Tracker offline; showing the world map until it reconnects."
                    else -> "Connecting to the pet tracker..."
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            AndroidView(
                factory = { mapView },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Text(
                text = "Map data © OpenStreetMap contributors",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun createMapView(context: Context): MapView {
    Configuration.getInstance().userAgentValue =
        "PawTrack/1.0 (Android; com.example.pawtrack2; " +
            "https://github.com/ralphpuerto/Paw-Track.1)"

    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(2.0)
        controller.setCenter(GeoPoint(0.0, 0.0))
    }
}

private const val DEVICE_ID = "esp32-pawtrack"
private const val PET_MARKER_ID = "paw-track-pet"
private const val PET_ZOOM = 17.0
