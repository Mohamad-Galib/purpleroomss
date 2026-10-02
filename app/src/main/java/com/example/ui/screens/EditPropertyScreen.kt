package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.model.FoodType
import com.example.model.Property
import com.example.model.PropertyType
import com.example.model.UserProfile
import com.example.ui.components.PurpleRoomsBrand
import com.example.ui.theme.*
import com.example.viewmodel.PurpleRoomsViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditPropertyScreen(
    property: Property?,
    currentUser: UserProfile,
    isSaving: Boolean,
    errorMessage: String?,
    viewModel: PurpleRoomsViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (property == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundLavender)
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = PurplePrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Loading property details...",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
        return
    }

    // Editable form state initialized with existing property data
    var selectedType by remember(property.id) { mutableStateOf(property.propertyType) }
    var title by remember(property.id) { mutableStateOf(property.title) }
    var description by remember(property.id) { mutableStateOf(property.description) }
    var address by remember(property.id) { mutableStateOf(property.location) }
    var locality by remember(property.id) { mutableStateOf(property.locality) }
    var city by remember(property.id) { mutableStateOf(property.city) }
    var stateText by remember(property.id) { mutableStateOf(property.state) }
    var pincode by remember(property.id) { mutableStateOf(property.pincode) }
    var rentText by remember(property.id) { mutableStateOf(property.rent.toString()) }
    var depositText by remember(property.id) { mutableStateOf(property.deposit.toString()) }
    var seatsAvailable by remember(property.id) { mutableStateOf(property.roomsCount.toString()) }
    var availableFrom by remember(property.id) { mutableStateOf(property.availableFrom) }
    var foodType by remember(property.id) { mutableStateOf(property.foodType) }
    var gender by remember(property.id) { mutableStateOf(property.gender) }
    var rules by remember(property.id) { mutableStateOf(property.rules) }
    var selectedAmenities by remember(property.id) { mutableStateOf(property.amenities.toSet()) }
    var photos by remember(property.id) { mutableStateOf(property.images) }

    var validationError by remember { mutableStateOf<String?>(null) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var photoToReplaceIndex by remember { mutableStateOf<Int?>(null) }
    var successToast by remember { mutableStateOf<String?>(null) }

    // Check if any fields were modified
    val hasUnsavedChanges by remember {
        derivedStateOf {
            selectedType != property.propertyType ||
                    title != property.title ||
                    description != property.description ||
                    address != property.location ||
                    locality != property.locality ||
                    city != property.city ||
                    stateText != property.state ||
                    pincode != property.pincode ||
                    rentText != property.rent.toString() ||
                    depositText != property.deposit.toString() ||
                    seatsAvailable != property.roomsCount.toString() ||
                    availableFrom != property.availableFrom ||
                    foodType != property.foodType ||
                    gender != property.gender ||
                    rules != property.rules ||
                    selectedAmenities != property.amenities.toSet() ||
                    photos != property.images
        }
    }

    // Handle back button
    val handleBack: () -> Unit = {
        if (hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onBackClick()
        }
    }

    BackHandler(enabled = true) {
        handleBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    PurpleRoomsBrand()
                },
                navigationIcon = {
                    IconButton(
                        onClick = handleBack,
                        modifier = Modifier.testTag("edit_property_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PurplePrimary
                        )
                    }
                },
                actions = {
                    Text(
                        text = "Edit Property",
                        fontSize = 14.sp,
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
                Column(modifier = Modifier.padding(16.dp)) {
                    if (validationError != null) {
                        Text(
                            text = validationError ?: "",
                            color = AlertRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = AlertRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    if (successToast != null) {
                        Text(
                            text = successToast ?: "",
                            color = VerifiedGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                validationError = "Property name is required."
                                return@Button
                            }

                            val rent = rentText.toIntOrNull()
                            if (rent == null || rent <= 0) {
                                validationError = "Enter a valid monthly rent."
                                return@Button
                            }

                            if (address.isBlank()) {
                                validationError = "Property address is required."
                                return@Button
                            }

                            val deposit = depositText.toIntOrNull() ?: property.deposit
                            val rooms = seatsAvailable.toIntOrNull() ?: property.roomsCount

                            validationError = null

                            val updatedProperty = property.copy(
                                title = title.trim(),
                                propertyType = selectedType,
                                location = address.trim(),
                                locality = locality.trim().ifEmpty { "Alkapuri" },
                                city = city.trim().ifEmpty { "Vadodara" },
                                state = stateText.trim().ifEmpty { "Gujarat" },
                                pincode = pincode.trim().ifEmpty { "390007" },
                                rent = rent,
                                deposit = deposit,
                                roomsCount = rooms,
                                availableFrom = availableFrom,
                                foodType = foodType,
                                gender = gender,
                                description = description.trim(),
                                rules = rules.trim(),
                                amenities = selectedAmenities.toList(),
                                images = photos.ifEmpty { property.images }
                            )

                            viewModel.updateProperty(
                                propertyId = property.id,
                                updatedProperty = updatedProperty,
                                onSuccess = {
                                    successToast = "Property updated successfully ✓"
                                    onSaveSuccess()
                                },
                                onError = { errorMsg ->
                                    validationError = errorMsg
                                }
                            )
                        },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_changes_btn")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Saving changes...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save Changes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
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
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            // Screen Header Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Edit Property",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .background(PurpleSubtle, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ID: ${property.id}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PurplePrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Modify pricing, availability, photos, and amenities for your listing.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // 1. Property Type
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

            // 2. Property Name & Description
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Basic Information", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Property Name / Title *") },
                            placeholder = { Text("e.g. Premium PG for Boys") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_title_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            minLines = 3,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 3. Location, Address, Locality, City, State, Pincode
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Address & Location", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address / Full Location *") },
                            placeholder = { Text("e.g. Alkapuri, Vadodara, Gujarat") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_address_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = locality,
                                onValueChange = { locality = it },
                                label = { Text("Locality") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = stateText,
                                onValueChange = { stateText = it },
                                label = { Text("State") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = pincode,
                                onValueChange = { pincode = it },
                                label = { Text("Pincode") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. Pricing & Deposit
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rent & Deposit", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = rentText,
                                onValueChange = { rentText = it },
                                label = { Text("Monthly Rent (₹) *") },
                                leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = PurplePrimary) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("edit_rent_input")
                            )

                            OutlinedTextField(
                                value = depositText,
                                onValueChange = { depositText = it },
                                label = { Text("Security Deposit (₹)") },
                                leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = PurplePrimary) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 5. Rooms, Availability & Gender
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Availability & Occupancy", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))

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
                                modifier = Modifier.weight(1.3f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Gender Preference", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("For Boys", "For Girls", "Any").forEach { gOption ->
                                val isSel = gender == gOption
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
                                        .clickable { gender = gOption }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = gOption,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) PurplePrimary else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Food Availability
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

            // 7. Amenities Checklist
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Amenities", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))

                        val allAmenities = listOf(
                            "Wi-Fi", "AC", "Food", "Laundry",
                            "Parking", "24x7 Security", "Furnished",
                            "Study Room", "Attached Washroom", "Garden"
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allAmenities.forEach { amenity ->
                                val isSelected = selectedAmenities.contains(amenity)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PurplePrimary else Color(0xFFF8F6FD))
                                        .border(1.dp, if (isSelected) PurplePrimary else PurpleBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedAmenities = if (isSelected) {
                                                selectedAmenities - amenity
                                            } else {
                                                selectedAmenities + amenity
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = amenity,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 8. Property Photos (Add, Remove, Replace)
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Property Photos", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("${photos.size} photos loaded", fontSize = 11.sp, color = TextSecondary)
                            }

                            Button(
                                onClick = { showAddPhotoDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PurpleSubtle, contentColor = PurplePrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp).testTag("add_photo_btn")
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Add Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(photos) { index, photoRes ->
                                Card(
                                    modifier = Modifier
                                        .width(130.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(95.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = photoRes),
                                                contentDescription = "Photo $index",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )

                                            // Remove Photo Button
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(AlertRed)
                                                    .clickable {
                                                        if (photos.size > 1) {
                                                            photos = photos.filterIndexed { i, _ -> i != index }
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove photo",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }

                                            // Cover badge on first image
                                            if (index == 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomStart)
                                                        .padding(4.dp)
                                                        .background(VerifiedGreen, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Cover Photo", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }

                                        // Photo Controls Row: Replace & Reorder
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFFBF9FF))
                                                .padding(horizontal = 4.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Replace button
                                            Text(
                                                text = "Replace",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PurplePrimary,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .clickable {
                                                        photoToReplaceIndex = index
                                                    }
                                                    .padding(2.dp)
                                            )

                                            // Reorder buttons: Move Left / Move Right
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                if (index > 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(PurpleSubtle)
                                                            .clickable {
                                                                val list = photos.toMutableList()
                                                                val temp = list[index]
                                                                list[index] = list[index - 1]
                                                                list[index - 1] = temp
                                                                photos = list
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("◀", fontSize = 8.sp, color = PurplePrimary)
                                                    }
                                                }
                                                if (index < photos.size - 1) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(PurpleSubtle)
                                                            .clickable {
                                                                val list = photos.toMutableList()
                                                                val temp = list[index]
                                                                list[index] = list[index + 1]
                                                                list[index + 1] = temp
                                                                photos = list
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("▶", fontSize = 8.sp, color = PurplePrimary)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 9. Property Rules
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Property Rules", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = rules,
                            onValueChange = { rules = it },
                            placeholder = { Text("e.g. No smoking inside rooms. Gate closes at 10:30 PM.") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Unsaved Changes Confirmation Dialog
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = {
                Text(
                    text = "Unsaved Changes",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "You have unsaved changes. Do you want to leave?",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnsavedDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("Discard Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text("Stay", color = PurplePrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Add or Replace Photo Selection Dialog
    val isReplacingPhoto = photoToReplaceIndex != null
    if (showAddPhotoDialog || isReplacingPhoto) {
        val samplePhotoOptions = listOf(
            Pair("Cozy Bedroom", R.drawable.hero_bedroom_splash),
            Pair("Living Area", R.drawable.prop_apartment_living),
            Pair("Hostel Bunk", R.drawable.prop_hostel_bunk),
            Pair("Villa Exterior", R.drawable.prop_villa_exterior),
            Pair("Campus & Garden", R.drawable.hero_owner_list)
        )

        AlertDialog(
            onDismissRequest = {
                showAddPhotoDialog = false
                photoToReplaceIndex = null
            },
            title = {
                Text(
                    text = if (isReplacingPhoto) "Replace Photo #${(photoToReplaceIndex ?: 0) + 1}" else "Add Photo to Property",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isReplacingPhoto) "Select a new image to replace this photo:" else "Choose a photo to add to this listing:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    samplePhotoOptions.forEach { (label, res) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val replaceIdx = photoToReplaceIndex
                                    if (replaceIdx != null && replaceIdx in photos.indices) {
                                        val list = photos.toMutableList()
                                        list[replaceIdx] = res
                                        photos = list
                                        photoToReplaceIndex = null
                                    } else {
                                        photos = photos + res
                                        showAddPhotoDialog = false
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = res),
                                    contentDescription = label,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showAddPhotoDialog = false
                    photoToReplaceIndex = null
                }) {
                    Text("Cancel", color = PurplePrimary)
                }
            }
        )
    }
}
