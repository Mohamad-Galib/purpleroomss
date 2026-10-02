package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Property
import com.example.model.calculateDistanceKm
import com.example.model.formatDistance
import com.example.ui.theme.*
import com.example.viewmodel.PurpleRoomsViewModel
import com.example.viewmodel.UiState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    uiState: UiState,
    viewModel: PurpleRoomsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mapOffsetX by remember { mutableFloatStateOf(0f) }
    var mapOffsetY by remember { mutableFloatStateOf(0f) }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var selectedRadiusKm by remember { mutableDoubleStateOf(5.0) }
    var showSearchThisArea by remember { mutableStateOf(false) }

    val selectedProperty = uiState.mapSelectedProperty ?: uiState.properties.firstOrNull()

    // Calculate map bounds and visible properties
    val currentCenterLat = uiState.mapCenterLat - (mapOffsetY * 0.0005)
    val currentCenterLng = uiState.mapCenterLng + (mapOffsetX * 0.0005)

    val propertiesWithDistance = remember(uiState.properties, currentCenterLat, currentCenterLng, selectedRadiusKm) {
        uiState.properties.map { prop ->
            val dist = calculateDistanceKm(currentCenterLat, currentCenterLng, prop.latitude, prop.longitude)
            prop to dist
        }.filter { it.second <= selectedRadiusKm + 10.0 }
        .sortedBy { it.second }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Explore on Map",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = uiState.selectedLocation,
                            fontSize = 12.sp,
                            color = PurplePrimary,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("map_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PurplePrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openChooseLocationScreen() },
                        modifier = Modifier.testTag("map_change_location_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditLocationAlt,
                            contentDescription = "Change Location",
                            tint = PurplePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundLavender,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Map Canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE5E7EB))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                mapOffsetX += dragAmount.x
                                mapOffsetY += dragAmount.y
                                showSearchThisArea = true
                            }
                        )
                    }
            ) {
                // Vector Map Grid Lines & Roads Rendering
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2 + mapOffsetX
                    val cy = h / 2 + mapOffsetY

                    // Map grid background
                    drawRect(Color(0xFFF3F4F6))

                    // Simulated Roads & Blocks
                    val roadColor = Color(0xFFFFFFFF)
                    val highwayColor = Color(0xFFFEF3C7)
                    val riverColor = Color(0xFFBFDBFE)

                    // River
                    val riverPath = Path().apply {
                        moveTo(cx - 300f, cy - h)
                        cubicTo(cx - 150f, cy - 200f, cx - 200f, cy + 200f, cx - 100f, cy + h)
                    }
                    drawPath(riverPath, riverColor, style = Stroke(width = 32f, cap = StrokeCap.Round))

                    // Main Highways
                    drawLine(
                        color = highwayColor,
                        start = Offset(0f, cy),
                        end = Offset(w, cy),
                        strokeWidth = 24f
                    )
                    drawLine(
                        color = highwayColor,
                        start = Offset(cx, 0f),
                        end = Offset(cx, h),
                        strokeWidth = 24f
                    )

                    // Secondary roads grid
                    for (i in -4..4) {
                        val yPos = cy + (i * 140f)
                        drawLine(
                            color = roadColor,
                            start = Offset(0f, yPos),
                            end = Offset(w, yPos),
                            strokeWidth = 12f
                        )
                        val xPos = cx + (i * 140f)
                        drawLine(
                            color = roadColor,
                            start = Offset(xPos, 0f),
                            end = Offset(xPos, h),
                            strokeWidth = 12f
                        )
                    }

                    // Selected Location Area Radius Circle
                    drawCircle(
                        color = PurplePrimary.copy(alpha = 0.08f),
                        radius = (selectedRadiusKm * 40f).toFloat().coerceIn(120f, 400f),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = PurplePrimary.copy(alpha = 0.35f),
                        radius = (selectedRadiusKm * 40f).toFloat().coerceIn(120f, 400f),
                        center = Offset(cx, cy),
                        style = Stroke(width = 2f)
                    )

                    // User GPS Pulse Location Marker
                    drawCircle(
                        color = Color(0xFF3B82F6).copy(alpha = 0.25f),
                        radius = 28f,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 10f,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color(0xFF2563EB),
                        radius = 7f,
                        center = Offset(cx, cy)
                    )
                }

                // Property Pin Markers overlaid dynamically on the Map
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val centerX = constraints.maxWidth / 2f + mapOffsetX
                    val centerY = constraints.maxHeight / 2f + mapOffsetY

                    propertiesWithDistance.forEach { (property, distance) ->
                        // Coordinate to screen mapping relative to center
                        val dLat = (property.latitude - uiState.mapCenterLat) * 8000
                        val dLng = (property.longitude - uiState.mapCenterLng) * 8000
                        val pinX = (centerX + dLng).toFloat()
                        val pinY = (centerY - dLat).toFloat()

                        val isSelected = selectedProperty?.id == property.id

                        if (pinX in -100f..(constraints.maxWidth + 100f) && pinY in -100f..(constraints.maxHeight + 100f)) {
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(pinX.roundToInt() - 40, pinY.roundToInt() - 40) }
                                    .shadow(if (isSelected) 8.dp else 4.dp, RoundedCornerShape(20.dp))
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) PurplePrimary else Color.White)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.5.dp,
                                        color = if (isSelected) Color.White else PurplePrimary,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        viewModel.selectMapProperty(property)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("map_marker_${property.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "🏠",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "₹${property.rent}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else PurplePrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Top Overlay: "Search this area" Button (appears when user pans)
            AnimatedVisibility(
                visible = showSearchThisArea,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Button(
                    onClick = {
                        showSearchThisArea = false
                        viewModel.updateMapCenter(currentCenterLat, currentCenterLng)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier.testTag("search_this_area_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Search this area", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Radius Selector Chips at top
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = if (showSearchThisArea) 68.dp else 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(2.0 to "2 km", 5.0 to "5 km", 10.0 to "10 km", 20.0 to "20 km").forEach { (rad, label) ->
                        val isRadSelected = selectedRadiusKm == rad
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isRadSelected) PurplePrimary else Color.Transparent)
                                .clickable { selectedRadiusKm = rad }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isRadSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isRadSelected) Color.White else TextPrimary
                            )
                        }
                    }
                }
            }

            // Floating Controls: Recenter GPS + Zoom Controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Recenter location button
                FloatingActionButton(
                    onClick = {
                        mapOffsetX = 0f
                        mapOffsetY = 0f
                        showSearchThisArea = false
                    },
                    containerColor = Color.White,
                    contentColor = PurplePrimary,
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp).testTag("recenter_map_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Bottom Property Preview Card when marker selected
            if (selectedProperty != null) {
                val dist = calculateDistanceKm(
                    uiState.mapCenterLat,
                    uiState.mapCenterLng,
                    selectedProperty.latitude,
                    selectedProperty.longitude
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(12.dp, RoundedCornerShape(18.dp))
                        .testTag("map_property_preview_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Property thumbnail
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = selectedProperty.images.firstOrNull() ?: R.drawable.hero_bedroom_splash),
                                contentDescription = selectedProperty.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedProperty.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = RatingAmber,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${selectedProperty.rating}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
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
                                        text = "${selectedProperty.locality}, ${selectedProperty.city} • ${formatDistance(dist)}",
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
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "₹${selectedProperty.rent}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PurplePrimary
                                        )
                                        Text(
                                            text = "/month",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }

                                    Surface(
                                        color = Color(0xFFECFDF5),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Zero Brokerage",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF059669),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                viewModel.selectProperty(selectedProperty)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("view_property_from_map_btn")
                        ) {
                            Text("View Property", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
