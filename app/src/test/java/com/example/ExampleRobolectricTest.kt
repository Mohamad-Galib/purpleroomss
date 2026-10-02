package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.LocationRepository
import com.example.model.BookingStatus
import com.example.model.FoodType
import com.example.model.PropertyType
import com.example.model.UserRole
import com.example.viewmodel.AppScreen
import com.example.viewmodel.BottomNavTab
import com.example.viewmodel.PurpleRoomsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var locationRepository: LocationRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = AppDatabase.createInMemory(context)
        authRepository = AuthRepository(database.userDao())
        locationRepository = LocationRepository(database.locationDao())
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    private fun createViewModel(): PurpleRoomsViewModel {
        return PurpleRoomsViewModel(
            customAuthRepository = authRepository,
            customLocationRepository = locationRepository
        )
    }

    @Test
    fun `read string from context verifies Purple Rooms name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Purple Rooms", appName)
    }

    @Test
    fun `test complete student booking flow and COD handling`() {
        val viewModel = createViewModel()
        assertEquals(AppScreen.SPLASH, viewModel.uiState.value.currentScreen)

        // 1. Splash -> Student Home
        viewModel.navigateTo(AppScreen.STUDENT_HOME)
        assertEquals(AppScreen.STUDENT_HOME, viewModel.uiState.value.currentScreen)
        assertEquals(BottomNavTab.HOME, viewModel.uiState.value.currentTab)

        // 2. Select property -> Property Details
        val firstProp = viewModel.uiState.value.properties.first()
        viewModel.selectProperty(firstProp)
        assertEquals(AppScreen.PROPERTY_DETAILS, viewModel.uiState.value.currentScreen)
        assertEquals(firstProp.id, viewModel.uiState.value.selectedProperty.id)

        // 3. Start Booking -> Booking Process
        viewModel.startBooking(firstProp)
        assertEquals(AppScreen.BOOKING_PROCESS, viewModel.uiState.value.currentScreen)
        assertNotNull(viewModel.uiState.value.bookingDraft)

        // 4. Update room and duration
        val customRoom = firstProp.roomOptions.last()
        viewModel.updateSelectedRoom(customRoom)
        assertEquals(customRoom.id, viewModel.uiState.value.bookingDraft?.selectedRoom?.id)

        // 5. Navigate to Payment
        viewModel.navigateTo(AppScreen.PAYMENT)
        assertEquals(AppScreen.PAYMENT, viewModel.uiState.value.currentScreen)

        // 6. Confirm Booking via COD
        val initialBookingCount = viewModel.uiState.value.bookings.size
        viewModel.confirmBooking(isCod = true)
        assertEquals(AppScreen.BOOKING_CONFIRMATION, viewModel.uiState.value.currentScreen)
        assertEquals(initialBookingCount + 1, viewModel.uiState.value.bookings.size)

        val latest = viewModel.uiState.value.latestBooking
        assertNotNull(latest)
        assertEquals("Pending", latest?.paymentStatus)
        assertEquals(BookingStatus.CONFIRMED, latest?.bookingStatus)
        assertTrue(latest?.id?.startsWith("#PR") == true)
    }

    @Test
    fun `test favorites toggle functionality`() {
        val viewModel = createViewModel()
        val propId = "prop_2"

        val initialSaved = viewModel.uiState.value.savedPropertyIds.contains(propId)
        viewModel.toggleFavorite(propId)
        assertEquals(!initialSaved, viewModel.uiState.value.savedPropertyIds.contains(propId))

        viewModel.toggleFavorite(propId)
        assertEquals(initialSaved, viewModel.uiState.value.savedPropertyIds.contains(propId))
    }

    @Test
    fun `test owner post property and dashboard listing`() {
        val viewModel = createViewModel()
        viewModel.switchMode(isOwner = true)
        assertEquals(AppScreen.OWNER_HOME, viewModel.uiState.value.currentScreen)
        assertTrue(viewModel.uiState.value.isOwnerMode)

        val initialPropsCount = viewModel.uiState.value.properties.size
        val initialOwnerPropsCount = viewModel.uiState.value.ownerProperties.size

        viewModel.publishNewProperty(
            title = "Test Luxury Studio",
            type = PropertyType.APARTMENT,
            rent = 14000,
            deposit = 28000,
            description = "A wonderful test apartment",
            address = "Sayajigunj, Vadodara",
            availableFrom = "01 Nov 2026",
            foodType = FoodType.NO_FOOD
        )

        assertEquals(AppScreen.OWNER_DASHBOARD, viewModel.uiState.value.currentScreen)
        assertEquals(initialPropsCount + 1, viewModel.uiState.value.properties.size)
        assertEquals(initialOwnerPropsCount + 1, viewModel.uiState.value.ownerProperties.size)

        val added = viewModel.uiState.value.ownerProperties.first()
        assertEquals("Test Luxury Studio", added.title)
        assertEquals(14000, added.rent)
    }

    @Test
    fun `test owner edit property 1 - rent change`() {
        val viewModel = createViewModel()
        viewModel.switchMode(isOwner = true)

        val initialCount = viewModel.uiState.value.properties.size
        val initialOwnerCount = viewModel.uiState.value.ownerProperties.size

        // 1. Open Edit Property for prop_1
        val opened = viewModel.openEditProperty("prop_1")
        assertTrue("Edit Property should open for owned property", opened)
        assertEquals(AppScreen.EDIT_PROPERTY, viewModel.uiState.value.currentScreen)
        assertEquals("prop_1", viewModel.uiState.value.editingPropertyId)

        val prop1 = viewModel.uiState.value.propertyToEdit
        assertNotNull(prop1)
        assertEquals(6500, prop1?.rent)

        // 2. Change rent to 7000
        val updatedProp = prop1!!.copy(rent = 7000)
        var callbackInvoked = false
        viewModel.updateProperty("prop_1", updatedProp, onSuccess = { callbackInvoked = true })

        // 3. Verify updates
        assertTrue(callbackInvoked)
        assertEquals(initialCount, viewModel.uiState.value.properties.size)
        assertEquals(initialOwnerCount, viewModel.uiState.value.ownerProperties.size)

        val savedInOwnerList = viewModel.uiState.value.ownerProperties.find { it.id == "prop_1" }
        assertNotNull(savedInOwnerList)
        assertEquals(7000, savedInOwnerList?.rent)

        val savedInAllList = viewModel.uiState.value.properties.find { it.id == "prop_1" }
        assertNotNull(savedInAllList)
        assertEquals(7000, savedInAllList?.rent)
    }

    @Test
    fun `test owner edit property 2 - title change`() {
        val viewModel = createViewModel()
        viewModel.switchMode(isOwner = true)

        val opened = viewModel.openEditProperty("prop_2")
        assertTrue(opened)

        val prop2 = viewModel.uiState.value.propertyToEdit
        assertNotNull(prop2)
        assertEquals("Student Hostel", prop2?.title)

        val updatedProp = prop2!!.copy(title = "Modern Student Hostel Near MSU")
        viewModel.updateProperty("prop_2", updatedProp)

        val savedInOwnerList = viewModel.uiState.value.ownerProperties.find { it.id == "prop_2" }
        assertEquals("Modern Student Hostel Near MSU", savedInOwnerList?.title)

        val savedInAllList = viewModel.uiState.value.properties.find { it.id == "prop_2" }
        assertEquals("Modern Student Hostel Near MSU", savedInAllList?.title)
    }

    @Test
    fun `test owner edit property 3 - amenities and photos change`() {
        val viewModel = createViewModel()
        viewModel.switchMode(isOwner = true)

        val opened = viewModel.openEditProperty("prop_3")
        assertTrue(opened)

        val prop3 = viewModel.uiState.value.propertyToEdit
        assertNotNull(prop3)

        val newAmenities = prop3!!.amenities + "Garden" + "Study Room"
        val newPhotos = prop3.images + R.drawable.hero_bedroom_splash
        val updatedProp = prop3.copy(amenities = newAmenities, images = newPhotos)

        viewModel.updateProperty("prop_3", updatedProp)

        val savedInOwnerList = viewModel.uiState.value.ownerProperties.find { it.id == "prop_3" }
        assertTrue(savedInOwnerList?.amenities?.contains("Garden") == true)
        assertTrue(savedInOwnerList?.amenities?.contains("Study Room") == true)
        assertEquals(prop3.images.size + 1, savedInOwnerList?.images?.size)
    }

    @Test
    fun `test owner edit property - non existent property returns false`() {
        val viewModel = createViewModel()
        viewModel.switchMode(isOwner = true)

        val opened = viewModel.openEditProperty("prop_non_existent")
        assertFalse("Non existent property should not open", opened)
        assertEquals("Property not found.", viewModel.uiState.value.editPropertyError)
    }

    @Test
    fun `test all owner properties can be edited and saved without duplication`() {
        val viewModel = createViewModel()
        viewModel.switchMode(isOwner = true)

        val ownerProperties = viewModel.uiState.value.ownerProperties
        assertTrue(ownerProperties.isNotEmpty())

        for (prop in ownerProperties) {
            val opened = viewModel.openEditProperty(prop.id)
            assertTrue("Every property in My Properties must be editable", opened)
            assertEquals(AppScreen.EDIT_PROPERTY, viewModel.uiState.value.currentScreen)
            assertEquals(prop.id, viewModel.uiState.value.editingPropertyId)
            assertNotNull(viewModel.uiState.value.propertyToEdit)

            // Modify rent by +500
            val updated = prop.copy(rent = prop.rent + 500)
            viewModel.updateProperty(prop.id, updated)

            assertEquals(AppScreen.OWNER_DASHBOARD, viewModel.uiState.value.currentScreen)
            val savedProp = viewModel.uiState.value.ownerProperties.find { it.id == prop.id }
            assertNotNull(savedProp)
            assertEquals(prop.rent + 500, savedProp?.rent)
            assertEquals(prop.id, savedProp?.id) // ID is preserved!
        }
    }

    @Test
    fun `test email login with student credentials routes to student home`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.initJob.join()
        viewModel.navigateTo(AppScreen.LOGIN)

        viewModel.loginWithEmail("student@purplerooms.com", "password123").join()

        val state = viewModel.uiState.value
        assertNotNull(state.currentUser)
        assertEquals("student_001", state.currentUser?.id)
        assertEquals(UserRole.STUDENT, state.currentUser?.role)
        assertEquals(AppScreen.STUDENT_HOME, state.currentScreen)
        assertFalse(state.isOwnerMode)
        assertNull(state.authError)
    }

    @Test
    fun `test email login with owner credentials routes to owner dashboard`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.initJob.join()
        viewModel.navigateTo(AppScreen.LOGIN)

        viewModel.loginWithEmail("owner@purplerooms.com", "password123").join()

        val state = viewModel.uiState.value
        assertNotNull(state.currentUser)
        assertEquals("owner_001", state.currentUser?.id)
        assertEquals(UserRole.OWNER, state.currentUser?.role)
        assertEquals(AppScreen.OWNER_DASHBOARD, state.currentScreen)
        assertTrue(state.isOwnerMode)
        assertNull(state.authError)
    }

    @Test
    fun `test email login with invalid password displays error`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.initJob.join()
        viewModel.navigateTo(AppScreen.LOGIN)

        viewModel.loginWithEmail("student@purplerooms.com", "wrong_password_999").join()

        val state = viewModel.uiState.value
        assertEquals("Incorrect password. Please try again.", state.authError)
        assertEquals(AppScreen.LOGIN, state.currentScreen)
    }

    @Test
    fun `test mobile OTP login for existing user routes to correct dashboard`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.initJob.join()
        viewModel.sendMobileOtp("9876543210").join()

        val stateAfterOtp = viewModel.uiState.value
        assertEquals(AppScreen.MOBILE_OTP, stateAfterOtp.currentScreen)
        assertNotNull(stateAfterOtp.simulatedSmsToast)

        // Extract 6-digit code from simulated SMS
        val match = Regex("\\b\\d{6}\\b").find(stateAfterOtp.simulatedSmsToast!!)
        assertNotNull(match)
        val otpCode = match!!.value

        viewModel.verifyMobileOtp("9876543210", otpCode).join()

        val finalState = viewModel.uiState.value
        assertNotNull(finalState.currentUser)
        assertEquals("student_001", finalState.currentUser?.id)
        assertEquals(AppScreen.STUDENT_HOME, finalState.currentScreen)
    }

    @Test
    fun `test mobile OTP login for new user triggers complete profile`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.initJob.join()
        val newMobile = "9887766554"
        viewModel.sendMobileOtp(newMobile).join()

        val sms = viewModel.uiState.value.simulatedSmsToast
        assertNotNull(sms)
        val code = Regex("\\b\\d{6}\\b").find(sms!!)!!.value

        viewModel.verifyMobileOtp(newMobile, code).join()

        assertEquals(AppScreen.COMPLETE_PROFILE, viewModel.uiState.value.currentScreen)
        assertEquals(newMobile, viewModel.uiState.value.newRegistrationMobile)

        // Complete profile
        viewModel.completeProfile(
            name = "Kavita Rao",
            email = "kavita@test.com",
            mobile = newMobile,
            role = UserRole.STUDENT
        ).join()

        val state = viewModel.uiState.value
        assertNotNull(state.currentUser)
        assertEquals("Kavita Rao", state.currentUser?.name)
        assertEquals(AppScreen.STUDENT_HOME, state.currentScreen)
    }

    @Test
    fun `test email signup registration`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.initJob.join()
        viewModel.signupWithEmail(
            name = "Devansh Shah",
            email = "devansh@purplerooms.com",
            mobile = "9776655443",
            password = "securePassword123",
            confirmPassword = "securePassword123",
            role = UserRole.OWNER,
            termsAccepted = true
        ).join()

        val state = viewModel.uiState.value
        assertNotNull(state.currentUser)
        assertEquals("Devansh Shah", state.currentUser?.name)
        assertEquals(UserRole.OWNER, state.currentUser?.role)
        assertEquals(AppScreen.OWNER_DASHBOARD, state.currentScreen)
    }

    @Test
    fun `test forgot password reset flow`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.initJob.join()
        viewModel.sendPasswordResetToken("student@purplerooms.com").join()

        val resetToast = viewModel.uiState.value.simulatedResetTokenToast
        assertNotNull(resetToast)
        val token = Regex("\\b\\d{6}\\b").find(resetToast!!)!!.value

        viewModel.resetPassword("student@purplerooms.com", token, "newPassword456", "newPassword456").join()

        assertEquals(AppScreen.LOGIN, viewModel.uiState.value.currentScreen)

        // Now login with new password should succeed
        viewModel.loginWithEmail("student@purplerooms.com", "newPassword456").join()
        assertEquals(AppScreen.STUDENT_HOME, viewModel.uiState.value.currentScreen)
    }

    @Test
    fun `test role security - student is blocked from owner screens and editing`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.initJob.join()
        viewModel.loginWithEmail("student@purplerooms.com", "password123").join()

        assertEquals(UserRole.STUDENT, viewModel.uiState.value.currentUser?.role)

        // Attempt to navigate to Owner Dashboard directly
        viewModel.navigateTo(AppScreen.OWNER_DASHBOARD)
        assertNotEquals(AppScreen.OWNER_DASHBOARD, viewModel.uiState.value.currentScreen)
        assertNotNull(viewModel.uiState.value.securityAccessDeniedMessage)

        // Attempt to edit property
        val editOpened = viewModel.openEditProperty("prop_1")
        assertFalse("Student cannot open edit property", editOpened)
        assertNotNull(viewModel.uiState.value.editPropertyError)
    }

    @Test
    fun `test logout clears session and resets to splash`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.initJob.join()
        viewModel.loginWithEmail("student@purplerooms.com", "password123").join()

        assertNotNull(viewModel.uiState.value.currentUser)

        viewModel.logout().join()

        val state = viewModel.uiState.value
        assertNull(state.currentUser)
        assertEquals(AppScreen.SPLASH, state.currentScreen)
        assertEquals("Logged out successfully.", state.authSuccessMessage)
    }

    @Test
    fun `test location selection updates selectedLocation and coordinates`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.initJob.join()
        val bettiah = com.example.model.LocationData(
            placeId = "city_bettiah",
            latitude = 26.8026,
            longitude = 84.5029,
            locality = "Kamalnath Nagar",
            city = "Bettiah",
            state = "Bihar",
            country = "India",
            pincode = "845438",
            formattedAddress = "Kamalnath Nagar, Bettiah, Bihar",
            displayName = "Bettiah, Bihar"
        )

        viewModel.selectLocation(bettiah)

        val state = viewModel.uiState.value
        assertEquals("Kamalnath Nagar, Bettiah", state.selectedLocation)
        assertEquals(26.8026, state.mapCenterLat, 0.0001)
        assertEquals(84.5029, state.mapCenterLng, 0.0001)
        assertEquals(bettiah.placeId, state.selectedLocationData.placeId)
    }

    @Test
    fun `test location autocomplete search and master database results`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.initJob.join()
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Search for "vad"
        viewModel.searchLocationAutocomplete("vad", context).join()
        val state = viewModel.uiState.value
        assertTrue("Autocomplete results should not be empty for 'vad'", state.locationSearchResults.isNotEmpty())
        assertTrue(state.locationSearchResults.any { it.city.contains("Vadodara", ignoreCase = true) })

        // Search for "bett"
        viewModel.searchLocationAutocomplete("bett", context).join()
        val bettiahState = viewModel.uiState.value
        assertTrue("Autocomplete results should not be empty for 'bett'", bettiahState.locationSearchResults.isNotEmpty())
        assertTrue(bettiahState.locationSearchResults.any { it.city.contains("Bettiah", ignoreCase = true) })
    }

    @Test
    fun `test distance calculation formula and formatting`() {
        // Distance between Vadodara (22.3072, 73.1812) and Ahmedabad (23.0225, 72.5714) ~ 100km
        val distKm = com.example.model.calculateDistanceKm(22.3072, 73.1812, 23.0225, 72.5714)
        assertTrue("Distance between Vadodara and Ahmedabad should be ~100 km", distKm in 90.0..120.0)

        // Distance formatting
        val formattedMeters = com.example.model.formatDistance(0.45)
        assertEquals("450 m away", formattedMeters)

        val formattedKm = com.example.model.formatDistance(2.4)
        assertEquals("2.4 km away", formattedKm)
    }

    @Test
    fun `test map screen and property selection`() {
        val viewModel = createViewModel()
        viewModel.openMapScreen()
        assertEquals(AppScreen.MAP_SCREEN, viewModel.uiState.value.currentScreen)

        val prop = viewModel.uiState.value.properties.first()
        viewModel.selectMapProperty(prop)
        assertEquals(prop.id, viewModel.uiState.value.mapSelectedProperty?.id)

        viewModel.updateMapCenter(22.3200, 73.1900)
        assertEquals(22.3200, viewModel.uiState.value.mapCenterLat, 0.0001)
        assertEquals(73.1900, viewModel.uiState.value.mapCenterLng, 0.0001)
    }

    @Test
    fun `test GPS detection fallback and reverse geocoding`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.initJob.join()
        val context = ApplicationProvider.getApplicationContext<Context>()

        viewModel.detectDeviceLocation(context).join()
        val state = viewModel.uiState.value
        assertFalse(state.isDetectingLocation)
        assertNotNull(state.selectedLocation)
        assertTrue(state.selectedLocation.isNotBlank())
    }
}
