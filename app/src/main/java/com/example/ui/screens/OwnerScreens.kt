package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.FoodType
import com.example.model.Property
import com.example.model.PropertyStatus
import com.example.model.PropertyType
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.PurpleRoomsViewModel
import com.example.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(
    uiState: UiState,
    viewModel: PurpleRoomsViewModel,
    onContinueAsOwner: () -> Unit,
    onContinueAsStudent: () -> Unit,
    onNavigateToPostProperty: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            PurpleRoomsTopBar(
                selectedLocation = uiState.selectedLocation,
                onLocationClick = {},
                onSavedClick = {},
                onNotificationsClick = {},
                onProfileClick = {},
                savedCount = 0,
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = BackgroundLavender,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
        ) {
            // Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.hero_owner_list),
                                contentDescription = "List Your Property",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Start Earning badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(12.dp))
                                    .border(1.dp, PurpleBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = PurplePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "Start Earning",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PurplePrimary
                                        )
                                        Text(
                                            text = "With Purple Rooms",
                                            fontSize = 9.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "List Your Property\n& Find Genuine Tenants",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                lineHeight = 28.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No Brokerage. Direct Tenants. Safe & Verified Platform.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Benefits List
                            OwnerBenefitRow(
                                icon = Icons.Default.CurrencyRupee,
                                title = "No Brokerage",
                                subtitle = "Keep 100% of your rental income"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OwnerBenefitRow(
                                icon = Icons.Default.VerifiedUser,
                                title = "Verified Tenants",
                                subtitle = "100% safe & verified students"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OwnerBenefitRow(
                                icon = Icons.Default.Chat,
                                title = "Direct Communication",
                                subtitle = "Chat directly with tenants"
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OwnerBenefitRow(
                                icon = Icons.Default.Assignment,
                                title = "Easy Property Management",
                                subtitle = "List, manage and track bookings"
                            )
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PrimaryPurpleButton(
                        text = "Continue as Owner",
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                        onClick = onContinueAsOwner,
                        modifier = Modifier.testTag("owner_continue_btn")
                    )

                    SecondaryOutlinedButton(
                        text = "Continue as Student",
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                        onClick = onContinueAsStudent,
                        modifier = Modifier.testTag("student_continue_btn")
                    )
                }
            }

            // Why List on Purple Rooms Grid
            item {
                Text(
                    text = "Why List on Purple Rooms?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WhyListCard(
                        icon = Icons.Default.CurrencyRupee,
                        title = "0%\nBrokerage",
                        subtitle = "No hidden fees, no middlemen",
                        modifier = Modifier.weight(1f)
                    )
                    WhyListCard(
                        icon = Icons.Default.Shield,
                        title = "Verified\nTenants",
                        subtitle = "Safe & genuine students",
                        modifier = Modifier.weight(1f)
                    )
                    WhyListCard(
                        icon = Icons.Default.Schedule,
                        title = "Faster\nBookings",
                        subtitle = "Get tenants quickly",
                        modifier = Modifier.weight(1f)
                    )
                    WhyListCard(
                        icon = Icons.Default.BarChart,
                        title = "Full\nControl",
                        subtitle = "Manage easily",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // List These Properties Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "List These Properties",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "View All >",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PurplePrimary,
                        modifier = Modifier.clickable { onNavigateToPostProperty() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val propertyTypes = listOf(
                        Pair("Room", R.drawable.hero_bedroom_splash),
                        Pair("PG", R.drawable.hero_bedroom_splash),
                        Pair("Hostel", R.drawable.prop_hostel_bunk),
                        Pair("Apartment", R.drawable.prop_apartment_living),
                        Pair("Villa", R.drawable.prop_villa_exterior)
                    )

                    items(propertyTypes) { (type, imgRes) ->
                        Column(
                            modifier = Modifier
                                .width(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(1.dp, PurpleBorder, RoundedCornerShape(12.dp))
                                .clickable { onNavigateToPostProperty() }
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = imgRes),
                                    contentDescription = type,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = type,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnerBenefitRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PurpleSubtle),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun WhyListCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, PurpleBorder, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(PurpleSubtle),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 13.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = subtitle,
            fontSize = 9.sp,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}

// -------------------------------------------------------------
// OWNER DASHBOARD ("My Properties") SCREEN 10B
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    uiState: UiState,
    viewModel: PurpleRoomsViewModel,
    onAddPropertyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ownerProps = uiState.ownerProperties
    val activeProps = ownerProps.filter { it.status == PropertyStatus.ACTIVE }
    val inactiveProps = ownerProps.filter { it.status == PropertyStatus.INACTIVE }
    val draftProps = ownerProps.filter { it.status == PropertyStatus.DRAFT }

    val displayedProps = when (uiState.ownerPropertiesTab) {
        "Active" -> activeProps
        "Inactive" -> inactiveProps
        "Draft" -> draftProps
        else -> ownerProps
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    PurpleRoomsBrand()
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = PurplePrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = PurplePrimary)
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PurpleSubtle)
                            .border(1.dp, PurplePrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("MG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = BackgroundLavender,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
        ) {
            // Header with Add Property button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My Properties",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Manage your listings, view bookings and track performance.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = onAddPropertyClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_property_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Property", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tabs Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OwnerTabPill(
                        title = "All (${ownerProps.size})",
                        isSelected = uiState.ownerPropertiesTab == "All",
                        onClick = { viewModel.setOwnerPropertiesTab("All") },
                        modifier = Modifier.weight(1f)
                    )
                    OwnerTabPill(
                        title = "Active (${activeProps.size})",
                        isSelected = uiState.ownerPropertiesTab == "Active",
                        onClick = { viewModel.setOwnerPropertiesTab("Active") },
                        modifier = Modifier.weight(1f)
                    )
                    OwnerTabPill(
                        title = "Inactive (${inactiveProps.size})",
                        isSelected = uiState.ownerPropertiesTab == "Inactive",
                        onClick = { viewModel.setOwnerPropertiesTab("Inactive") },
                        modifier = Modifier.weight(1f)
                    )
                    OwnerTabPill(
                        title = "Draft (${draftProps.size})",
                        isSelected = uiState.ownerPropertiesTab == "Draft",
                        onClick = { viewModel.setOwnerPropertiesTab("Draft") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (uiState.editPropertySuccessMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = VerifiedGreenBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VerifiedGreenBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(uiState.editPropertySuccessMessage, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VerifiedGreen)
                            }
                            IconButton(onClick = { viewModel.clearEditFeedback() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = VerifiedGreen, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // Properties list
            items(displayedProps, key = { it.id }) { prop ->
                OwnerPropertyCard(
                    property = prop,
                    onEditClick = { viewModel.openEditProperty(prop.id) }
                )
            }
        }
    }

    if (uiState.editPropertyError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearEditFeedback() },
            title = { Text("Permission Denied", fontWeight = FontWeight.Bold, color = AlertRed) },
            text = { Text(uiState.editPropertyError ?: "", fontSize = 13.sp, color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearEditFeedback() },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun OwnerTabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) PurplePrimary else Color.White)
            .border(1.dp, if (isSelected) PurplePrimary else PurpleBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextPrimary
        )
    }
}

@Composable
private fun OwnerPropertyCard(
    property: Property,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Thumbnail
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = property.images.firstOrNull() ?: R.drawable.hero_bedroom_splash),
                        contentDescription = property.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Photo count
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "📷 ${property.images.size.coerceAtLeast(6)}",
                            fontSize = 9.sp,
                            color = Color.White
                        )
                    }

                    // Status pill
                    val isActive = property.status == PropertyStatus.ACTIVE
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .background(if (isActive) VerifiedGreenBg else WarningOrangeBg, RoundedCornerShape(10.dp))
                            .border(1.dp, if (isActive) VerifiedGreenBorder else Color(0xFFFED7AA), RoundedCornerShape(10.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isActive) "Active" else "Inactive",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) VerifiedGreen else WarningOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = property.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "₹${"%,d".format(property.rent)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PurplePrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "📍 ${property.location}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = "/ month", fontSize = 10.sp, color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "🛏️ ${property.roomsCount} Rooms",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                        Text(text = "•", fontSize = 10.sp, color = TextMuted)
                        Text(
                            text = property.occupancy,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF3F0FA))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${property.viewsCount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Views", fontSize = 10.sp, color = TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${property.inquiriesCount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Inquiries", fontSize = 10.sp, color = TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${property.bookingsCount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Bookings", fontSize = 10.sp, color = TextSecondary)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("edit_property_btn_${property.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 11.sp, color = PurplePrimary)
                    }

                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = TextSecondary)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Property") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = PurplePrimary) },
                                onClick = {
                                    showMenu = false
                                    onEditClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// POST PROPERTY WIZARD (SCREEN 10C)
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostPropertyWizardScreen(
    uiState: UiState,
    viewModel: PurpleRoomsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf(PropertyType.ROOM) }
    var title by remember { mutableStateOf("Premium PG for Boys near MS University") }
    var description by remember { mutableStateOf("Fully furnished PG for boys with all modern amenities. Clean rooms, home-cooked food, high-speed Wi-Fi, 24x7 security and peaceful environment.") }
    var rentText by remember { mutableStateOf("6500") }
    var depositText by remember { mutableStateOf("1000") }
    var address by remember { mutableStateOf("Alkapuri, Vadodara") }
    var availableFrom by remember { mutableStateOf("15 Oct 2026") }
    var foodType by remember { mutableStateOf(FoodType.BOTH) }
    var seatsAvailable by remember { mutableStateOf("3") }

    var validationError by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    PurpleRoomsBrand()
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PurplePrimary)
                    }
                },
                actions = {
                    Text(
                        text = "List Property",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    PrimaryPurpleButton(
                        text = "Publish Property",
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                        onClick = {
                            if (title.isBlank()) {
                                validationError = "Please enter a property title"
                                return@PrimaryPurpleButton
                            }
                            val rent = rentText.toIntOrNull()
                            if (rent == null || rent <= 0) {
                                validationError = "Please enter a valid monthly rent"
                                return@PrimaryPurpleButton
                            }
                            val deposit = depositText.toIntOrNull() ?: 0

                            viewModel.publishNewProperty(
                                title = title,
                                type = selectedType,
                                rent = rent,
                                deposit = deposit,
                                description = description,
                                address = address,
                                availableFrom = availableFrom,
                                foodType = foodType
                            )
                            showSuccessDialog = true
                        },
                        modifier = Modifier.testTag("publish_property_btn")
                    )
                }
            }
        },
        containerColor = BackgroundLavender,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
        ) {
            // Steps indicator
            item {
                StepIndicator(
                    currentStep = 1,
                    totalSteps = 5,
                    stepTitles = listOf("Basic Info", "Location", "Amenities", "Photos", "Preview")
                )
            }

            // Headline
            item {
                Column {
                    Text(
                        text = "Basic Property Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Add basic details about your room, PG, apartment or hostel.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            // Property Type Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Property Type", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val types = listOf(
                                Pair(PropertyType.ROOM, "Room"),
                                Pair(PropertyType.PG, "PG"),
                                Pair(PropertyType.HOSTEL, "Hostel"),
                                Pair(PropertyType.APARTMENT, "Apartment"),
                                Pair(PropertyType.VILLA, "Villa")
                            )

                            types.forEach { (type, label) ->
                                val isSel = selectedType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) PurpleSubtle else Color(0xFFF9F8FD))
                                        .border(
                                            width = if (isSel) 1.5.dp else 1.dp,
                                            color = if (isSel) PurplePrimary else PurpleBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedType = type }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) PurplePrimary else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Title & Description
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Property Title") },
                            placeholder = { Text("e.g. Premium PG for Boys near MS University") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            minLines = 3,
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Pricing & Availability
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = rentText,
                                onValueChange = { rentText = it },
                                label = { Text("Monthly Rent (₹)") },
                                leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = PurplePrimary) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = depositText,
                                onValueChange = { depositText = it },
                                label = { Text("Security Deposit") },
                                leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = PurplePrimary) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = seatsAvailable,
                                onValueChange = { seatsAvailable = it },
                                label = { Text("Rooms Available") },
                                leadingIcon = { Icon(Icons.Default.Bed, contentDescription = null, tint = PurplePrimary) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = availableFrom,
                                onValueChange = { availableFrom = it },
                                label = { Text("Available From") },
                                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PurplePrimary) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Food Availability
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Food Availability", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val foods = listOf(
                                Pair(FoodType.VEG_ONLY, "Veg Only"),
                                Pair(FoodType.NON_VEG_ONLY, "Non-Veg"),
                                Pair(FoodType.BOTH, "Both (Veg/Non)"),
                                Pair(FoodType.NO_FOOD, "No Food")
                            )

                            foods.forEach { (type, label) ->
                                val isSel = foodType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) PurpleSubtle else Color(0xFFF9F8FD))
                                        .border(
                                            width = if (isSel) 1.5.dp else 1.dp,
                                            color = if (isSel) PurplePrimary else PurpleBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { foodType = type }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) PurplePrimary else TextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (validationError != null) {
                item {
                    Text(
                        text = validationError ?: "",
                        color = AlertRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.navigateTo(com.example.viewmodel.AppScreen.OWNER_DASHBOARD)
            },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(48.dp))
            },
            title = {
                Text("Property Listed Successfully!", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Text(
                    "Your property '$title' has been published with 100% verified status and zero brokerage. Tenants can now find and request bookings immediately!",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.navigateTo(com.example.viewmodel.AppScreen.OWNER_DASHBOARD)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Go to My Properties")
                }
            }
        )
    }
}
