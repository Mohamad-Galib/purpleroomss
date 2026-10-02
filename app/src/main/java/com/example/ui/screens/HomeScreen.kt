package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.model.PropertyType
import com.example.model.calculateDistanceKm
import com.example.model.formatDistance
import com.example.ui.components.PropertyCard
import com.example.ui.components.PurpleRoomsTopBar
import com.example.ui.theme.*
import com.example.viewmodel.PurpleRoomsViewModel
import com.example.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: UiState,
    viewModel: PurpleRoomsViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }

    // Filter and sort properties based on selected category, search query, and proximity to selected location
    val filteredProperties = remember(
        uiState.properties,
        uiState.selectedCategory,
        uiState.searchQuery,
        uiState.selectedLocationData
    ) {
        uiState.properties.filter { prop ->
            val matchCat = uiState.selectedCategory == PropertyType.ALL || prop.propertyType == uiState.selectedCategory
            val matchQuery = uiState.searchQuery.isBlank() ||
                    prop.title.contains(uiState.searchQuery, ignoreCase = true) ||
                    prop.location.contains(uiState.searchQuery, ignoreCase = true) ||
                    prop.propertyType.displayName.contains(uiState.searchQuery, ignoreCase = true)
            matchCat && matchQuery
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
            // Search Bar & Filter Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = {
                            Text(
                                "Search rooms, PG, hostel, apartment...",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
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
                            .weight(1f)
                            .testTag("home_search_input")
                    )

                    // Filter action button
                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, PurpleBorder, RoundedCornerShape(12.dp))
                            .testTag("home_filter_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filters",
                            tint = PurplePrimary
                        )
                    }
                }
            }

            // Category Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val categories = listOf(
                        Triple(PropertyType.ALL, "All", Icons.Default.GridView),
                        Triple(PropertyType.ROOM, "Room", Icons.Default.Bed),
                        Triple(PropertyType.PG, "PG", Icons.Default.Apartment),
                        Triple(PropertyType.HOSTEL, "Hostel", Icons.Default.HomeWork),
                        Triple(PropertyType.APARTMENT, "Apartment", Icons.Default.Domain),
                        Triple(PropertyType.VILLA, "Villa", Icons.Default.Cottage)
                    )

                    categories.forEach { (type, name, icon) ->
                        val isSelected = uiState.selectedCategory == type
                        CategoryPill(
                            name = name,
                            icon = icon,
                            isSelected = isSelected,
                            onClick = { viewModel.selectCategory(type) }
                        )
                    }
                }
            }

            // Filter Dropdowns Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickFilterChip(
                        title = "Rent Range",
                        isSelected = uiState.maxBudget < 15000f || uiState.minBudget > 3000f,
                        onClick = onNavigateToSearch
                    )
                    QuickFilterChip(
                        title = "Food Type",
                        isSelected = uiState.selectedFoodType != null,
                        onClick = onNavigateToSearch
                    )
                    QuickFilterChip(
                        title = "Amenities",
                        isSelected = uiState.selectedAmenities.isNotEmpty(),
                        onClick = onNavigateToSearch
                    )
                    QuickFilterChip(
                        title = "Gender",
                        isSelected = uiState.selectedGender != null,
                        onClick = onNavigateToSearch
                    )
                    QuickFilterChip(
                        title = "Sort",
                        isSelected = false,
                        onClick = onNavigateToSearch
                    )
                }
            }

            // Promotional Featured Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .clickable(onClick = onNavigateToSearch),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PurpleDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Find Your\nPerfect Room",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Safe. Verified. No Brokerage.",
                                fontSize = 12.sp,
                                color = PurpleSubtle
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(85.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.hero_bedroom_splash),
                                contentDescription = "Banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Explore on Map Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.openMapScreen() }
                        .testTag("home_explore_map_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(PurpleSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Map View",
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Explore on Map",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "View prices & nearby rooms in ${uiState.selectedLocation}",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Map",
                            tint = PurplePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Featured Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Properties near ${uiState.selectedLocation.split(",").firstOrNull() ?: uiState.selectedLocation}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Sorted by proximity to your selected location",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "View All",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PurplePrimary,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToSearch)
                            .testTag("view_all_properties")
                    )
                }
            }

            // Property Items
            if (filteredProperties.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No properties",
                            tint = PurplePrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No properties found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Try adjusting your category or search keywords",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.clearFilters() },
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Text("Reset Filters")
                        }
                    }
                }
            } else {
                items(filteredProperties, key = { it.id }) { property ->
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

    // Quick Filter Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Filters",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    TextButton(onClick = { viewModel.clearFilters() }) {
                        Text("Clear All", color = PurplePrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Property Type",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PropertyType.values().take(4).forEach { pt ->
                        val sel = uiState.selectedCategory == pt
                        FilterChip(
                            selected = sel,
                            onClick = { viewModel.selectCategory(pt) },
                            label = { Text(pt.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showFilterSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Filters")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CategoryPill(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) PurplePrimary else Color.White)
            .border(1.dp, if (isSelected) PurplePrimary else PurpleBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = name,
            tint = if (isSelected) Color.White else PurplePrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextPrimary
        )
    }
}

@Composable
fun QuickFilterChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) PurpleSubtle else Color.White)
            .border(1.dp, if (isSelected) PurplePrimary else Color(0xFFE5E0F0), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) PurplePrimary else TextSecondary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = if (isSelected) PurplePrimary else TextSecondary,
            modifier = Modifier.size(14.dp)
        )
    }
}
