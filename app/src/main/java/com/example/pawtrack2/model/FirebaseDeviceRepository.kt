package com.example.pawtrack2.model

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val DATABASE_URL = "https://paw-track-fbdb-default-rtdb.firebaseio.com"

class FirebaseDeviceRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(DATABASE_URL)
) : DeviceRepository {

    override fun observeStatus(deviceId: String, listener: DeviceStatusListener): Subscription {
        val reference = database.getReference("devices").child(deviceId).child("status")
        val valueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listener.onStatusChanged(
                    DeviceStatus(
                        ledOn = snapshot.child("led").getValue(Boolean::class.java) ?: false,
                        online = snapshot.child("online").getValue(Boolean::class.java) ?: false,
                        lastUpdated = snapshot.child("updatedAt").getValue(Long::class.java)
                    )
                )
            }

            override fun onCancelled(error: DatabaseError) {
                listener.onError(error.toException())
            }

        }
        reference.addValueEventListener(valueListener)
        return object : Subscription {
            override fun unsubscribe() {
                reference.removeEventListener(valueListener)
            }
        }
    }

    override fun setLed(deviceId: String, enabled: Boolean, onComplete: (Result<Unit>) -> Unit) {
        database.getReference("devices").child(deviceId).child("command").child("led")
            .setValue(enabled)
            .addOnSuccessListener { onComplete(Result.success(Unit)) }
            .addOnFailureListener { onComplete(Result.failure(it)) }
    }
}
