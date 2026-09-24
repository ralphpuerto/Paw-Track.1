package com.example.pawtrack2.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pawtrack2.model.DeviceRepository
import com.example.pawtrack2.model.DeviceStatus
import com.example.pawtrack2.model.DeviceStatusListener
import com.example.pawtrack2.model.Subscription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DashboardUiState(
    val status: DeviceStatus = DeviceStatus(),
    val isSending: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val deviceRepository: DeviceRepository,
    private val deviceId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private var subscription: Subscription? = null

    fun startObserving() {
        if (subscription != null) return
        subscription = deviceRepository.observeStatus(deviceId, object : DeviceStatusListener {
            override fun onStatusChanged(status: DeviceStatus) {
                _uiState.value = _uiState.value.copy(status = status, errorMessage = null)
            }

            override fun onError(error: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = error.message ?: "Unable to read device status")
            }
        })
    }

    fun setLed(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isSending = true, errorMessage = null)
        deviceRepository.setLed(deviceId, enabled) { result ->
            _uiState.value = _uiState.value.copy(
                isSending = false,
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }

    fun stopObserving() {
        subscription?.unsubscribe()
        subscription = null
    }

    override fun onCleared() {
        stopObserving()
    }
}
