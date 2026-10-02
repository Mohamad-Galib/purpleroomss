package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.PrimaryPurpleButton
import com.example.ui.components.PurpleRoomsBrand
import com.example.ui.components.SecondaryOutlinedButton
import com.example.ui.theme.*

@Composable
fun SplashScreen(
    onContinueAsStudent: () -> Unit,
    onContinueAsOwner: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLavender)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PurpleRoomsBrand(isLarge = false)
                Text(
                    text = "Skip >",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PurplePrimary,
                    modifier = Modifier
                        .clickable(onClick = onContinueAsStudent)
                        .padding(6.dp)
                        .testTag("skip_button")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Tagline
            Text(
                text = buildAnnotatedString {
                    append("Find Your Perfect Space\n")
                    withStyle(style = SpanStyle(color = PurplePrimary, fontWeight = FontWeight.ExtraBold)) {
                        append("Without Any Brokerage.")
                    }
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4 USP Icons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ) {
                UspItem(
                    icon = Icons.Default.CurrencyRupee,
                    title = "NO\nBROKERAGE"
                )
                UspItem(
                    icon = Icons.Default.VerifiedUser,
                    title = "100% SAFE\n& VERIFIED"
                )
                UspItem(
                    icon = Icons.Default.PersonOutline,
                    title = "DIRECT\nOWNER"
                )
                UspItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "NO HIDDEN\nFEES"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hero Image Card
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, PurpleBorder, RoundedCornerShape(20.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.hero_bedroom_splash),
                        contentDescription = "Purple Rooms Interior",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.35f)
                                    ),
                                    startY = 200f
                                )
                            )
                    )
                    // Room wall poster / quote
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                            .border(1.dp, PurpleBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Good Rooms\nBetter\nTomorrows",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurplePrimary,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryPurpleButton(
                    text = "Continue as Student",
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onContinueAsStudent,
                    modifier = Modifier.testTag("continue_student_btn")
                )

                SecondaryOutlinedButton(
                    text = "Continue as Owner",
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onContinueAsOwner,
                    modifier = Modifier.testTag("continue_owner_btn")
                )

                Text(
                    text = "Login / Sign Up",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PurplePrimary,
                    modifier = Modifier
                        .clickable(onClick = onLoginClick)
                        .padding(vertical = 4.dp)
                        .testTag("login_signup_btn")
                )

                Text(
                    text = "No Brokerage. Verified Listings. Safe & Secure.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun UspItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PurpleSubtle)
                .border(1.5.dp, PurplePrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PurplePrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )
    }
}
