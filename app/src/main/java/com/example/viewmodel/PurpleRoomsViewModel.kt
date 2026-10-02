package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.PurpleRoomsApplication
import com.example.data.SampleData
import com.example.data.local.AppDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.data.repository.OtpVerifyResult
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AppScreen {
    SPLASH,
    LOGIN,
    MOBILE_OTP,
    COMPLETE_PROFILE,
    SIGNUP,
    FORGOT_PASSWORD,
    CHOOSE_LOCATION,
    MAP_SCREEN,
    STUDENT_HOME,
    SEARCH_EXPLORE,
    PROPERTY_DETAILS,
    BOOKING_PROCESS,
    PAYMENT,
    BOOKING_CONFIRMATION,
    MY_BOOKINGS,
    MESSAGES,
    CHAT_SCREEN,
    PROFILE,
    SAVED_PROPERTIES,
    OWNER_HOME,
    OWNER_DASHBOARD,
    POST_PROPERTY,
    EDIT_PROPERTY
}

enum class BottomNavTab {
    HOME,
    SEARCH,
    BOOKINGS,
    MESSAGES,
    PROFILE,
    OWNER_PROPERTIES
}

data class BookingDraft(
    val property: Property,
    val selectedRoom: RoomOption,
    val moveInDate: String = "15 Oct 2026",
    val durationMonths: Int = 6,
    val durationLabel: String = "6 Months",
    val additionalRequests: String = "",
    val paymentMethod: String = "Online Payment",
    val rentAmount: Int = property.rent,
    val depositAmount: Int = property.deposit,
    val serviceFee: Int = 0,
    val totalAmount: Int = property.rent + property.deposit
)

data class UiState(
    val currentScreen: AppScreen = AppScreen.SPLASH,
    val screenStack: List<AppScreen> = listOf(AppScreen.SPLASH),
    val currentTab: BottomNavTab = BottomNavTab.HOME,
    val isOwnerMode: Boolean = false,
    val currentUser: User? = null,
    val intendedRole: UserRole = UserRole.STUDENT,
    val isAuthLoading: Boolean = false,
    val authLoadingMessage: String = "",
    val authError: String? = null,
    val authSuccessMessage: String? = null,
    val otpSentMobile: String = "",
    val simulatedSmsToast: String? = null,
    val simulatedResetTokenToast: String? = null,
    val newRegistrationMobile: String = "",
    val isSessionRestored: Boolean = false,
    val securityAccessDeniedMessage: String? = null,
    val selectedLocationData: LocationData = LocationData(
        placeId = "city_vadodara",
        latitude = 22.3072,
        longitude = 73.1812,
        locality = "Alkapuri",
        city = "Vadodara",
        state = "Gujarat",
        country = "India",
        pincode = "390007",
        formattedAddress = "Alkapuri, Vadodara, Gujarat",
        displayName = "Vadodara, Gujarat"
    ),
    val selectedLocation: String = "Vadodara, Gujarat",
    val recentLocations: List<LocationData> = emptyList(),
    val popularCities: List<LocationData> = emptyList(),
    val locationSearchResults: List<LocationData> = emptyList(),
    val isDetectingLocation: Boolean = false,
    val locationDetectionError: String? = null,
    val locationPermissionDenied: Boolean = false,
    val gpsDisabled: Boolean = false,
    val mapSelectedProperty: Property? = null,
    val mapCenterLat: Double = 22.3072,
    val mapCenterLng: Double = 73.1812,
    val searchQuery: String = "",
    val selectedCategory: PropertyType = PropertyType.ALL,
    val minBudget: Float = 3000f,
    val maxBudget: Float = 15000f,
    val selectedAmenities: Set<String> = emptySet(),
    val selectedFoodType: FoodType? = null,
    val selectedGender: String? = null,
    val sortOption: String = "Recommended",
    val properties: List<Property> = SampleData.allProperties,
    val savedPropertyIds: Set<String> = setOf("prop_1", "prop_3"),
    val selectedProperty: Property = SampleData.property1,
    val bookingDraft: BookingDraft? = null,
    val latestBooking: Booking? = null,
    val bookings: List<Booking> = SampleData.initialBookings,
    val bookingsTab: String = "All",
    val conversations: List<Conversation> = SampleData.initialConversations,
    val activeConversation: Conversation? = null,
    val userProfile: UserProfile = UserProfile(),
    val ownerPropertiesTab: String = "All",
    val ownerProperties: List<Property> = SampleData.allProperties.take(5),
    val postPropertyStep: Int = 1,
    val newPropertyDraft: Property? = null,
    val editingPropertyId: String? = null,
    val propertyToEdit: Property? = null,
    val isSavingProperty: Boolean = false,
    val editPropertyError: String? = null,
    val editPropertySuccessMessage: String? = null
)

class PurpleRoomsViewModel(
    customAuthRepository: AuthRepository? = null,
    customLocationRepository: com.example.data.repository.LocationRepository? = null
) : ViewModel() {

    private val authRepo: AuthRepository by lazy {
        customAuthRepository ?: run {
            val app = PurpleRoomsApplication.instance
            if (app != null) {
                app.authRepository
            } else {
                val context = try {
                    val appClass = Class.forName("androidx.test.core.app.ApplicationProvider")
                    val method = appClass.getMethod("getApplicationContext")
                    method.invoke(null) as android.content.Context
                } catch (e: Throwable) {
                    null
                }
                if (context != null) {
                    val db = AppDatabase.getInstance(context)
                    AuthRepository(db.userDao())
                } else {
                    throw IllegalStateException("Context not available for AuthRepository")
                }
            }
        }
    }

    private val locationRepo: com.example.data.repository.LocationRepository by lazy {
        customLocationRepository ?: run {
            val app = PurpleRoomsApplication.instance
            if (app != null) {
                app.locationRepository
            } else {
                val context = try {
                    val appClass = Class.forName("androidx.test.core.app.ApplicationProvider")
                    val method = appClass.getMethod("getApplicationContext")
                    method.invoke(null) as android.content.Context
                } catch (e: Throwable) {
                    null
                }
                if (context != null) {
                    val db = AppDatabase.getInstance(context)
                    com.example.data.repository.LocationRepository(db.locationDao())
                } else {
                    com.example.data.repository.LocationRepository()
                }
            }
        }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val initJob: kotlinx.coroutines.Job = viewModelScope.launch {
        try {
            authRepo.initializePreSeededUsers()
            val restoredUser = authRepo.restoreActiveSession()
            if (restoredUser != null) {
                val isOwner = (restoredUser.role == UserRole.OWNER)
                _uiState.update {
                    it.copy(
                        currentUser = restoredUser,
                        isOwnerMode = isOwner,
                        intendedRole = restoredUser.role,
                        userProfile = UserProfile(
                            id = restoredUser.id,
                            name = restoredUser.name,
                            email = restoredUser.email,
                            phone = restoredUser.mobile,
                            ownerType = if (isOwner) "Individual Owner" else "Student / Resident"
                        ),
                        currentScreen = if (isOwner) AppScreen.OWNER_DASHBOARD else AppScreen.STUDENT_HOME,
                        screenStack = listOf(if (isOwner) AppScreen.OWNER_DASHBOARD else AppScreen.STUDENT_HOME),
                        currentTab = if (isOwner) BottomNavTab.OWNER_PROPERTIES else BottomNavTab.HOME,
                        isSessionRestored = true
                    )
                }
            }

            // Initialize location system
            val popular = locationRepo.popularCities
            val recent = locationRepo.getRecentLocations()
            val defaultLoc = recent.firstOrNull() ?: popular.firstOrNull() ?: LocationData()
            _uiState.update {
                it.copy(
                    popularCities = popular,
                    recentLocations = recent,
                    selectedLocationData = defaultLoc,
                    selectedLocation = if (defaultLoc.locality.isNotBlank() && defaultLoc.locality != defaultLoc.city) "${defaultLoc.locality}, ${defaultLoc.city}" else "${defaultLoc.city}, ${defaultLoc.state}",
                    mapCenterLat = defaultLoc.latitude,
                    mapCenterLng = defaultLoc.longitude
                )
            }
        } catch (e: Exception) {
            // Ignore initialization failures in testing environments
        }
    }

    fun navigateTo(screen: AppScreen) {
        val ownerOnlyScreens = listOf(
            AppScreen.OWNER_HOME,
            AppScreen.OWNER_DASHBOARD,
            AppScreen.POST_PROPERTY,
            AppScreen.EDIT_PROPERTY
        )
        val user = _uiState.value.currentUser
        if (screen in ownerOnlyScreens && user != null && user.role == UserRole.STUDENT) {
            _uiState.update {
                it.copy(
                    securityAccessDeniedMessage = "Access Denied: Only property owners can access owner dashboard and properties.",
                    authError = "Owner account required."
                )
            }
            return
        }

        _uiState.update { current ->
            val newStack = current.screenStack + screen
            val tab = when (screen) {
                AppScreen.STUDENT_HOME -> BottomNavTab.HOME
                AppScreen.SEARCH_EXPLORE -> BottomNavTab.SEARCH
                AppScreen.MY_BOOKINGS -> BottomNavTab.BOOKINGS
                AppScreen.MESSAGES -> BottomNavTab.MESSAGES
                AppScreen.PROFILE -> BottomNavTab.PROFILE
                AppScreen.OWNER_DASHBOARD -> BottomNavTab.OWNER_PROPERTIES
                else -> current.currentTab
            }
            current.copy(
                currentScreen = screen,
                screenStack = newStack,
                currentTab = tab
            )
        }
    }

    fun navigateBack(): Boolean {
        val stack = _uiState.value.screenStack
        if (stack.size <= 1) {
            return false
        }
        val newStack = stack.dropLast(1)
        val targetScreen = newStack.last()
        val tab = when (targetScreen) {
            AppScreen.STUDENT_HOME -> BottomNavTab.HOME
            AppScreen.SEARCH_EXPLORE -> BottomNavTab.SEARCH
            AppScreen.MY_BOOKINGS -> BottomNavTab.BOOKINGS
            AppScreen.MESSAGES -> BottomNavTab.MESSAGES
            AppScreen.PROFILE -> BottomNavTab.PROFILE
            AppScreen.OWNER_DASHBOARD -> BottomNavTab.OWNER_PROPERTIES
            else -> _uiState.value.currentTab
        }
        _uiState.update {
            it.copy(
                currentScreen = targetScreen,
                screenStack = newStack,
                currentTab = tab
            )
        }
        return true
    }

    fun selectTab(tab: BottomNavTab) {
        val targetScreen = when (tab) {
            BottomNavTab.HOME -> if (_uiState.value.isOwnerMode) AppScreen.OWNER_HOME else AppScreen.STUDENT_HOME
            BottomNavTab.SEARCH -> AppScreen.SEARCH_EXPLORE
            BottomNavTab.BOOKINGS -> AppScreen.MY_BOOKINGS
            BottomNavTab.MESSAGES -> AppScreen.MESSAGES
            BottomNavTab.PROFILE -> AppScreen.PROFILE
            BottomNavTab.OWNER_PROPERTIES -> AppScreen.OWNER_DASHBOARD
        }
        _uiState.update {
            it.copy(
                currentScreen = targetScreen,
                screenStack = listOf(targetScreen),
                currentTab = tab
            )
        }
    }

    fun selectProperty(property: Property) {
        _uiState.update {
            it.copy(
                selectedProperty = property,
                bookingDraft = BookingDraft(
                    property = property,
                    selectedRoom = property.roomOptions.firstOrNull() ?: RoomOption("opt_def", "Standard Room", property.rent),
                    rentAmount = property.rent,
                    depositAmount = property.deposit,
                    totalAmount = property.rent + property.deposit
                )
            )
        }
        navigateTo(AppScreen.PROPERTY_DETAILS)
    }

    fun toggleFavorite(propertyId: String) {
        _uiState.update { state ->
            val set = state.savedPropertyIds.toMutableSet()
            if (set.contains(propertyId)) {
                set.remove(propertyId)
            } else {
                set.add(propertyId)
            }
            state.copy(savedPropertyIds = set)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(type: PropertyType) {
        _uiState.update { it.copy(selectedCategory = type) }
    }

    fun setBudgetRange(min: Float, max: Float) {
        _uiState.update { it.copy(minBudget = min, maxBudget = max) }
    }

    fun toggleAmenityFilter(amenity: String) {
        _uiState.update { state ->
            val set = state.selectedAmenities.toMutableSet()
            if (set.contains(amenity)) set.remove(amenity) else set.add(amenity)
            state.copy(selectedAmenities = set)
        }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedCategory = PropertyType.ALL,
                minBudget = 3000f,
                maxBudget = 15000f,
                selectedAmenities = emptySet(),
                selectedFoodType = null,
                selectedGender = null
            )
        }
    }

    fun openChooseLocationScreen() {
        navigateTo(AppScreen.CHOOSE_LOCATION)
    }

    fun openMapScreen() {
        navigateTo(AppScreen.MAP_SCREEN)
    }

    fun setLocation(location: String) {
        _uiState.update { it.copy(selectedLocation = location) }
    }

    fun selectLocation(location: LocationData) {
        val display = if (location.locality.isNotBlank() && location.locality != location.city) {
            "${location.locality}, ${location.city}"
        } else {
            "${location.city}, ${location.state}"
        }

        _uiState.update {
            it.copy(
                selectedLocationData = location,
                selectedLocation = display,
                mapCenterLat = location.latitude,
                mapCenterLng = location.longitude,
                locationSearchResults = emptyList(),
                locationPermissionDenied = false,
                gpsDisabled = false
            )
        }

        viewModelScope.launch {
            try {
                locationRepo.saveRecentLocation(location)
                val updatedRecents = locationRepo.getRecentLocations()
                _uiState.update { it.copy(recentLocations = updatedRecents) }
            } catch (e: Exception) {
                // Ignore in tests
            }
        }
    }

    fun detectDeviceLocation(context: android.content.Context): kotlinx.coroutines.Job {
        _uiState.update {
            it.copy(
                isDetectingLocation = true,
                locationDetectionError = null,
                locationPermissionDenied = false
            )
        }

        return viewModelScope.launch {
            try {
                val detected = locationRepo.detectCurrentGpsLocation(context)
                selectLocation(detected)
                _uiState.update {
                    it.copy(
                        isDetectingLocation = false,
                        locationDetectionError = null,
                        currentScreen = AppScreen.STUDENT_HOME,
                        screenStack = listOf(AppScreen.STUDENT_HOME)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDetectingLocation = false,
                        locationDetectionError = e.message ?: "Failed to detect GPS location"
                    )
                }
            }
        }
    }

    fun onLocationPermissionDenied() {
        _uiState.update {
            it.copy(
                locationPermissionDenied = true,
                isDetectingLocation = false
            )
        }
    }

    fun clearLocationPermissionError() {
        _uiState.update {
            it.copy(
                locationPermissionDenied = false,
                gpsDisabled = false,
                locationDetectionError = null
            )
        }
    }

    fun searchLocationAutocomplete(query: String, context: android.content.Context?): kotlinx.coroutines.Job {
        return viewModelScope.launch {
            try {
                val results = locationRepo.searchLocations(query, context)
                _uiState.update { it.copy(locationSearchResults = results) }
            } catch (e: Exception) {
                _uiState.update { it.copy(locationSearchResults = emptyList()) }
            }
        }
    }

    fun clearRecentLocations() {
        viewModelScope.launch {
            locationRepo.clearRecentLocations()
            _uiState.update { it.copy(recentLocations = emptyList()) }
        }
    }

    fun deleteRecentLocation(placeId: String) {
        viewModelScope.launch {
            locationRepo.deleteRecentLocation(placeId)
            val updated = locationRepo.getRecentLocations()
            _uiState.update { it.copy(recentLocations = updated) }
        }
    }

    fun updateMapCenter(lat: Double, lng: Double) {
        _uiState.update {
            it.copy(
                mapCenterLat = lat,
                mapCenterLng = lng
            )
        }
    }

    fun selectMapProperty(property: Property?) {
        _uiState.update { it.copy(mapSelectedProperty = property) }
    }

    // Booking flow methods
    fun startBooking(property: Property) {
        val initialRoom = property.roomOptions.firstOrNull() ?: RoomOption("opt_1", "Shared Room", property.rent)
        _uiState.update {
            it.copy(
                selectedProperty = property,
                bookingDraft = BookingDraft(
                    property = property,
                    selectedRoom = initialRoom,
                    rentAmount = initialRoom.price,
                    depositAmount = property.deposit,
                    totalAmount = initialRoom.price + property.deposit
                )
            )
        }
        navigateTo(AppScreen.BOOKING_PROCESS)
    }

    fun updateSelectedRoom(roomOption: RoomOption) {
        _uiState.update { state ->
            val draft = state.bookingDraft ?: return@update state
            val total = roomOption.price + draft.depositAmount + draft.serviceFee
            state.copy(
                bookingDraft = draft.copy(
                    selectedRoom = roomOption,
                    rentAmount = roomOption.price,
                    totalAmount = total
                )
            )
        }
    }

    fun updateMoveInDate(date: String) {
        _uiState.update { state ->
            val draft = state.bookingDraft ?: return@update state
            state.copy(bookingDraft = draft.copy(moveInDate = date))
        }
    }

    fun updateDuration(months: Int, label: String) {
        _uiState.update { state ->
            val draft = state.bookingDraft ?: return@update state
            state.copy(
                bookingDraft = draft.copy(
                    durationMonths = months,
                    durationLabel = label
                )
            )
        }
    }

    fun updateAdditionalRequests(text: String) {
        _uiState.update { state ->
            val draft = state.bookingDraft ?: return@update state
            state.copy(bookingDraft = draft.copy(additionalRequests = text))
        }
    }

    fun updatePaymentMethod(method: String) {
        _uiState.update { state ->
            val draft = state.bookingDraft ?: return@update state
            state.copy(bookingDraft = draft.copy(paymentMethod = method))
        }
    }

    fun confirmBooking(isCod: Boolean = false) {
        val draft = _uiState.value.bookingDraft ?: return
        val randomNum = (100..999).random()
        val bookingId = "#PR20261015$randomNum"
        val paymentMethod = if (isCod) "Pay at Property (COD)" else draft.paymentMethod
        val paymentStatus = if (isCod) "Pending" else "Paid"
        val bookingStatus = BookingStatus.CONFIRMED

        val newBooking = Booking(
            id = bookingId,
            property = draft.property,
            selectedRoom = draft.selectedRoom,
            moveInDate = draft.moveInDate,
            duration = draft.durationLabel,
            durationMonths = draft.durationMonths,
            additionalRequests = draft.additionalRequests,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            bookingStatus = bookingStatus,
            rentAmount = draft.rentAmount,
            depositAmount = draft.depositAmount,
            totalAmount = draft.totalAmount,
            bookingDate = "Today"
        )

        _uiState.update { state ->
            state.copy(
                bookings = listOf(newBooking) + state.bookings,
                latestBooking = newBooking,
                bookingDraft = null
            )
        }
        navigateTo(AppScreen.BOOKING_CONFIRMATION)
    }

    fun cancelBooking(bookingId: String) {
        _uiState.update { state ->
            val updated = state.bookings.map {
                if (it.id == bookingId) it.copy(bookingStatus = BookingStatus.CANCELLED, paymentStatus = "Cancelled")
                else it
            }
            state.copy(bookings = updated)
        }
    }

    fun setBookingsTab(tab: String) {
        _uiState.update { it.copy(bookingsTab = tab) }
    }

    // Chat methods
    fun openConversation(conversation: Conversation) {
        // Mark as read
        val updated = _uiState.value.conversations.map {
            if (it.id == conversation.id) it.copy(unreadCount = 0) else it
        }
        _uiState.update {
            it.copy(
                conversations = updated,
                activeConversation = conversation.copy(unreadCount = 0)
            )
        }
        navigateTo(AppScreen.CHAT_SCREEN)
    }

    fun openOwnerChat(property: Property) {
        // Find existing or create conversation
        val existing = _uiState.value.conversations.find { it.contactName.contains(property.ownerName) || it.contactSubtitle.contains(property.title) }
        if (existing != null) {
            openConversation(existing)
        } else {
            val newConv = Conversation(
                id = "conv_${System.currentTimeMillis()}",
                contactName = property.ownerName,
                contactSubtitle = "Owner • ${property.title}",
                isVerified = property.isVerified,
                lastMessage = "Hi! Thanks for checking out ${property.title}. How can I help you?",
                timestamp = "Just now",
                unreadCount = 0,
                messages = listOf(
                    ChatMessage(
                        id = "msg_${System.currentTimeMillis()}",
                        text = "Hi! Thanks for checking out ${property.title}. How can I help you?",
                        isFromUser = false,
                        timestamp = "Just now"
                    )
                )
            )
            _uiState.update { it.copy(conversations = listOf(newConv) + it.conversations) }
            openConversation(newConv)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val currentConv = _uiState.value.activeConversation ?: return
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        val userMessage = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            text = text.trim(),
            isFromUser = true,
            timestamp = currentTime
        )

        val updatedMessages = currentConv.messages + userMessage
        val updatedConv = currentConv.copy(
            messages = updatedMessages,
            lastMessage = text.trim(),
            timestamp = currentTime
        )

        val updatedList = _uiState.value.conversations.map {
            if (it.id == updatedConv.id) updatedConv else it
        }

        _uiState.update {
            it.copy(
                activeConversation = updatedConv,
                conversations = updatedList
            )
        }

        // Simulate realistic owner reply
        viewModelScope.launch {
            delay(1200)
            val replyText = when {
                text.contains("price", ignoreCase = true) || text.contains("rent", ignoreCase = true) ->
                    "Rent is inclusive of Wi-Fi and maintenance. Deposit is fully refundable on check-out!"
                text.contains("visit", ignoreCase = true) || text.contains("see", ignoreCase = true) ->
                    "You're welcome to visit tomorrow between 10 AM and 6 PM. Shall I share the exact Google Maps location?"
                text.contains("food", ignoreCase = true) ->
                    "Yes, hygienic breakfast and dinner are served daily with both veg and non-veg options on weekends."
                else ->
                    "Thank you for your message! I've noted your request and will assist you right away."
            }
            val replyTime = timeFormat.format(Date())
            val replyMessage = ChatMessage(
                id = "reply_${System.currentTimeMillis()}",
                text = replyText,
                isFromUser = false,
                timestamp = replyTime
            )
            _uiState.update { state ->
                val active = state.activeConversation
                if (active != null && active.id == updatedConv.id) {
                    val withReply = active.copy(
                        messages = active.messages + replyMessage,
                        lastMessage = replyText,
                        timestamp = replyTime
                    )
                    val allConvs = state.conversations.map { if (it.id == withReply.id) withReply else it }
                    state.copy(activeConversation = withReply, conversations = allConvs)
                } else {
                    state
                }
            }
        }
    }

    // Owner management & Role Security
    fun switchMode(isOwner: Boolean) {
        val targetRole = if (isOwner) UserRole.OWNER else UserRole.STUDENT
        val currentUser = if (isOwner) {
            User(
                id = "owner_001",
                name = "Rahul Sharma",
                email = "owner@purplerooms.com",
                mobile = "+91 91234 56789",
                passwordHash = "",
                role = UserRole.OWNER
            )
        } else {
            User(
                id = "student_001",
                name = "Aarav Mehta",
                email = "student@purplerooms.com",
                mobile = "+91 98765 43210",
                passwordHash = "",
                role = UserRole.STUDENT
            )
        }
        _uiState.update {
            it.copy(
                isOwnerMode = isOwner,
                currentUser = currentUser,
                intendedRole = targetRole,
                userProfile = UserProfile(
                    id = currentUser.id,
                    name = currentUser.name,
                    email = currentUser.email,
                    phone = currentUser.mobile,
                    ownerType = if (isOwner) "Individual Owner" else "Student / Resident"
                ),
                currentScreen = if (isOwner) AppScreen.OWNER_HOME else AppScreen.STUDENT_HOME,
                screenStack = listOf(if (isOwner) AppScreen.OWNER_HOME else AppScreen.STUDENT_HOME),
                currentTab = if (isOwner) BottomNavTab.HOME else BottomNavTab.HOME
            )
        }
    }

    fun setOwnerPropertiesTab(tab: String) {
        _uiState.update { it.copy(ownerPropertiesTab = tab) }
    }

    fun publishNewProperty(
        title: String,
        type: PropertyType,
        rent: Int,
        deposit: Int,
        description: String,
        address: String,
        availableFrom: String,
        foodType: FoodType
    ) {
        val user = _uiState.value.currentUser
        if (user != null && user.role == UserRole.STUDENT) {
            _uiState.update {
                it.copy(
                    securityAccessDeniedMessage = "Access denied: Students cannot publish properties."
                )
            }
            return
        }

        val newProp = Property(
            id = "owner_prop_${System.currentTimeMillis()}",
            title = title,
            propertyType = type,
            location = address,
            city = "Vadodara",
            rent = rent,
            deposit = deposit,
            images = listOf(
                com.example.R.drawable.prop_apartment_living,
                com.example.R.drawable.hero_bedroom_splash
            ),
            rating = 5.0,
            reviewsCount = 1,
            roomsCount = 2,
            occupancy = "Flexible",
            foodType = foodType,
            gender = "Any",
            amenities = listOf("Wi-Fi", "AC", "24x7 Security", "Furnished"),
            description = description,
            ownerName = _uiState.value.userProfile.name,
            ownerPhone = _uiState.value.userProfile.phone,
            ownerRole = "Verified Owner • Just Added",
            isVerified = true,
            noBrokerage = true,
            badgeTag = "New Listing",
            viewsCount = 0,
            inquiriesCount = 0,
            bookingsCount = 0,
            status = PropertyStatus.ACTIVE,
            availableFrom = availableFrom
        )

        _uiState.update { state ->
            state.copy(
                properties = listOf(newProp) + state.properties,
                ownerProperties = listOf(newProp) + state.ownerProperties
            )
        }
        navigateTo(AppScreen.OWNER_DASHBOARD)
    }

    fun openEditProperty(propertyId: String): Boolean {
        val user = _uiState.value.currentUser
        if (user != null && user.role == UserRole.STUDENT) {
            _uiState.update {
                it.copy(
                    editPropertyError = "Access denied: Only verified property owners can edit properties.",
                    securityAccessDeniedMessage = "Access denied: Owner privileges required."
                )
            }
            return false
        }

        val property = _uiState.value.ownerProperties.find { it.id == propertyId }
            ?: _uiState.value.properties.find { it.id == propertyId }
            ?: SampleData.allProperties.find { it.id == propertyId }

        if (property == null) {
            _uiState.update { it.copy(editPropertyError = "Property not found.") }
            return false
        }

        _uiState.update {
            it.copy(
                editingPropertyId = propertyId,
                propertyToEdit = property,
                editPropertyError = null,
                editPropertySuccessMessage = null,
                isSavingProperty = false
            )
        }
        navigateTo(AppScreen.EDIT_PROPERTY)
        return true
    }

    fun updateProperty(
        propertyId: String,
        updatedProperty: Property,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = _uiState.value.currentUser
        if (user != null && user.role == UserRole.STUDENT) {
            val errorMsg = "Access denied: Students are not authorized to update properties."
            _uiState.update { it.copy(editPropertyError = errorMsg) }
            onError(errorMsg)
            return
        }

        try {
            // Keep the exact original ID and assign current owner
            val savedProperty = updatedProperty.copy(
                id = propertyId,
                ownerId = _uiState.value.userProfile.id
            )

            _uiState.update { state ->
                val updatedProps = if (state.properties.any { it.id == propertyId }) {
                    state.properties.map { if (it.id == propertyId) savedProperty else it }
                } else {
                    listOf(savedProperty) + state.properties
                }

                val updatedOwnerProps = if (state.ownerProperties.any { it.id == propertyId }) {
                    state.ownerProperties.map { if (it.id == propertyId) savedProperty else it }
                } else {
                    listOf(savedProperty) + state.ownerProperties
                }

                val updatedSelected = if (state.selectedProperty.id == propertyId) savedProperty else state.selectedProperty
                val updatedBookings = state.bookings.map {
                    if (it.property.id == propertyId) it.copy(property = savedProperty) else it
                }

                val newStack = state.screenStack.filter { it != AppScreen.EDIT_PROPERTY }

                state.copy(
                    properties = updatedProps,
                    ownerProperties = updatedOwnerProps,
                    selectedProperty = updatedSelected,
                    bookings = updatedBookings,
                    isSavingProperty = false,
                    editPropertySuccessMessage = "Property '${savedProperty.title}' updated successfully ✓",
                    editPropertyError = null,
                    propertyToEdit = null,
                    editingPropertyId = null,
                    currentScreen = AppScreen.OWNER_DASHBOARD,
                    screenStack = if (newStack.isNotEmpty()) newStack else listOf(AppScreen.OWNER_DASHBOARD),
                    currentTab = BottomNavTab.OWNER_PROPERTIES
                )
            }
            onSuccess()
        } catch (e: Exception) {
            val errorMsg = "Unable to update property. Please try again."
            _uiState.update { it.copy(isSavingProperty = false, editPropertyError = errorMsg) }
            onError(errorMsg)
        }
    }

    fun clearEditFeedback() {
        _uiState.update { it.copy(editPropertyError = null, editPropertySuccessMessage = null) }
    }

    fun updateUserProfile(name: String, email: String, phone: String, city: String) {
        _uiState.update {
            it.copy(
                userProfile = it.userProfile.copy(
                    name = name,
                    email = email,
                    phone = phone,
                    city = city
                )
            )
        }
    }

    // ==========================================
    // AUTHENTICATION METHODS
    // ==========================================

    fun setIntendedRole(role: UserRole) {
        _uiState.update {
            it.copy(
                intendedRole = role,
                isOwnerMode = (role == UserRole.OWNER),
                authError = null
            )
        }
    }

    fun clearAuthFeedback() {
        _uiState.update {
            it.copy(
                authError = null,
                authSuccessMessage = null,
                simulatedSmsToast = null,
                simulatedResetTokenToast = null,
                securityAccessDeniedMessage = null
            )
        }
    }

    fun loginWithEmail(email: String, password: String, onSuccess: () -> Unit = {}): kotlinx.coroutines.Job {
        _uiState.update {
            it.copy(
                isAuthLoading = true,
                authLoadingMessage = "Authenticating...",
                authError = null,
                authSuccessMessage = null
            )
        }

        return viewModelScope.launch {
            when (val result = authRepo.loginWithEmail(email, password)) {
                is AuthResult.Success -> {
                    val user = result.data
                    val isOwner = (user.role == UserRole.OWNER)
                    val targetScreen = if (isOwner) AppScreen.OWNER_DASHBOARD else AppScreen.STUDENT_HOME
                    val targetTab = if (isOwner) BottomNavTab.OWNER_PROPERTIES else BottomNavTab.HOME

                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            isOwnerMode = isOwner,
                            intendedRole = user.role,
                            userProfile = UserProfile(
                                id = user.id,
                                name = user.name,
                                email = user.email,
                                phone = user.mobile,
                                ownerType = if (isOwner) "Individual Owner" else "Student / Resident"
                            ),
                            isAuthLoading = false,
                            authError = null,
                            authSuccessMessage = "Welcome back, ${user.name}!",
                            currentScreen = targetScreen,
                            screenStack = listOf(targetScreen),
                            currentTab = targetTab
                        )
                    }
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authError = result.message
                        )
                    }
                }
                is AuthResult.Loading -> {
                    // Handled above
                }
            }
        }
    }

    fun sendMobileOtp(mobile: String): kotlinx.coroutines.Job {
        _uiState.update {
            it.copy(
                isAuthLoading = true,
                authLoadingMessage = "Sending OTP to $mobile...",
                authError = null,
                simulatedSmsToast = null
            )
        }

        return viewModelScope.launch {
            when (val result = authRepo.sendMobileOtp(mobile)) {
                is AuthResult.Success -> {
                    val code = result.data
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            otpSentMobile = mobile,
                            simulatedSmsToast = "📲 SMS Delivered: Your Purple Rooms verification OTP is $code (Expires in 2 mins)",
                            authError = null
                        )
                    }
                    navigateTo(AppScreen.MOBILE_OTP)
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authError = result.message
                        )
                    }
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    fun verifyMobileOtp(mobile: String, enteredOtp: String): kotlinx.coroutines.Job {
        _uiState.update {
            it.copy(
                isAuthLoading = true,
                authLoadingMessage = "Verifying code...",
                authError = null
            )
        }

        return viewModelScope.launch {
            when (val result = authRepo.verifyMobileOtp(mobile, enteredOtp)) {
                is OtpVerifyResult.ExistingUser -> {
                    val user = result.user
                    val isOwner = (user.role == UserRole.OWNER)
                    val targetScreen = if (isOwner) AppScreen.OWNER_DASHBOARD else AppScreen.STUDENT_HOME
                    val targetTab = if (isOwner) BottomNavTab.OWNER_PROPERTIES else BottomNavTab.HOME

                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            isOwnerMode = isOwner,
                            intendedRole = user.role,
                            userProfile = UserProfile(
                                id = user.id,
                                name = user.name,
                                email = user.email,
                                phone = user.mobile,
                                ownerType = if (isOwner) "Individual Owner" else "Student / Resident"
                            ),
                            isAuthLoading = false,
                            authError = null,
                            authSuccessMessage = "Logged in successfully as ${user.name}",
                            currentScreen = targetScreen,
                            screenStack = listOf(targetScreen),
                            currentTab = targetTab
                        )
                    }
                }
                is OtpVerifyResult.NewUser -> {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            newRegistrationMobile = result.mobile,
                            authError = null
                        )
                    }
                    navigateTo(AppScreen.COMPLETE_PROFILE)
                }
                is OtpVerifyResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authError = result.message
                        )
                    }
                }
                is OtpVerifyResult.Expired -> {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authError = result.message
                        )
                    }
                }
                is OtpVerifyResult.TooManyAttempts -> {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authError = result.message
                        )
                    }
                }
            }
        }
    }

    fun completeProfile(
        name: String,
        email: String,
        mobile: String,
        role: UserRole,
        profilePhoto: String? = null
    ): kotlinx.coroutines.Job {
        _uiState.update {
            it.copy(
                isAuthLoading = true,
                authLoadingMessage = "Creating your account...",
                authError = null
            )
        }

        return viewModelScope.launch {
            when (val result = authRepo.completeProfile(name, email, mobile, role, profilePhoto)) {
                is AuthResult.Success -> {
                    val user = result.data
                    val isOwner = (user.role == UserRole.OWNER)
                    val targetScreen = if (isOwner) AppScreen.OWNER_DASHBOARD else AppScreen.STUDENT_HOME
                    val targetTab = if (isOwner) BottomNavTab.OWNER_PROPERTIES else BottomNavTab.HOME

                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            isOwnerMode = isOwner,
                            intendedRole = user.role,
                            userProfile = UserProfile(
                                id = user.id,
                                name = user.name,
                                email = user.email,
                                phone = user.mobile,
                                ownerType = if (isOwner) "Individual Owner" else "Student / Resident"
                            ),
                            isAuthLoading = false,
                            authError = null,
                            authSuccessMessage = "Account created successfully! Welcome to Purple Rooms.",
                            currentScreen = targetScreen,
                            screenStack = listOf(targetScreen),
                            currentTab = targetTab
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authError = result.message
                        )
                    }
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    fun signupWithEmail(
        name: String,
        email: String,
        mobile: String,
        password: String,
        confirmPassword: String,
        role: UserRole,
        termsAccepted: Boolean
    ): kotlinx.coroutines.Job {
        _uiState.update {
            it.copy(
                isAuthLoading = true,
                authLoadingMessage = "Creating account...",
                authError = null
            )
        }

        return viewModelScope.launch {
            when (val result = authRepo.registerUser(name, email, mobile, password, confirmPassword, role, termsAccepted)) {
                is AuthResult.Success -> {
                    val user = result.data
                    val isOwner = (user.role == UserRole.OWNER)
                    val targetScreen = if (isOwner) AppScreen.OWNER_DASHBOARD else AppScreen.STUDENT_HOME
                    val targetTab = if (isOwner) BottomNavTab.OWNER_PROPERTIES else BottomNavTab.HOME

                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            isOwnerMode = isOwner,
                            intendedRole = user.role,
                            userProfile = UserProfile(
                                id = user.id,
                                name = user.name,
                                email = user.email,
                                phone = user.mobile,
                                ownerType = if (isOwner) "Individual Owner" else "Student / Resident"
                            ),
                            isAuthLoading = false,
                            authError = null,
                            authSuccessMessage = "Account created successfully!",
                            currentScreen = targetScreen,
                            screenStack = listOf(targetScreen),
                            currentTab = targetTab
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authError = result.message
                        )
                    }
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    fun sendPasswordResetToken(email: String): kotlinx.coroutines.Job {
        _uiState.update {
            it.copy(
                isAuthLoading = true,
                authLoadingMessage = "Sending password reset link...",
                authError = null,
                simulatedResetTokenToast = null
            )
        }

        return viewModelScope.launch {
            when (val result = authRepo.sendPasswordResetToken(email)) {
                is AuthResult.Success -> {
                    val token = result.data
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            simulatedResetTokenToast = "🔑 Reset code sent to $email: $token (Valid for 5 mins)",
                            authError = null
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authError = result.message
                        )
                    }
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    fun resetPassword(email: String, token: String, newPassword: String, confirmPassword: String): kotlinx.coroutines.Job {
        _uiState.update {
            it.copy(
                isAuthLoading = true,
                authLoadingMessage = "Updating password...",
                authError = null
            )
        }

        return viewModelScope.launch {
            when (val result = authRepo.resetPassword(email, token, newPassword, confirmPassword)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authSuccessMessage = "Password updated successfully. Please log in.",
                            authError = null,
                            currentScreen = AppScreen.LOGIN,
                            screenStack = listOf(AppScreen.LOGIN)
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authError = result.message
                        )
                    }
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    fun logout(): kotlinx.coroutines.Job {
        return viewModelScope.launch {
            try {
                authRepo.logout()
            } catch (e: Exception) {
                // Ignore
            }
            _uiState.update {
                it.copy(
                    currentUser = null,
                    isOwnerMode = false,
                    intendedRole = UserRole.STUDENT,
                    authError = null,
                    authSuccessMessage = "Logged out successfully.",
                    currentScreen = AppScreen.SPLASH,
                    screenStack = listOf(AppScreen.SPLASH),
                    currentTab = BottomNavTab.HOME
                )
            }
        }
    }
}
