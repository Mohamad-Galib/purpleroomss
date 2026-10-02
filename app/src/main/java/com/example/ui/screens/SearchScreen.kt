package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PropertyType
import com.example.model.calculateDistanceKm
import com.example.model.formatDistance
import com.example.ui.components.PrimaryPurpleButton
import com.example.ui.components.PropertyCard
import com.example.ui.components.PurpleRoomsTopBar
import com.example.ui.theme.*
import com.example.viewmodel.PurpleRoomsViewModel
import com.example.viewmodel.UiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    uiState: UiState,
    viewModel: PurpleRoomsViewModel,
    onNavigateToSaved: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResultsOnly by remember { mutableStateOf(false) }

    val filteredResults = remember(
        uiState.properties,
        uiState.selectedCategory,
        uiState.searchQuery,
        uiState.minBudget,
        uiState.maxBudget,
        uiState.selectedAmenities,
        uiState.selectedLocationData
    ) {
        uiState.properties.filter { prop ->
            val matchCat = uiState.selectedCategory == PropertyType.ALL || prop.propertyType == uiState.selectedCategory
            val matchQuery = uiState.searchQuery.isBlank() ||
                    prop.title.contains(uiState.searchQuery, ignoreCase = true) ||
                    prop.location.contains(uiState.searchQuery, ignoreCase = true)
            val matchBudget = prop.rent in (uiState.minBudget.toInt()..uiState.maxBudget.toInt())
            val matchAmenities = uiState.selectedAmenities.isEmpty() ||
                    uiState.selectedAmenities.all { sel -> prop.amenities.any { it.contains(sel, ignoreCase = true) } }

            matchCat && matchQuery && matchBudget && matchAmenities
        }.sortedBy { prop ->
            calculateDistanceKm(
                uiState.selectedLocationData.latitude,
                uiState.selectedLocationData.longitude,
                prop.latitude,
                prop.longitude
            )
        }
    }

    Scaffold(
        topBar = {
            PurpleRoomsTopBar(
                selectedLocation = uiState.selectedLocation,
                onLocationClick = { viewModel.openChooseLocationScreen() },
                onSavedClick = onNavigateToSaved,
                onNotificationsClick = onNavigateToNotifications,
                onProfileClick = onNavigateToProfile,
                savedCount = uiState.savedPropertyIds.size,
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
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Title Header
            item {
                Column {
                    Text(
                        text = "Search Your Perfect Space",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Rooms, PGs, Hostels, Apartments & more near you",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            // Search text field
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = {
                        viewModel.updateSearchQuery(it)
                        if (it.isNotEmpty()) showResultsOnly = true
                    },
                    placeholder = {
                        Text("Search for Rooms, PG, Hostel...", fontSize = 14.sp, color = TextMuted)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = PurplePrimary
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = PurpleBorder
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_screen_input")
                )
            }

            // Category Selection Cards
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "What are you looking for?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "View All >",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PurplePrimary,
                            modifier = Modifier.clickable { viewModel.selectCategory(PropertyType.ALL) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SearchCategoryCard(
                            title = "Room",
                            icon = Icons.Default.Bed,
                            isSelected = uiState.selectedCategory == PropertyType.ROOM,
                            onClick = { viewModel.selectCategory(PropertyType.ROOM) },
                            modifier = Modifier.weight(1f)
                        )
                        SearchCategoryCard(
                            title = "PG",
                            icon = Icons.Default.Home,
                            isSelected = uiState.selectedCategory == PropertyType.PG,
                            onClick = { viewModel.selectCategory(PropertyType.PG) },
                            modifier = Modifier.weight(1f)
                        )
                        SearchCategoryCard(
                            title = "Hostel",
                            icon = Icons.Default.HomeWork,
                            isSelected = uiState.selectedCategory == PropertyType.HOSTEL,
                            onClick = { viewModel.selectCategory(PropertyType.HOSTEL) },
                            modifier = Modifier.weight(1f)
                        )
                        SearchCategoryCard(
                            title = "Apartment",
                            icon = Icons.Default.Domain,
                            isSelected = uiState.selectedCategory == PropertyType.APARTMENT,
                            onClick = { viewModel.selectCategory(PropertyType.APARTMENT) },
                            modifier = Modifier.weight(1f)
                        )
                        SearchCategoryCard(
                            title = "Villa",
                            icon = Icons.Default.Cottage,
                            isSelected = uiState.selectedCategory == PropertyType.VILLA,
                            onClick = { viewModel.selectCategory(PropertyType.VILLA) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Budget Range Slider
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
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
                                text = "Budget Range (Per Month)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "₹${"%,d".format(uiState.minBudget.toInt())} – ₹${"%,d".format(uiState.maxBudget.toInt())}+",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary
                            )
                        }

                        RangeSlider(
                            value = uiState.minBudget..uiState.maxBudget,
                            onValueChange = { range ->
                                viewModel.setBudgetRange(range.start, range.endInclusive)
                            },
                            valueRange = 2000f..20000f,
                            colors = SliderDefaults.colors(
                                thumbColor = PurplePrimary,
                                activeTrackColor = PurplePrimary,
                                inactiveTrackColor = PurpleSubtle
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "₹2,000", fontSize = 12.sp, color = TextSecondary)
                            Text(text = "₹20,000+", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // Filters section with clear
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filters",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Clear All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PurplePrimary,
                            modifier = Modifier.clickable { viewModel.clearFilters() }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val amenityList = listOf(
                        "Wi-Fi", "AC", "Food", "Attached Washroom",
                        "Furnished", "Near University", "For Boys",
                        "For Girls", "Parking", "Security", "Pets Allowed"
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        amenityList.forEach { filterName ->
                            val isSelected = uiState.selectedAmenities.contains(filterName)
                            FilterChipItem(
                                name = filterName,
                                isSelected = isSelected,
                                onToggle = { viewModel.toggleAmenityFilter(filterName) }
                            )
                        }
                    }
                }
            }

            // Search CTA Button
            item {
                PrimaryPurpleButton(
                    text = "Search Properties (${filteredResults.size})",
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { showResultsOnly = true },
                    modifier = Modifier.testTag("search_properties_btn")
                )
            }

            // Results count Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${filteredResults.size} Properties Found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Near ${uiState.selectedLocation}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterButton(
                            icon = Icons.Default.Map,
                            text = "Map",
                            onClick = { viewModel.openMapScreen() }
                        )
                        FilterButton(icon = Icons.Default.SwapVert, text = "Sort")
                        FilterButton(icon = Icons.Default.FilterList, text = "Filter")
                    }
                }
            }

            // Results List
            items(filteredResults, key = { it.id }) { property ->
                val dist = calculateDistanceKm(
                    uiState.selectedLocationData.latitude,
                    uiState.selectedLocationData.longitude,
                    property.latitude,
                    property.longitude
                )
                PropertyCard(
                    property = property,
                    isFavorite = uiState.savedPropertyIds.contains(property.id),
                    onPropertyClick = { viewModel.selectProperty(property) },
                    onFavoriteToggle = { viewModel.toggleFavorite(property.id) },
                    distanceText = formatDistance(dist)
                )
            }
        }
    }
}

@Composable
private fun SearchCategoryCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) PurpleSubtle else Color.White)
            .border(1.dp, if (isSelected) PurplePrimary else PurpleBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = PurplePrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PurplePrimary else TextPrimary,
            maxLines = 1
        )
    }
}

@Composable
private fun FilterChipItem(
    name: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) PurplePrimary else Color.White)
            .border(1.dp, if (isSelected) PurplePrimary else PurpleBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else TextPrimary
        )
    }
}

@Composable
private fun FilterButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, PurpleBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}
