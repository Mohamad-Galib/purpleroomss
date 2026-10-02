package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.BottomNavTab

@Composable
fun PurpleRoomsBottomBar(
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    isOwnerMode: Boolean = false,
    unreadMessagesCount: Int = 3,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .navigationBarsPadding(),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            BottomNavItem(
                icon = if (currentTab == BottomNavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                label = "Home",
                isSelected = currentTab == BottomNavTab.HOME,
                onClick = { onTabSelected(BottomNavTab.HOME) },
                testTag = "nav_home"
            )

            // Search / My Properties
            if (isOwnerMode) {
                BottomNavItem(
                    icon = if (currentTab == BottomNavTab.OWNER_PROPERTIES) Icons.Filled.Apartment else Icons.Outlined.Apartment,
                    label = "My Properties",
                    isSelected = currentTab == BottomNavTab.OWNER_PROPERTIES,
                    onClick = { onTabSelected(BottomNavTab.OWNER_PROPERTIES) },
                    testTag = "nav_owner_properties"
                )
            } else {
                BottomNavItem(
                    icon = if (currentTab == BottomNavTab.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                    label = "Search",
                    isSelected = currentTab == BottomNavTab.SEARCH,
                    onClick = { onTabSelected(BottomNavTab.SEARCH) },
                    testTag = "nav_search"
                )
            }

            // Bookings
            BottomNavItem(
                icon = if (currentTab == BottomNavTab.BOOKINGS) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                label = "Bookings",
                isSelected = currentTab == BottomNavTab.BOOKINGS,
                onClick = { onTabSelected(BottomNavTab.BOOKINGS) },
                testTag = "nav_bookings"
            )

            // Messages with badge
            BottomNavItem(
                icon = if (currentTab == BottomNavTab.MESSAGES) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                label = "Messages",
                isSelected = currentTab == BottomNavTab.MESSAGES,
                onClick = { onTabSelected(BottomNavTab.MESSAGES) },
                badgeCount = unreadMessagesCount,
                testTag = "nav_messages"
            )

            // Profile
            BottomNavItem(
                icon = if (currentTab == BottomNavTab.PROFILE) Icons.Filled.Person else Icons.Outlined.PersonOutline,
                label = "Profile",
                isSelected = currentTab == BottomNavTab.PROFILE,
                onClick = { onTabSelected(BottomNavTab.PROFILE) },
                testTag = "nav_profile"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0,
    testTag: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) PurplePrimary else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-4).dp)
                        .background(AlertRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$badgeCount",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) PurplePrimary else TextSecondary
        )
    }
}
