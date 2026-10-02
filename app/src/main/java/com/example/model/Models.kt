package com.example.model

import androidx.annotation.DrawableRes

enum class PropertyType(val displayName: String) {
    ALL("All"),
    ROOM("Room"),
    PG("PG"),
    HOSTEL("Hostel"),
    APARTMENT("Apartment"),
    VILLA("Villa"),
    OTHER("Other")
}

enum class FoodType(val displayName: String) {
    BOTH("Both Food"),
    VEG_ONLY("Veg Only"),
    NON_VEG_ONLY("Non-Veg Only"),
    NO_FOOD("No Food")
}

enum class PropertyStatus(val displayName: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    DRAFT("Draft")
}

data class RoomOption(
    val id: String,
    val name: String,
    val price: Int,
    val subtitle: String? = null
)

data class Landmark(
    val name: String,
    val distance: String,
    val iconType: String = "landmark"
)

data class Property(
    val id: String,
    val title: String,
    val propertyType: PropertyType,
    val location: String,
    val address: String = location,
    val locality: String = "Alkapuri",
    val city: String = "Vadodara",
    val state: String = "Gujarat",
    val country: String = "India",
    val pincode: String = "390007",
    val latitude: Double = 22.3072,
    val longitude: Double = 73.1812,
    val rent: Int,
    val deposit: Int = 1000,
    @DrawableRes val images: List<Int>,
    val rating: Double = 4.7,
    val reviewsCount: Int = 358,
    val roomsCount: Int = 3,
    val occupancy: String = "Sharing (2/3)",
    val foodType: FoodType = FoodType.BOTH,
    val gender: String = "For Boys",
    val amenities: List<String> = listOf("Wi-Fi", "AC", "24x7 Security", "Furnished"),
    val description: String = "Fully furnished PG for boys with all modern amenities. Clean rooms, home-cooked food, high-speed Wi-Fi, 24x7 security and peaceful environment. Located near MS University, Alkapuri.",
    val rules: String = "No smoking inside rooms. Gate closes at 10:30 PM.",
    val ownerId: String = "owner_001",
    val ownerName: String = "Rahul Sharma",
    val ownerPhone: String = "+91 98765 43210",
    val ownerRole: String = "Property Owner • Member since Jan 2024",
    val isVerified: Boolean = true,
    val noBrokerage: Boolean = true,
    val badgeTag: String? = "Verified",
    val viewsCount: Int = 24,
    val inquiriesCount: Int = 8,
    val bookingsCount: Int = 5,
    val status: PropertyStatus = PropertyStatus.ACTIVE,
    val availableFrom: String = "15 Oct 2026",
    val roomOptions: List<RoomOption> = listOf(
        RoomOption("opt_1", "Shared Room (2-3 people)", rent, "Occupancy 2/3"),
        RoomOption("opt_2", "Single Room", rent + 2500, "Single private"),
        RoomOption("opt_3", "AC Room", rent + 1500, "Air Conditioned")
    ),
    val landmarks: List<Landmark> = listOf(
        Landmark("MS University", "0.8 km", "school"),
        Landmark("Inorbit Mall", "1.5 km", "shopping"),
        Landmark("Bus Stand", "2.0 km", "bus"),
        Landmark("Vadodara Railway Station", "3.5 km", "train")
    )
)

data class LocationData(
    val placeId: String = "loc_vadodara_alkapuri",
    val latitude: Double = 22.3072,
    val longitude: Double = 73.1812,
    val locality: String = "Alkapuri",
    val city: String = "Vadodara",
    val state: String = "Gujarat",
    val country: String = "India",
    val pincode: String = "390007",
    val formattedAddress: String = "Alkapuri, Vadodara, Gujarat",
    val displayName: String = "Vadodara, Gujarat",
    val isCurrentGpsLocation: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadiusKm * c
}

fun formatDistance(distanceKm: Double): String {
    return if (distanceKm < 1.0) {
        val meters = (distanceKm * 1000).toInt().coerceAtLeast(50)
        "$meters m away"
    } else {
        "%.1f km away".format(distanceKm)
    }
}

enum class BookingStatus(val label: String) {
    CONFIRMED("Confirmed"),
    PENDING("Pending"),
    CANCELLED("Cancelled")
}

data class Booking(
    val id: String,
    val property: Property,
    val selectedRoom: RoomOption,
    val moveInDate: String,
    val duration: String,
    val durationMonths: Int,
    val additionalRequests: String = "",
    val paymentMethod: String,
    val paymentStatus: String,
    val bookingStatus: BookingStatus,
    val rentAmount: Int,
    val depositAmount: Int,
    val totalAmount: Int,
    val bookingDate: String = "Today"
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String
)

data class Conversation(
    val id: String,
    val contactName: String,
    val contactSubtitle: String = "Property Owner",
    val isVerified: Boolean = true,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val messages: List<ChatMessage> = emptyList()
)

data class UserProfile(
    val id: String = "owner_001",
    val name: String = "Rahul Sharma",
    val email: String = "owner@purplerooms.com",
    val phone: String = "+91 98765 43210",
    val city: String = "Vadodara, Gujarat",
    val ownerType: String = "Individual Owner"
)

enum class UserRole(val displayName: String) {
    STUDENT("Student"),
    OWNER("Owner")
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val mobile: String,
    val passwordHash: String,
    val role: UserRole,
    val isVerified: Boolean = true,
    val profileImage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
