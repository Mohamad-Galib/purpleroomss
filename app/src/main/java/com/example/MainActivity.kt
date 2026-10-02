package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.PurpleRoomsBottomBar
import com.example.ui.screens.*
import com.example.ui.theme.BackgroundLavender
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.BottomNavTab
import com.example.viewmodel.PurpleRoomsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PurpleRoomsApp()
            }
        }
    }
}

@Composable
fun PurpleRoomsApp(
    viewModel: PurpleRoomsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle back press properly
    val canGoBack = uiState.screenStack.size > 1 && uiState.currentScreen != AppScreen.SPLASH
    BackHandler(enabled = canGoBack) {
        viewModel.navigateBack()
    }

    val showBottomBar = uiState.currentScreen in listOf(
        AppScreen.STUDENT_HOME,
        AppScreen.SEARCH_EXPLORE,
        AppScreen.MY_BOOKINGS,
        AppScreen.MESSAGES,
        AppScreen.PROFILE,
        AppScreen.OWNER_HOME,
        AppScreen.OWNER_DASHBOARD
    )

    val unreadMessagesCount = uiState.conversations.sumOf { it.unreadCount }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                PurpleRoomsBottomBar(
                    currentTab = uiState.currentTab,
                    onTabSelected = { tab -> viewModel.selectTab(tab) },
                    isOwnerMode = uiState.isOwnerMode,
                    unreadMessagesCount = unreadMessagesCount
                )
            }
        },
        containerColor = BackgroundLavender,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp
                )
        ) {
            AnimatedContent(
                targetState = uiState.currentScreen,
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    AppScreen.SPLASH -> {
                        SplashScreen(
                            onContinueAsStudent = {
                                viewModel.setIntendedRole(com.example.model.UserRole.STUDENT)
                                viewModel.navigateTo(AppScreen.LOGIN)
                            },
                            onContinueAsOwner = {
                                viewModel.setIntendedRole(com.example.model.UserRole.OWNER)
                                viewModel.navigateTo(AppScreen.LOGIN)
                            },
                            onLoginClick = {
                                viewModel.navigateTo(AppScreen.LOGIN)
                            }
                        )
                    }

                    AppScreen.LOGIN -> {
                        LoginScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() },
                            onNavigateToSignup = { viewModel.navigateTo(AppScreen.SIGNUP) },
                            onNavigateToMobileOtp = { viewModel.navigateTo(AppScreen.MOBILE_OTP) },
                            onNavigateToForgotPassword = { viewModel.navigateTo(AppScreen.FORGOT_PASSWORD) }
                        )
                    }

                    AppScreen.MOBILE_OTP -> {
                        MobileOtpScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() },
                            onSwitchToEmailLogin = { viewModel.navigateTo(AppScreen.LOGIN) }
                        )
                    }

                    AppScreen.COMPLETE_PROFILE -> {
                        CompleteProfileScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() }
                        )
                    }

                    AppScreen.SIGNUP -> {
                        SignupScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() },
                            onNavigateToLogin = { viewModel.navigateTo(AppScreen.LOGIN) }
                        )
                    }

                    AppScreen.FORGOT_PASSWORD -> {
                        ForgotPasswordScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() },
                            onNavigateToLogin = { viewModel.navigateTo(AppScreen.LOGIN) }
                        )
                    }

                    AppScreen.CHOOSE_LOCATION -> {
                        ChooseLocationScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() }
                        )
                    }

                    AppScreen.MAP_SCREEN -> {
                        MapScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() }
                        )
                    }

                    AppScreen.STUDENT_HOME -> {
                        HomeScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onNavigateToSearch = { viewModel.selectTab(BottomNavTab.SEARCH) },
                            onNavigateToSaved = { viewModel.navigateTo(AppScreen.SAVED_PROPERTIES) },
                            onNavigateToNotifications = { viewModel.selectTab(BottomNavTab.MESSAGES) },
                            onNavigateToProfile = { viewModel.selectTab(BottomNavTab.PROFILE) }
                        )
                    }

                    AppScreen.SEARCH_EXPLORE -> {
                        SearchScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onNavigateToSaved = { viewModel.navigateTo(AppScreen.SAVED_PROPERTIES) },
                            onNavigateToNotifications = { viewModel.selectTab(BottomNavTab.MESSAGES) },
                            onNavigateToProfile = { viewModel.selectTab(BottomNavTab.PROFILE) }
                        )
                    }

                    AppScreen.PROPERTY_DETAILS -> {
                        PropertyDetailsScreen(
                            property = uiState.selectedProperty,
                            isFavorite = uiState.savedPropertyIds.contains(uiState.selectedProperty.id),
                            onBackClick = { viewModel.navigateBack() },
                            onFavoriteToggle = { viewModel.toggleFavorite(uiState.selectedProperty.id) },
                            onChatClick = { viewModel.openOwnerChat(uiState.selectedProperty) },
                            onBookNowClick = { viewModel.startBooking(uiState.selectedProperty) },
                            onMapClick = { viewModel.openMapScreen() }
                        )
                    }

                    AppScreen.BOOKING_PROCESS -> {
                        BookingProcessScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() },
                            onProceedToPayment = { viewModel.navigateTo(AppScreen.PAYMENT) }
                        )
                    }

                    AppScreen.PAYMENT -> {
                        PaymentScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() },
                            onBookingSuccess = { viewModel.navigateTo(AppScreen.BOOKING_CONFIRMATION) }
                        )
                    }

                    AppScreen.BOOKING_CONFIRMATION -> {
                        BookingConfirmationScreen(
                            booking = uiState.latestBooking,
                            onViewBookingDetails = { viewModel.selectTab(BottomNavTab.BOOKINGS) },
                            onGoHome = { viewModel.selectTab(BottomNavTab.HOME) }
                        )
                    }

                    AppScreen.MY_BOOKINGS -> {
                        MyBookingsScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() },
                            onExploreRooms = { viewModel.selectTab(BottomNavTab.SEARCH) },
                            onChatWithOwner = { booking -> viewModel.openOwnerChat(booking.property) }
                        )
                    }

                    AppScreen.MESSAGES -> {
                        MessagesScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() }
                        )
                    }

                    AppScreen.CHAT_SCREEN -> {
                        ChatScreen(
                            conversation = uiState.activeConversation,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() }
                        )
                    }

                    AppScreen.PROFILE -> {
                        ProfileScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() },
                            onNavigateToBookings = { viewModel.selectTab(BottomNavTab.BOOKINGS) },
                            onNavigateToSaved = { viewModel.navigateTo(AppScreen.SAVED_PROPERTIES) },
                            onLogoutClick = {
                                viewModel.logout()
                            },
                            onSwitchRole = {
                                viewModel.switchMode(!uiState.isOwnerMode)
                            }
                        )
                    }

                    AppScreen.SAVED_PROPERTIES -> {
                        SavedPropertiesScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() },
                            onExploreRooms = { viewModel.selectTab(BottomNavTab.SEARCH) }
                        )
                    }

                    AppScreen.OWNER_HOME -> {
                        OwnerHomeScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onContinueAsOwner = { viewModel.navigateTo(AppScreen.OWNER_DASHBOARD) },
                            onContinueAsStudent = {
                                viewModel.switchMode(isOwner = false)
                                viewModel.navigateTo(AppScreen.STUDENT_HOME)
                            },
                            onNavigateToPostProperty = { viewModel.navigateTo(AppScreen.POST_PROPERTY) }
                        )
                    }

                    AppScreen.OWNER_DASHBOARD -> {
                        OwnerDashboardScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onAddPropertyClick = { viewModel.navigateTo(AppScreen.POST_PROPERTY) }
                        )
                    }

                    AppScreen.POST_PROPERTY -> {
                        PostPropertyWizardScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() }
                        )
                    }

                    AppScreen.EDIT_PROPERTY -> {
                        EditPropertyScreen(
                            property = uiState.propertyToEdit,
                            currentUser = uiState.userProfile,
                            isSaving = uiState.isSavingProperty,
                            errorMessage = uiState.editPropertyError,
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateBack() },
                            onSaveSuccess = {
                                // updateProperty already transitioned currentScreen to OWNER_DASHBOARD
                            }
                        )
                    }
                }
            }
        }
    }
}
