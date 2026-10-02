package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.LocationData

@Entity(tableName = "recent_locations")
data class RecentLocationEntity(
    @PrimaryKey val placeId: String,
    val latitude: Double,
    val longitude: Double,
    val locality: String,
    val city: String,
    val state: String,
    val country: String,
    val pincode: String,
    val formattedAddress: String,
    val displayName: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toLocationData(): LocationData {
        return LocationData(
            placeId = placeId,
            latitude = latitude,
            longitude = longitude,
            locality = locality,
            city = city,
            state = state,
            country = country,
            pincode = pincode,
            formattedAddress = formattedAddress,
            displayName = displayName,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromLocationData(loc: LocationData): RecentLocationEntity {
            return RecentLocationEntity(
                placeId = loc.placeId.ifBlank { "${loc.latitude}_${loc.longitude}" },
                latitude = loc.latitude,
                longitude = loc.longitude,
                locality = loc.locality,
                city = loc.city,
                state = loc.state,
                country = loc.country,
                pincode = loc.pincode,
                formattedAddress = loc.formattedAddress,
                displayName = loc.displayName,
                timestamp = System.currentTimeMillis()
            )
        }
    }
}
