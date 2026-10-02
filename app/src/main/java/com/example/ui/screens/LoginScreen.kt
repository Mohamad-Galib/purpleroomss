package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.ui.components.PrimaryPurpleButton
import com.example.ui.components.PurpleRoomsBrand
import com.example.ui.components.SecondaryOutlinedButton
import com.example.ui.theme.*
import com.example.viewmodel.PurpleRoomsViewModel
import com.example.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    uiState: UiState,
    viewModel: PurpleRoomsViewModel,
    onBackClick: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onNavigateToMobileOtp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf(if (uiState.intendedRole == UserRole.OWNER) "owner@purplerooms.com" else "student@purplerooms.com") }
    var password by remember { mutableStateOf("password123") }
    var passwordVisible by remember { mutableStateOf(false) }

    val role = uiState.intendedRole

    Scaffold(
        topBar = {
            TopAppBar(
                title = { PurpleRoomsBrand(isLarge = false) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("login_back_btn")
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
            Spacer(modifier = Modifier.height(12.dp))

            // Role Badge with Toggle
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PurpleSubtle,
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                modifier = Modifier.testTag("role_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (role == UserRole.OWNER) Icons.Default.Business else Icons.Default.School,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (role == UserRole.OWNER) "Logging in as Owner" else "Logging in as Student",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PurplePrimary
                    )
                    Text(
                        text = "• Switch",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurpleDark,
                        modifier = Modifier
                            .clickable {
                                val nextRole = if (role == UserRole.OWNER) UserRole.STUDENT else UserRole.OWNER
                                viewModel.setIntendedRole(nextRole)
                                email = if (nextRole == UserRole.OWNER) "owner@purplerooms.com" else "student@purplerooms.com"
                            }
                            .testTag("switch_intended_role_btn")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (role == UserRole.OWNER)
                    "Access your property dashboard & tenant inquiries"
                else
                    "Discover verified, brokerage-free rooms & hostels",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Feedback / Error Banner
            if (uiState.authError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("auth_error_banner")
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

            // Quick demo buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.setIntendedRole(UserRole.STUDENT)
                        email = "student@purplerooms.com"
                        password = "password123"
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_fill_student_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (role == UserRole.STUDENT) PurplePrimary else TextSecondary
                    )
                ) {
                    Text("Demo Student", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.setIntendedRole(UserRole.OWNER)
                        email = "owner@purplerooms.com"
                        password = "password123"
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_fill_owner_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (role == UserRole.OWNER) PurplePrimary else TextSecondary
                    )
                ) {
                    Text("Demo Owner", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (uiState.authError != null) viewModel.clearAuthFeedback()
                },
                label = { Text("Email") },
                placeholder = { Text("Enter email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = PurplePrimary
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
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
                    .testTag("email_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (uiState.authError != null) viewModel.clearAuthFeedback()
                },
                label = { Text("Password") },
                placeholder = { Text("Enter password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = PurplePrimary
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.testTag("toggle_password_visibility")
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = PurplePrimary
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.loginWithEmail(email, password)
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
                    .testTag("password_input")
            )

            // Forgot Password
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot Password?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PurplePrimary,
                    modifier = Modifier
                        .clickable(onClick = onNavigateToForgotPassword)
                        .padding(4.dp)
                        .testTag("forgot_password_link")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Login Button
            PrimaryPurpleButton(
                text = if (uiState.isAuthLoading) "Authenticating..." else "Login",
                enabled = !uiState.isAuthLoading,
                onClick = {
                    focusManager.clearFocus()
                    viewModel.loginWithEmail(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_submit_btn")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Divider OR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = PurpleBorder)
                Text(
                    text = "OR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = PurpleBorder)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Continue with Mobile OTP
            SecondaryOutlinedButton(
                text = "Continue with Mobile OTP",
                trailingIcon = Icons.Default.PhoneIphone,
                onClick = onNavigateToMobileOtp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("continue_mobile_otp_btn")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up link
            Row(
                modifier = Modifier.padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Sign Up",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary,
                    modifier = Modifier
                        .clickable(onClick = onNavigateToSignup)
                        .padding(4.dp)
                        .testTag("nav_to_signup_link")
                )
            }
        }
    }
}
