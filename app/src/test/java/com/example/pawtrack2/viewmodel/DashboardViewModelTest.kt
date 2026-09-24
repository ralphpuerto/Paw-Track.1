package com.example.pawtrack2.viewmodel

import com.example.pawtrack2.model.DeviceRepository
import com.example.pawtrack2.model.DeviceStatus
import com.example.pawtrack2.model.DeviceStatusListener
import com.example.pawtrack2.model.Subscription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardViewModelTest {
    @Test
    fun statusUpdatesAreExposedAndLedCommandUsesDeviceId() {
        val repository = FakeDeviceRepository()
        val viewModel = DashboardViewModel(repository, "device-1")

        viewModel.startObserving()
        repository.emit(DeviceStatus(ledOn = true, online = true))
        assertTrue(viewModel.uiState.value.status.ledOn)
        assertTrue(viewModel.uiState.value.status.online)

        viewModel.setLed(false)
        assertEquals("device-1", repository.commandDeviceId)
        assertFalse(repository.commandValue)
        assertFalse(viewModel.uiState.value.isSending)
    }

    private class FakeDeviceRepository : DeviceRepository {
        private var listener: DeviceStatusListener? = null
        var commandDeviceId: String? = null
        var commandValue = true

        override fun observeStatus(deviceId: String, listener: DeviceStatusListener): Subscription {
            this.listener = listener
            return object : Subscription {
                override fun unsubscribe() {
                    this@FakeDeviceRepository.listener = null
                }
            }
        }

        override fun setLed(deviceId: String, enabled: Boolean, onComplete: (Result<Unit>) -> Unit) {
            commandDeviceId = deviceId
            commandValue = enabled
            onComplete(Result.success(Unit))
        }

        fun emit(status: DeviceStatus) {
            listener?.onStatusChanged(status)
        }
    }
}
