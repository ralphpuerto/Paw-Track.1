package com.example.pawtrack2.model

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val DATABASE_URL = "https://paw-track-fbdb-default-rtdb.firebaseio.com"

data class PetTrackerLocation(
    val online: Boolean,
    val latitude: Double?,
    val longitude: Double?
)

interface PetLocationListener {
    fun onLocationChanged(location: PetTrackerLocation)
    fun onError(error: Exception)
}

fun interface PetLocationSubscription {
    fun unsubscribe()
}

class PetLocationRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(DATABASE_URL)
) {
    fun observe(deviceId: String, listener: PetLocationListener): PetLocationSubscription {
        val reference = database.getReference("devices").child(deviceId).child("status")
        val valueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latitude = snapshot.child("location/latitude").numberValue()
                val longitude = snapshot.child("location/longitude").numberValue()
                val coordinatesAreValid = latitude != null && longitude != null &&
                    latitude in -90.0..90.0 && longitude in -180.0..180.0

                listener.onLocationChanged(
                    PetTrackerLocation(
                        online = snapshot.child("online").getValue(Boolean::class.java) ?: false,
                        latitude = latitude.takeIf { coordinatesAreValid },
                        longitude = longitude.takeIf { coordinatesAreValid }
                    )
                )
            }

            override fun onCancelled(error: DatabaseError) {
                listener.onError(error.toException())
            }
        }

        reference.addValueEventListener(valueListener)
        return PetLocationSubscription {
            reference.removeEventListener(valueListener)
        }
    }
}

private fun DataSnapshot.numberValue(): Double? =
    (getValue() as? Number)?.toDouble()
