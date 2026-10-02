package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StepIndicator
import com.example.ui.theme.*
import com.example.viewmodel.BookingDraft
import com.example.viewmodel.PurpleRoomsViewModel
import com.example.viewmodel.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    uiState: UiState,
    viewModel: PurpleRoomsViewModel,
    onBackClick: () -> Unit,
    onBookingSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val draft = uiState.bookingDraft ?: BookingDraft(property = uiState.selectedProperty, selectedRoom = uiState.selectedProperty.roomOptions.first())

    var selectedMethod by remember { mutableStateOf("ONLINE") }
    var isProcessing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val totalAmount = draft.rentAmount + draft.depositAmount + draft.serviceFee

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Payment",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("payment_back_btn")
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
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            if (selectedMethod == "COD") {
                                viewModel.confirmBooking(isCod = true)
                            } else {
                                isProcessing = true
                                coroutineScope.launch {
                                    delay(1000)
                                    isProcessing = false
                                    viewModel.confirmBooking(isCod = false)
                                }
                            }
                        },
                        enabled = !isProcessing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("pay_button")
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Processing Payment...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text(
                                text = if (selectedMethod == "COD") "Confirm & Pay at Property" else "Pay ₹${"%,d".format(totalAmount)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "100% Secure Payment",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
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
            // Step Indicator
            item {
                StepIndicator(
                    currentStep = 3,
                    totalSteps = 4,
                    stepTitles = listOf("Property", "Details", "Payment", "Confirmation")
                )
            }

            // Payment Methods Selection Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select Payment Method",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Online Payment
                        PaymentMethodRow(
                            icon = Icons.Default.CreditCard,
                            title = "Online Payment",
                            subtitle = "UPI, Cards, Net Banking",
                            isSelected = selectedMethod == "ONLINE",
                            onClick = { selectedMethod = "ONLINE" }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // COD / Pay at Property
                        PaymentMethodRow(
                            icon = Icons.Default.Payments,
                            title = "Pay at Property (Cash)",
                            subtitle = "Pay when you move in (COD)",
                            isSelected = selectedMethod == "COD",
                            onClick = { selectedMethod = "COD" }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Wallet Balance
                        PaymentMethodRow(
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "Wallet Balance",
                            subtitle = "Available: ₹2,000",
                            isSelected = selectedMethod == "WALLET",
                            onClick = { selectedMethod = "WALLET" }
                        )
                    }
                }
            }

            // Payment Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Payment Summary",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SummaryRow(
                            label = "Rent (1 Month)",
                            value = "₹${"%,d".format(draft.rentAmount)}"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SummaryRow(
                            label = "Security Deposit",
                            value = "₹${"%,d".format(draft.depositAmount)}"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SummaryRow(
                            label = "Service Fee",
                            value = "₹0",
                            highlight = "Free"
                        )

                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = PurpleBorder
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Amount",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "₹${"%,d".format(totalAmount)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PurplePrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) PurpleSubtle else Color(0xFFF9F8FD))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) PurplePrimary else PurpleBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, PurpleBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = PurplePrimary,
                unselectedColor = TextSecondary
            )
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    highlight: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (highlight != null) {
                Text(
                    text = highlight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerifiedGreen,
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}
