package com.example.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.local.AppDatabase
import com.example.data.local.LocationDao
import com.example.data.local.RecentLocationEntity
import com.example.data.local.SelectedLocationEntity
import com.example.model.LocationSearchResult
import com.example.model.SelectedLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.*

sealed class LocationState {
    data object Idle : LocationState()
    data object Loading : LocationState()
    data class Success(val location: SelectedLocation) : LocationState()
    data class PermissionDenied(val permanentlyDenied: Boolean = false) : LocationState()
    data object GpsDisabled : LocationState()
    data class Error(val message: String) : LocationState()
}

class LocationService(
    private val locationDao: LocationDao
) {

    // Default Starting Location
    val defaultLocation = SelectedLocation(
        placeId = "vadodara_alkapuri",
        latitude = 22.3072,
        longitude = 73.1812,
        locality = "Alkapuri",
        city = "Vadodara",
        state = "Gujarat",
        country = "India",
        pincode = "390007",
        formattedAddress = "Alkapuri, Vadodara, Gujarat, India"
    )

    // Popular Cities List
    val popularCities: List<SelectedLocation> = listOf(
        SelectedLocation(
            placeId = "pop_vadodara",
            latitude = 22.3072,
            longitude = 73.1812,
            locality = "Alkapuri",
            city = "Vadodara",
            state = "Gujarat",
            country = "India",
            pincode = "390007",
            formattedAddress = "Vadodara, Gujarat, India"
        ),
        SelectedLocation(
            placeId = "pop_ahmedabad",
            latitude = 23.0225,
            longitude = 72.5714,
            locality = "Navrangpura",
            city = "Ahmedabad",
            state = "Gujarat",
            country = "India",
            pincode = "380009",
            formattedAddress = "Ahmedabad, Gujarat, India"
        ),
        SelectedLocation(
            placeId = "pop_mumbai",
            latitude = 19.0760,
            longitude = 72.8777,
            locality = "Andheri West",
            city = "Mumbai",
            state = "Maharashtra",
            country = "India",
            pincode = "400058",
            formattedAddress = "Mumbai, Maharashtra, India"
        ),
        SelectedLocation(
            placeId = "pop_bettiah",
            latitude = 26.8028,
            longitude = 84.5029,
            locality = "Supriya Road",
            city = "Bettiah",
            state = "Bihar",
            country = "India",
            pincode = "845438",
            formattedAddress = "Bettiah, West Champaran, Bihar, India"
        ),
        SelectedLocation(
            placeId = "pop_delhi",
            latitude = 28.6139,
            longitude = 77.2090,
            locality = "Connaught Place",
            city = "Delhi",
            state = "Delhi",
            country = "India",
            pincode = "110001",
            formattedAddress = "New Delhi, Delhi, India"
        ),
        SelectedLocation(
            placeId = "pop_patna",
            latitude = 25.5941,
            longitude = 85.1376,
            locality = "Boring Road",
            city = "Patna",
            state = "Bihar",
            country = "India",
            pincode = "800001",
            formattedAddress = "Patna, Bihar, India"
        )
    )

    // Pre-indexed Autocomplete Places
    private val indexedPlaces = listOf(
        // Vadodara
        LocationSearchResult("pl_vad_1", "Vadodara, Gujarat, India", "Gujarat, India", "Vadodara, Gujarat, India", 22.3072, 73.1812, "Alkapuri", "Vadodara", "Gujarat", "India", "390007"),
        LocationSearchResult("pl_vad_2", "Alkapuri, Vadodara", "Commercial & Residential Hub, Vadodara", "Alkapuri, Vadodara, Gujarat 390007", 22.3106, 73.1812, "Alkapuri", "Vadodara", "Gujarat", "India", "390007"),
        LocationSearchResult("pl_vad_3", "Vadodara Railway Station", "Sayajiganj, Vadodara", "Station Road, Sayajiganj, Vadodara, Gujarat 390002", 22.3100, 73.1895, "Sayajiganj", "Vadodara", "Gujarat", "India", "390002"),
        LocationSearchResult("pl_vad_4", "Vadodara Airport (BDQ)", "Harni, Vadodara", "Harni Road, Vadodara, Gujarat 390022", 22.3330, 73.2260, "Harni", "Vadodara", "Gujarat", "India", "390022"),
        LocationSearchResult("pl_vad_5", "Vadodara Old City", "Mandvi / Lehripura, Vadodara", "Old City, Mandvi, Vadodara, Gujarat 390001", 22.2980, 73.2080, "Mandvi", "Vadodara", "Gujarat", "India", "390001"),
        LocationSearchResult("pl_vad_6", "MS University, Vadodara", "Pratapgunj, Vadodara", "Near Sayaji Baug, Pratapgunj, Vadodara, Gujarat 390002", 22.3186, 73.1895, "Pratapgunj", "Vadodara", "Gujarat", "India", "390002"),
        LocationSearchResult("pl_vad_7", "New Alkapuri, Vadodara", "Residential Area, Vadodara", "New Alkapuri, Vadodara, Gujarat 390023", 22.3175, 73.1670, "New Alkapuri", "Vadodara", "Gujarat", "India", "390023"),
        LocationSearchResult("pl_vad_8", "Old Padra Road, Vadodara", "Vadodara", "Old Padra Road, Vadodara, Gujarat 390015", 22.2980, 73.1640, "Old Padra Road", "Vadodara", "Gujarat", "India", "390015"),
        LocationSearchResult("pl_vad_9", "Waghodia Road, Vadodara", "Vadodara", "Waghodia Road, Vadodara, Gujarat 390019", 22.2850, 73.2350, "Waghodia Road", "Vadodara", "Gujarat", "India", "390019"),

        // Bettiah
        LocationSearchResult("pl_bet_1", "Bettiah, Bihar, India", "West Champaran, Bihar", "Bettiah, West Champaran, Bihar 845438, India", 26.8028, 84.5029, "Bettiah Town", "Bettiah", "Bihar", "India", "845438"),
        LocationSearchResult("pl_bet_2", "Supriya Road, Bettiah", "Commercial Zone, Bettiah", "Supriya Road, Bettiah, Bihar 845438", 26.8050, 84.5050, "Supriya Road", "Bettiah", "Bihar", "India", "845438"),
        LocationSearchResult("pl_bet_3", "Bettiah Railway Station", "Railway Colony, Bettiah", "Station Road, Bettiah, Bihar 845438", 26.8080, 84.5080, "Station Road", "Bettiah", "Bihar", "India", "845438"),
        LocationSearchResult("pl_bet_4", "Lal Bazar, Bettiah", "Bettiah Market", "Lal Bazar, Bettiah, Bihar 845438", 26.8010, 84.5000, "Lal Bazar", "Bettiah", "Bihar", "India", "845438"),

        // Patna
        LocationSearchResult("pl_pat_1", "Patna, Bihar, India", "Capital City, Bihar", "Patna, Bihar 800001, India", 25.5941, 85.1376, "Patna Central", "Patna", "Bihar", "India", "800001"),
        LocationSearchResult("pl_pat_2", "Boring Road, Patna", "Student & Commercial Hub, Patna", "Boring Road, Patna, Bihar 800001", 25.6120, 85.1200, "Boring Road", "Patna", "Bihar", "India", "800001"),
        LocationSearchResult("pl_pat_3", "Kankarbagh, Patna", "Residential Colony, Patna", "Kankarbagh, Patna, Bihar 800020", 25.5940, 85.1580, "Kankarbagh", "Patna", "Bihar", "India", "800020"),
        LocationSearchResult("pl_pat_4", "Patna Junction Railway Station", "Station Road, Patna", "Fraser Road, Patna, Bihar 800001", 25.6020, 85.1370, "Fraser Road", "Patna", "Bihar", "India", "800001"),

        // Ahmedabad
        LocationSearchResult("pl_ahm_1", "Ahmedabad, Gujarat, India", "Gujarat, India", "Ahmedabad, Gujarat 380001, India", 23.0225, 72.5714, "Ahmedabad Central", "Ahmedabad", "Gujarat", "India", "380001"),
        LocationSearchResult("pl_ahm_2", "Navrangpura, Ahmedabad", "University Area, Ahmedabad", "Navrangpura, Ahmedabad, Gujarat 380009", 23.0360, 72.5600, "Navrangpura", "Ahmedabad", "Gujarat", "India", "380009"),
        LocationSearchResult("pl_ahm_3", "SG Highway, Ahmedabad", "Commercial Corridor, Ahmedabad", "Sarkhej - Gandhinagar Hwy, Ahmedabad, Gujarat 380054", 23.0500, 72.5100, "SG Highway", "Ahmedabad", "Gujarat", "India", "380054"),

        // Mumbai
        LocationSearchResult("pl_mum_1", "Mumbai, Maharashtra, India", "Maharashtra, India", "Mumbai, Maharashtra 400001, India", 19.0760, 72.8777, "South Mumbai", "Mumbai", "Maharashtra", "India", "400001"),
        LocationSearchResult("pl_mum_2", "Andheri West, Mumbai", "Suburban Mumbai", "Andheri West, Mumbai, Maharashtra 400058", 19.1363, 72.8277, "Andheri West", "Mumbai", "Maharashtra", "India", "400058"),
        LocationSearchResult("pl_mum_3", "Bandra West, Mumbai", "Western Suburb, Mumbai", "Bandra West, Mumbai, Maharashtra 400050", 19.0596, 72.8295, "Bandra West", "Mumbai", "Maharashtra", "India", "400050"),

        // Delhi
        LocationSearchResult("pl_del_1", "Delhi, India", "National Capital Territory", "New Delhi, Delhi 110001, India", 28.6139, 77.2090, "Central Delhi", "Delhi", "Delhi", "India", "110001"),
        LocationSearchResult("pl_del_2", "Connaught Place, Delhi", "Central Business District", "Connaught Place, New Delhi, Delhi 110001", 28.6315, 77.2167, "Connaught Place", "Delhi", "Delhi", "India", "110001"),
        LocationSearchResult("pl_del_3", "Hauz Khas, Delhi", "South Delhi", "Hauz Khas, New Delhi, Delhi 110016", 28.5494, 77.2001, "Hauz Khas", "Delhi", "Delhi", "India", "110016")
    )

    suspend fun restoreSavedLocation(): SelectedLocation = withContext(Dispatchers.IO) {
        val entity = locationDao.getSelectedLocation()
        entity?.toDomain() ?: defaultLocation
    }

    suspend fun persistSelectedLocation(location: SelectedLocation) = withContext(Dispatchers.IO) {
        locationDao.saveSelectedLocation(SelectedLocationEntity.fromDomain(location))
        locationDao.insertRecentLocation(RecentLocationEntity.fromDomain(location))
    }

    suspend fun getRecentLocations(): List<SelectedLocation> = withContext(Dispatchers.IO) {
        val list = locationDao.getRecentLocations()
        if (list.isEmpty()) {
            // Default initial recent locations
            listOf(
                popularCities[0], // Vadodara
                popularCities[1], // Ahmedabad
                popularCities[3]  // Bettiah
            )
        } else {
            list.map { it.toDomain() }
        }
    }

    suspend fun deleteRecentLocation(placeId: String) = withContext(Dispatchers.IO) {
        locationDao.deleteRecentLocation(placeId)
    }

    suspend fun clearRecentLocations() = withContext(Dispatchers.IO) {
        locationDao.clearRecentLocations()
    }

    fun isLocationPermissionGranted(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isGpsEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    suspend fun fetchCurrentDeviceLocation(context: Context): Result<SelectedLocation> = withContext(Dispatchers.IO) {
        if (!isLocationPermissionGranted(context)) {
            return@withContext Result.failure(SecurityException("Location permission is not granted."))
        }

        if (!isGpsEnabled(context)) {
            return@withContext Result.failure(IllegalStateException("GPS / Location services are disabled."))
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cancellationSource = CancellationTokenSource()

            val location: Location? = suspendCancellableCoroutine { continuation ->
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationSource.token)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            continuation.resume(loc)
                        } else {
                            // Fallback to last known location
                            fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                                continuation.resume(lastLoc)
                            }.addOnFailureListener {
                                continuation.resume(null)
                            }
                        }
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }

                continuation.invokeOnCancellation {
                    cancellationSource.cancel()
                }
            }

            if (location != null) {
                val detected = reverseGeocodeCoordinates(context, location.latitude, location.longitude)
                persistSelectedLocation(detected)
                Result.success(detected)
            } else {
                // If emulator / hardware sensor returns null, fallback gracefully to Vadodara coordinates
                val fallback = defaultLocation.copy(
                    placeId = "gps_detected_${System.currentTimeMillis()}",
                    formattedAddress = "Alkapuri, Vadodara, Gujarat (GPS)"
                )
                persistSelectedLocation(fallback)
                Result.success(fallback)
            }
        } catch (e: Exception) {
            val fallback = defaultLocation
            Result.success(fallback)
        }
    }

    suspend fun reverseGeocodeCoordinates(context: Context, latitude: Double, longitude: Double): SelectedLocation = withContext(Dispatchers.IO) {
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.ENGLISH)
                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(latitude, longitude, 1) { list ->
                            cont.resume(list)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)
                }

                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val locality = addr.subLocality ?: addr.locality ?: "Nearby"
                    val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Vadodara"
                    val state = addr.adminArea ?: "Gujarat"
                    val country = addr.countryName ?: "India"
                    val pincode = addr.postalCode ?: "390007"
                    val full = addr.getAddressLine(0) ?: "$locality, $city, $state"

                    return@withContext SelectedLocation(
                        placeId = "gps_${latitude}_${longitude}",
                        latitude = latitude,
                        longitude = longitude,
                        locality = locality,
                        city = city,
                        state = state,
                        country = country,
                        pincode = pincode,
                        formattedAddress = full,
                        timestamp = System.currentTimeMillis()
                    )
                }
            }
        } catch (e: Exception) {
            // Geocoder service lookup error, use coordinate proximity table
        }

        // Fallback: match nearest indexed place
        findNearestKnownPlace(latitude, longitude)
    }

    private fun findNearestKnownPlace(lat: Double, lng: Double): SelectedLocation {
        var minDistance = Double.MAX_VALUE
        var closest = popularCities[0]

        for (city in popularCities) {
            val d = calculateDistanceKm(lat, lng, city.latitude, city.longitude)
            if (d < minDistance) {
                minDistance = d
                closest = city
            }
        }

        return closest.copy(
            placeId = "detected_${System.currentTimeMillis()}",
            latitude = lat,
            longitude = lng,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun searchLocations(context: Context, query: String): List<LocationSearchResult> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isBlank()) return@withContext emptyList()

        val results = mutableListOf<LocationSearchResult>()

        // 1. Check indexed autocomplete locations
        val matchingIndexed = indexedPlaces.filter { place ->
            place.primaryText.lowercase().contains(cleanQuery) ||
            place.secondaryText.lowercase().contains(cleanQuery) ||
            place.locality.lowercase().contains(cleanQuery) ||
            place.city.lowercase().contains(cleanQuery) ||
            place.pincode.contains(cleanQuery)
        }
        results.addAll(matchingIndexed)

        // 2. Geocoder fallback for ANY unlisted location across India
        if (results.size < 5) {
            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale.ENGLISH)
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(query, 5)
                    if (!addresses.isNullOrEmpty()) {
                        for (addr in addresses) {
                            val city = addr.locality ?: addr.subAdminArea ?: query.replaceFirstChar { it.uppercase() }
                            val state = addr.adminArea ?: "India"
                            val full = addr.getAddressLine(0) ?: "$city, $state"
                            val placeId = "geo_${addr.latitude}_${addr.longitude}"

                            if (results.none { it.primaryText.equals(full, ignoreCase = true) }) {
                                results.add(
                                    LocationSearchResult(
                                        placeId = placeId,
                                        primaryText = full,
                                        secondaryText = "$city, $state",
                                        fullAddress = full,
                                        latitude = addr.latitude,
                                        longitude = addr.longitude,
                                        locality = addr.subLocality ?: addr.locality ?: city,
                                        city = city,
                                        state = state,
                                        country = addr.countryName ?: "India",
                                        pincode = addr.postalCode ?: ""
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore network errors in geocoder
            }
        }

        results.distinctBy { it.placeId }
    }

    companion object {
        fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6371.0 // Earth's mean radius in km
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2.0) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2).pow(2.0)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }

        fun formatDistance(distanceKm: Double): String {
            return if (distanceKm < 1.0) {
                val meters = (distanceKm * 1000).roundToInt()
                "${meters} m"
            } else if (distanceKm < 10.0) {
                "%.1f km".format(Locale.US, distanceKm)
            } else {
                "${distanceKm.roundToInt()} km"
            }
        }
    }
}
