package com.example.pawtrack2.model

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val DATABASE_URL = "https://paw-track-fbdb-default-rtdb.firebaseio.com"

interface PetTrackerRepository {
    fun observeStatus(deviceId: String, listener: PetTrackerStatusListener): PetTrackerSubscription
}

interface PetTrackerStatusListener {
    fun onStatusChanged(status: PetTrackerStatus)
    fun onError(error: Exception)
}

interface PetTrackerSubscription {
    fun unsubscribe()
}

data class PetLocation(
    val latitude: Double,
    val longitude: Double
)

data class PetTrackerStatus(
    val isOnline: Boolean = false,
    val location: PetLocation? = null,
    val updatedAt: Long? = null
)

class FirebasePetTrackerRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(DATABASE_URL)
) : PetTrackerRepository {

    override fun observeStatus(
        deviceId: String,
        listener: PetTrackerStatusListener
    ): PetTrackerSubscription {
        val reference = database.getReference("devices").child(deviceId).child("status")
        val valueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latitude = snapshot.child("location").child("latitude").numberValue()
                val longitude = snapshot.child("location").child("longitude").numberValue()
                val location = if (
                    latitude != null && longitude != null &&
                    latitude in -90.0..90.0 && longitude in -180.0..180.0
                ) {
                    PetLocation(latitude, longitude)
                } else {
                    null
                }

                listener.onStatusChanged(
                    PetTrackerStatus(
                        isOnline = snapshot.child("online").getValue(Boolean::class.java) ?: false,
                        location = location,
                        updatedAt = snapshot.child("location").child("updatedAt")
                            .numberValue()
                            ?.toLong()
                            ?.takeIf { it >= MIN_EPOCH_MILLISECONDS }
                    )
                )
            }

            override fun onCancelled(error: DatabaseError) {
                listener.onError(error.toException())
            }
        }
        reference.addValueEventListener(valueListener)
        return object : PetTrackerSubscription {
            override fun unsubscribe() {
                reference.removeEventListener(valueListener)
            }
        }
    }
}

private fun DataSnapshot.numberValue(): Double? =
    (getValue() as? Number)?.toDouble()

private const val MIN_EPOCH_MILLISECONDS = 946_684_800_000L
