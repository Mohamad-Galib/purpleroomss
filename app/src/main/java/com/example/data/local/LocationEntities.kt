package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.SelectedLocation

@Entity(tableName = "selected_location")
data class SelectedLocationEntity(
    @PrimaryKey val id: Int = 1,
    val placeId: String,
    val latitude: Double,
    val longitude: Double,
    val locality: String,
    val city: String,
    val state: String,
    val country: String = "India",
    val pincode: String = "",
    val formattedAddress: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): SelectedLocation {
        return SelectedLocation(
            placeId = placeId,
            latitude = latitude,
            longitude = longitude,
            locality = locality,
            city = city,
            state = state,
            country = country,
            pincode = pincode,
            formattedAddress = formattedAddress,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomain(loc: SelectedLocation): SelectedLocationEntity {
            return SelectedLocationEntity(
                id = 1,
                placeId = loc.placeId,
                latitude = loc.latitude,
                longitude = loc.longitude,
                locality = loc.locality,
                city = loc.city,
                state = loc.state,
                country = loc.country,
                pincode = loc.pincode,
                formattedAddress = loc.formattedAddress,
                timestamp = loc.timestamp
            )
        }
    }
}

@Entity(tableName = "recent_locations")
data class RecentLocationEntity(
    @PrimaryKey val placeId: String,
    val latitude: Double,
    val longitude: Double,
    val locality: String,
    val city: String,
    val state: String,
    val country: String = "India",
    val pincode: String = "",
    val formattedAddress: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): SelectedLocation {
        return SelectedLocation(
            placeId = placeId,
            latitude = latitude,
            longitude = longitude,
            locality = locality,
            city = city,
            state = state,
            country = country,
            pincode = pincode,
            formattedAddress = formattedAddress,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomain(loc: SelectedLocation): RecentLocationEntity {
            return RecentLocationEntity(
                placeId = loc.placeId,
                latitude = loc.latitude,
                longitude = loc.longitude,
                locality = loc.locality,
                city = loc.city,
                state = loc.state,
                country = loc.country,
                pincode = loc.pincode,
                formattedAddress = loc.formattedAddress,
                timestamp = System.currentTimeMillis()
            )
        }
    }
}
