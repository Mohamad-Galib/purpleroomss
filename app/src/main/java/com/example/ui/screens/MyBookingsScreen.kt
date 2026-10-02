package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Booking
import com.example.model.BookingStatus
import com.example.ui.theme.*
import com.example.viewmodel.PurpleRoomsViewModel
import com.example.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    uiState: UiState,
    viewModel: PurpleRoomsViewModel,
    onBackClick: () -> Unit,
    onExploreRooms: () -> Unit,
    onChatWithOwner: (Booking) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBookingForDetail by remember { mutableStateOf<Booking?>(null) }

    val allBookings = uiState.bookings
    val upcomingBookings = allBookings.filter { it.bookingStatus == BookingStatus.CONFIRMED || it.bookingStatus == BookingStatus.PENDING }
    val pastBookings = allBookings.filter { it.bookingStatus == BookingStatus.CANCELLED }

    val displayedBookings = when (uiState.bookingsTab) {
        "Upcoming" -> upcomingBookings
        "Past" -> pastBookings
        else -> allBookings
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Bookings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("bookings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PurplePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = BackgroundLavender,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Tabs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BookingTabPill(
                    title = "All (${allBookings.size})",
                    isSelected = uiState.bookingsTab == "All",
                    onClick = { viewModel.setBookingsTab("All") },
                    modifier = Modifier.weight(1f)
                )
                BookingTabPill(
                    title = "Upcoming (${upcomingBookings.size})",
                    isSelected = uiState.bookingsTab == "Upcoming",
                    onClick = { viewModel.setBookingsTab("Upcoming") },
                    modifier = Modifier.weight(1f)
                )
                BookingTabPill(
                    title = "Past (${pastBookings.size})",
                    isSelected = uiState.bookingsTab == "Past",
                    onClick = { viewModel.setBookingsTab("Past") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (displayedBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No bookings found in this tab",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Explore rooms, PGs, and hostels with zero brokerage",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onExploreRooms,
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Text("Find Rooms")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(displayedBookings, key = { it.id }) { booking ->
                        BookingItemCard(
                            booking = booking,
                            onClick = { selectedBookingForDetail = booking }
                        )
                    }
                }
            }
        }
    }

    // Detail modal
    if (selectedBookingForDetail != null) {
        val b = selectedBookingForDetail!!
        AlertDialog(
            onDismissRequest = { selectedBookingForDetail = null },
            title = {
                Text(text = b.property.title, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column {
                    Text(text = "Booking ID: ${b.id}", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "Move-in: ${b.moveInDate}", fontSize = 13.sp, color = TextPrimary)
                    Text(text = "Duration: ${b.duration}", fontSize = 13.sp, color = TextPrimary)
                    Text(text = "Payment: ${b.paymentMethod} (${b.paymentStatus})", fontSize = 13.sp, color = TextPrimary)
                    Text(text = "Monthly Rent: ₹${"%,d".format(b.rentAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
                    if (b.additionalRequests.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Requests: ${b.additionalRequests}", fontSize = 12.sp, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Status: ${b.bookingStatus.label}",
                        fontWeight = FontWeight.Bold,
                        color = when (b.bookingStatus) {
                            BookingStatus.CONFIRMED -> VerifiedGreen
                            BookingStatus.PENDING -> WarningOrange
                            BookingStatus.CANCELLED -> AlertRed
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val prop = b.property
                        selectedBookingForDetail = null
                        onChatWithOwner(b)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Chat with Owner")
                }
            },
            dismissButton = {
                if (b.bookingStatus != BookingStatus.CANCELLED) {
                    TextButton(
                        onClick = {
                            viewModel.cancelBooking(b.id)
                            selectedBookingForDetail = null
                        }
                    ) {
                        Text("Cancel Booking", color = AlertRed)
                    }
                } else {
                    TextButton(onClick = { selectedBookingForDetail = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }
}

@Composable
private fun BookingTabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) PurplePrimary else Color.White)
            .border(1.dp, if (isSelected) PurplePrimary else PurpleBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextPrimary
        )
    }
}

@Composable
private fun BookingItemCard(
    booking: Booking,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, PurpleBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Image(
                    painter = painterResource(id = booking.property.images.firstOrNull() ?: R.drawable.hero_bedroom_splash),
                    contentDescription = booking.property.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = booking.property.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Status Pill
                    val (bgColor, textColor, borderCol) = when (booking.bookingStatus) {
                        BookingStatus.CONFIRMED -> Triple(VerifiedGreenBg, VerifiedGreen, VerifiedGreenBorder)
                        BookingStatus.PENDING -> Triple(WarningOrangeBg, WarningOrange, Color(0xFFFED7AA))
                        BookingStatus.CANCELLED -> Triple(AlertRedBg, AlertRed, Color(0xFFFECACA))
                    }

                    Box(
                        modifier = Modifier
                            .background(bgColor, RoundedCornerShape(12.dp))
                            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = booking.bookingStatus.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = booking.property.location,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 Move-in: ${booking.moveInDate}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Text(
                        text = "₹${"%,d".format(booking.rentAmount)} / mo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PurplePrimary
                    )
                }
            }
        }
    }
}
