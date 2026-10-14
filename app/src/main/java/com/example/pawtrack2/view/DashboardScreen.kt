package com.example.pawtrack2.view

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pawtrack2.contract.DashboardContract
import com.example.pawtrack2.model.FirebasePetTrackerRepository
import com.example.pawtrack2.model.PetLocation
import com.example.pawtrack2.model.PetTrackerRepository
import com.example.pawtrack2.model.PetTrackerStatus
import com.example.pawtrack2.model.PetTrackerStatusListener
import com.example.pawtrack2.model.SharedPreferencesAccountRepository
import com.example.pawtrack2.presenter.DashboardPresenter
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var welcomeName by remember { mutableStateOf("Pet Owner") }
    var trackerStatus by remember { mutableStateOf(PetTrackerStatus()) }
    var isLoading by remember { mutableStateOf(true) }
    var connectionError by remember { mutableStateOf<String?>(null) }
    var showLocationMap by rememberSaveable { mutableStateOf(false) }

    val accountRepository = remember { SharedPreferencesAccountRepository(context) }
    val presenter = remember { DashboardPresenter(accountRepository) }
    val trackerRepository: PetTrackerRepository = remember { FirebasePetTrackerRepository() }
    val view = remember {
        object : DashboardContract.View {
            override fun showWelcomeName(fullName: String) {
                welcomeName = fullName
            }
        }
    }

    DisposableEffect(presenter, view) {
        presenter.attachView(view)
        presenter.loadDashboard()
        onDispose { presenter.detachView() }
    }

    DisposableEffect(trackerRepository) {
        val subscription = trackerRepository.observeStatus(
            DEVICE_ID,
            object : PetTrackerStatusListener {
                override fun onStatusChanged(status: PetTrackerStatus) {
                    trackerStatus = status
                    isLoading = false
                    connectionError = null
                }

                override fun onError(error: Exception) {
                    isLoading = false
                    connectionError = error.message ?: "Could not load tracker data"
                }
            }
        )
        onDispose { subscription.unsubscribe() }
    }

    if (showLocationMap) {
        PetLocationMapScreen(
            location = trackerStatus.location,
            isTrackerOnline = trackerStatus.isOnline,
            mapsApiKeyConfigured = context.hasGoogleMapsApiKey(),
            onBack = { showLocationMap = false }
        )
        return
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, $welcomeName! 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Welcome to Paw Track", fontSize = 14.sp)
                }
                TextButton(onClick = onLogout) {
                    Text("Logout")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Pet Overview", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            InfoCard(
                title = "🌡 Temperature",
                value = "No reading",
                description = "Waiting for tracker data"
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoCard(
                title = "💧 Humidity",
                value = "No reading",
                description = "Waiting for tracker data"
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoCard(
                title = "📍 Location",
                value = locationLabel(trackerStatus, isLoading, connectionError),
                description = connectionError
                    ?: locationDescription(trackerStatus.location)
                    ?: "Tap to view map",
                onClick = { showLocationMap = true }
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoCard(
                title = "⚠️ Health Status",
                value = "Not available",
                description = "No health sensor data received"
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoCard(
                title = "🕐 Last Updated",
                value = trackerStatus.updatedAt?.let(::formatTimestamp) ?: "No update yet",
                description = if (trackerStatus.isOnline) {
                    "Tracker is online"
                } else if (isLoading) {
                    "Checking tracker connection"
                } else {
                    "Tracker is offline"
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Paw Track Monitoring",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            connectionError != null -> "Firebase error: $connectionError"
                            isLoading -> "Connecting to the pet tracker..."
                            trackerStatus.isOnline -> "Tracker connected. Tap Location to open the map."
                            else -> "Tracker is offline. Location will appear when it reconnects."
                        }
                    )
                }
            }
        }
    }
}

private fun locationLabel(
    status: PetTrackerStatus,
    isLoading: Boolean,
    error: String?
): String = when {
    error != null -> "Location unavailable"
    isLoading -> "Checking GPS..."
    status.location != null -> "GPS location received"
    status.isOnline -> "Waiting for GPS"
    else -> "Tracker offline"
}

private fun locationDescription(location: PetLocation?): String? =
    location?.let {
        "Lat ${String.format(Locale.US, "%.5f", it.latitude)}, " +
            "Lon ${String.format(Locale.US, "%.5f", it.longitude)} · Tap to view map"
    }

private fun formatTimestamp(timestamp: Long): String =
    DateFormat.getDateTimeInstance().format(Date(timestamp))

@Suppress("DEPRECATION")
private fun Context.hasGoogleMapsApiKey(): Boolean {
    val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    return applicationInfo.metaData
        ?.getString("com.google.android.geo.API_KEY")
        ?.isNotBlank() == true
}

private const val DEVICE_ID = "esp32-pawtrack"
