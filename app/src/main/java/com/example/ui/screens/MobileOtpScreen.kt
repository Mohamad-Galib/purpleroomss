package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PrimaryPurpleButton
import com.example.ui.components.PurpleRoomsBrand
import com.example.ui.theme.*
import com.example.viewmodel.PurpleRoomsViewModel
import com.example.viewmodel.UiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileOtpScreen(
    uiState: UiState,
    viewModel: PurpleRoomsViewModel,
    onBackClick: () -> Unit,
    onSwitchToEmailLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var mobileNumber by remember { mutableStateOf(if (uiState.otpSentMobile.isNotBlank()) uiState.otpSentMobile else "9876543210") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(uiState.otpSentMobile.isNotBlank()) }

    var resendCooldown by remember { mutableIntStateOf(30) }

    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            resendCooldown = 30
            while (resendCooldown > 0) {
                delay(1000)
                resendCooldown--
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { PurpleRoomsBrand(isLarge = false) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isOtpSent) {
                                isOtpSent = false
                                otpCode = ""
                            } else {
                                onBackClick()
                            }
                        },
                        modifier = Modifier.testTag("otp_back_btn")
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Error Banner
            if (uiState.authError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("otp_error_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.authError,
                            fontSize = 13.sp,
                            color = Color(0xFFC62828),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearAuthFeedback() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Real SMS delivery simulation banner
            if (uiState.simulatedSmsToast != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("simulated_sms_banner")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sms,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Simulated Carrier SMS Received",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.simulatedSmsToast,
                            fontSize = 12.sp,
                            color = Color(0xFF2E7D32)
                        )
                        // Extract code if present
                        val extractedOtp = Regex("\\b\\d{6}\\b").find(uiState.simulatedSmsToast)?.value
                        if (extractedOtp != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    otpCode = extractedOtp
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("autofill_otp_btn")
                            ) {
                                Text("Auto-fill OTP ($extractedOtp)", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            if (!isOtpSent) {
                // STEP 1: Enter Mobile Number
                Text(
                    text = "Mobile Login",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Enter your 10-digit mobile number. We'll send an OTP to verify.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
                )

                // Mobile Input Field
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = {
                        val digits = it.filter { char -> char.isDigit() }
                        if (digits.length <= 10) mobileNumber = digits
                    },
                    label = { Text("Mobile Number") },
                    placeholder = { Text("Enter 10-digit mobile number") },
                    leadingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                        ) {
                            Text(
                                text = "+91",
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(20.dp)
                                    .background(PurpleBorder)
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.sendMobileOtp(mobileNumber)
                            isOtpSent = true
                        }
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = PurpleBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mobile_number_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                PrimaryPurpleButton(
                    text = if (uiState.isAuthLoading) "Sending OTP..." else "Send OTP",
                    enabled = !uiState.isAuthLoading && mobileNumber.length >= 10,
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.sendMobileOtp(mobileNumber)
                        isOtpSent = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("send_otp_btn")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Login with Email & Password instead",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PurplePrimary,
                    modifier = Modifier
                        .clickable(onClick = onSwitchToEmailLogin)
                        .padding(8.dp)
                        .testTag("switch_to_email_login_btn")
                )
            } else {
                // STEP 2: Verify OTP
                Text(
                    text = "Verify Your Mobile Number",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Enter the 6-digit OTP sent to +91 $mobileNumber",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                )

                // 6-digit OTP Input
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = {
                        val filtered = it.filter { char -> char.isDigit() }
                        if (filtered.length <= 6) {
                            otpCode = filtered
                            if (filtered.length == 6) {
                                focusManager.clearFocus()
                                viewModel.verifyMobileOtp(mobileNumber, filtered)
                            }
                        }
                    },
                    label = { Text("Enter 6-digit OTP") },
                    placeholder = { Text("• • • • • •") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "OTP",
                            tint = PurplePrimary
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (otpCode.length == 6) {
                                viewModel.verifyMobileOtp(mobileNumber, otpCode)
                            }
                        }
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = PurpleBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("otp_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Visual 6-box indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in 0 until 6) {
                        val char = otpCode.getOrNull(i)?.toString() ?: ""
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(
                                    width = if (i == otpCode.length) 2.dp else 1.dp,
                                    color = if (char.isNotEmpty()) PurplePrimary else PurpleBorder,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                PrimaryPurpleButton(
                    text = if (uiState.isAuthLoading) "Verifying..." else "Verify & Continue",
                    enabled = !uiState.isAuthLoading && otpCode.length == 6,
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.verifyMobileOtp(mobileNumber, otpCode)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verify_otp_btn")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (resendCooldown > 0) "Resend in ${resendCooldown}s" else "Resend OTP",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (resendCooldown > 0) TextSecondary else PurplePrimary,
                        modifier = Modifier
                            .clickable(enabled = resendCooldown == 0) {
                                viewModel.sendMobileOtp(mobileNumber)
                            }
                            .padding(6.dp)
                            .testTag("resend_otp_btn")
                    )

                    Text(
                        text = "Change Mobile Number",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PurplePrimary,
                        modifier = Modifier
                            .clickable {
                                isOtpSent = false
                                otpCode = ""
                                viewModel.clearAuthFeedback()
                            }
                            .padding(6.dp)
                            .testTag("change_mobile_number_btn")
                    )
                }
            }
        }
    }
}
