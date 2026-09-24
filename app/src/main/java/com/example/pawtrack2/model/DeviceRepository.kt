package com.example.pawtrack2.model

/**
 * Firebase-facing device contract. The UI does not know Firebase paths or SDK types.
 */
interface DeviceRepository {
    fun observeStatus(deviceId: String, listener: DeviceStatusListener): Subscription
    fun setLed(deviceId: String, enabled: Boolean, onComplete: (Result<Unit>) -> Unit)
}

interface DeviceStatusListener {
    fun onStatusChanged(status: DeviceStatus)
    fun onError(error: Exception)
}

interface Subscription {
    fun unsubscribe()
}

data class DeviceStatus(
    val ledOn: Boolean = false,
    val online: Boolean = false,
    val lastUpdated: Long? = null
)
