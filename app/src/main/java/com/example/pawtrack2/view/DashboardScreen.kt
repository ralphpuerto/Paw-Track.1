package com.example.pawtrack2.view

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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pawtrack2.contract.DashboardContract
import com.example.pawtrack2.model.SharedPreferencesAccountRepository
import com.example.pawtrack2.presenter.DashboardPresenter
import com.example.pawtrack2.model.FirebaseDeviceRepository
import com.example.pawtrack2.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    onLogout: () -> Unit
) {

    val context = LocalContext.current

    var welcomeName by remember { mutableStateOf("Pet Owner") }

    val repository = remember { SharedPreferencesAccountRepository(context) }
    val presenter = remember { DashboardPresenter(repository) }
    val deviceViewModel = remember {
        DashboardViewModel(FirebaseDeviceRepository(), DEVICE_ID)
    }
    val deviceState by deviceViewModel.uiState.collectAsState()

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
    DisposableEffect(deviceViewModel) {
        deviceViewModel.startObserving()
        onDispose { deviceViewModel.stopObserving() }
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
                    Text(text = "Welcome to Paw Track", fontSize = 14.sp)
                }

                TextButton(onClick = onLogout) {
                    Text("Logout")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Pet Overview", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))

            InfoCard(title = "🌡 Temperature", value = "28.5 °C", description = "Normal")

            Spacer(modifier = Modifier.height(12.dp))

            InfoCard(title = "💧 Humidity", value = "65%", description = "Current humidity")

            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("External LED", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            when {
                                deviceState.errorMessage != null -> "Error: ${deviceState.errorMessage}"
                                !deviceState.status.online -> "Device offline"
                                deviceState.isSending -> "Sending command..."
                                else -> if (deviceState.status.ledOn) "On" else "Off"
                            },
                            fontSize = 13.sp
                        )
                        Text(
                            "Firebase: devices/$DEVICE_ID/status",
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = deviceState.status.ledOn,
                        enabled = deviceState.status.online && !deviceState.isSending,
                        onCheckedChange = deviceViewModel::setLed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            InfoCard(
                title = "📍 Location",
                value = "GPS Active",
                description = "Location data available"
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoCard(title = "⚠️ Health Status", value = "Normal", description = "No active alerts")

            Spacer(modifier = Modifier.height(12.dp))

            InfoCard(
                title = "🕐 Last Updated",
                value = "Just now",
                description = "Latest available reading"
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
                        text = "Your pet's live sensor and GPS data will appear here once the IoT device and Firebase are connected."
                    )
                }
            }

        }
    }
}

private const val DEVICE_ID = "esp32-pawtrack"