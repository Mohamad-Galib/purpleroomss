package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.SessionEntity
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.model.User
import com.example.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    data class Loading(val message: String = "Please wait...") : AuthResult<Nothing>()
}

sealed class OtpVerifyResult {
    data class ExistingUser(val user: User) : OtpVerifyResult()
    data class NewUser(val mobile: String) : OtpVerifyResult()
    data class Error(val message: String) : OtpVerifyResult()
    data class Expired(val message: String) : OtpVerifyResult()
    data class TooManyAttempts(val message: String) : OtpVerifyResult()
}

data class OtpSession(
    val mobile: String,
    val otpCode: String,
    val createdAt: Long = System.currentTimeMillis(),
    var attempts: Int = 0
)

data class PasswordResetSession(
    val email: String,
    val token: String,
    val createdAt: Long = System.currentTimeMillis()
)

class AuthRepository(
    private val userDao: UserDao
) {
    private val activeOtps = ConcurrentHashMap<String, OtpSession>()
    private val activeResetTokens = ConcurrentHashMap<String, PasswordResetSession>()

    // Fixed Salt for hashing
    private val passwordSalt = "PurpleRoomsSecuritySalt2026"

    suspend fun initializePreSeededUsers() = withContext(Dispatchers.IO) {
        if (userDao.countUsers() == 0) {
            val student = User(
                id = "student_001",
                name = "Aarav Mehta",
                email = "student@purplerooms.com",
                mobile = "+91 98765 43210",
                passwordHash = hashPassword("password123"),
                role = UserRole.STUDENT,
                isVerified = true,
                profileImage = null
            )
            val owner = User(
                id = "owner_001",
                name = "Rahul Sharma",
                email = "owner@purplerooms.com",
                mobile = "+91 91234 56789",
                passwordHash = hashPassword("password123"),
                role = UserRole.OWNER,
                isVerified = true,
                profileImage = null
            )
            userDao.insertUser(UserEntity.fromDomainUser(student))
            userDao.insertUser(UserEntity.fromDomainUser(owner))
        }
    }

    private suspend fun ensureSeeded() {
        if (userDao.countUsers() == 0) {
            initializePreSeededUsers()
        }
    }

    fun hashPassword(password: String): String {
        val input = "$password:$passwordSalt"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun loginWithEmail(email: String, password: String): AuthResult<User> = withContext(Dispatchers.IO) {
        ensureSeeded()
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) {
            return@withContext AuthResult.Error("Email address is required.")
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return@withContext AuthResult.Error("Please enter a valid email address.")
        }
        if (password.isBlank()) {
            return@withContext AuthResult.Error("Password cannot be empty.")
        }

        val userEntity = userDao.getUserByEmail(cleanEmail)
            ?: return@withContext AuthResult.Error("No account found for $cleanEmail. Please sign up.")

        val computedHash = hashPassword(password)
        if (userEntity.passwordHash != computedHash) {
            return@withContext AuthResult.Error("Incorrect password. Please try again.")
        }

        val user = userEntity.toDomainUser()
        saveSession(user)
        AuthResult.Success(user)
    }

    suspend fun sendMobileOtp(mobileInput: String): AuthResult<String> = withContext(Dispatchers.IO) {
        val cleanMobile = normalizeMobile(mobileInput)
        if (cleanMobile.length < 10) {
            return@withContext AuthResult.Error("Please enter a valid 10-digit mobile number.")
        }

        // Rate limiting: check if last OTP sent within 15 seconds
        val existingSession = activeOtps[cleanMobile]
        val now = System.currentTimeMillis()
        if (existingSession != null && (now - existingSession.createdAt) < 15000) {
            return@withContext AuthResult.Error("Please wait 15 seconds before requesting another OTP.")
        }

        // Generate genuine 6-digit random OTP
        val code = (100000..999999).random().toString()
        activeOtps[cleanMobile] = OtpSession(
            mobile = cleanMobile,
            otpCode = code,
            createdAt = now,
            attempts = 0
        )

        AuthResult.Success(code)
    }

    suspend fun verifyMobileOtp(mobileInput: String, enteredOtp: String): OtpVerifyResult = withContext(Dispatchers.IO) {
        ensureSeeded()
        val cleanMobile = normalizeMobile(mobileInput)
        val cleanCode = enteredOtp.trim()

        if (cleanCode.length != 6) {
            return@withContext OtpVerifyResult.Error("Please enter the complete 6-digit verification code.")
        }

        val session = activeOtps[cleanMobile]
            ?: return@withContext OtpVerifyResult.Error("No active OTP request found. Please tap Resend OTP.")

        val now = System.currentTimeMillis()
        // 2 minutes expiry (120,000 ms)
        if (now - session.createdAt > 120000) {
            activeOtps.remove(cleanMobile)
            return@withContext OtpVerifyResult.Expired("OTP has expired (2 min limit). Please request a new code.")
        }

        session.attempts += 1
        if (session.attempts > 4) {
            activeOtps.remove(cleanMobile)
            return@withContext OtpVerifyResult.TooManyAttempts("Too many incorrect attempts. Please request a new OTP.")
        }

        if (cleanCode != session.otpCode) {
            val remaining = 4 - session.attempts
            return@withContext OtpVerifyResult.Error("Invalid OTP. $remaining attempt(s) remaining.")
        }

        // Verified successfully!
        activeOtps.remove(cleanMobile)

        // Check if user already exists
        val existingUser = userDao.getUserByCleanMobile(cleanMobile)
            ?: userDao.getUserByMobile(cleanMobile)
            ?: userDao.getUserByMobile("+91 $cleanMobile")

        if (existingUser != null) {
            val user = existingUser.toDomainUser()
            saveSession(user)
            OtpVerifyResult.ExistingUser(user)
        } else {
            OtpVerifyResult.NewUser(cleanMobile)
        }
    }

    suspend fun registerUser(
        name: String,
        email: String,
        mobile: String,
        password: String,
        confirmPassword: String,
        role: UserRole,
        termsAccepted: Boolean
    ): AuthResult<User> = withContext(Dispatchers.IO) {
        ensureSeeded()
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanMobile = normalizeMobile(mobile)

        if (cleanName.isBlank()) {
            return@withContext AuthResult.Error("Full Name is required.")
        }
        if (cleanEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return@withContext AuthResult.Error("Please enter a valid email address.")
        }
        if (cleanMobile.length < 10) {
            return@withContext AuthResult.Error("Please enter a valid 10-digit mobile number.")
        }
        if (password.length < 6) {
            return@withContext AuthResult.Error("Password must be at least 6 characters long.")
        }
        if (password != confirmPassword) {
            return@withContext AuthResult.Error("Passwords do not match. Please verify.")
        }
        if (!termsAccepted) {
            return@withContext AuthResult.Error("You must accept the Terms & Conditions to register.")
        }

        if (userDao.getUserByEmail(cleanEmail) != null) {
            return@withContext AuthResult.Error("An account with this email is already registered. Please log in.")
        }
        if (userDao.getUserByMobile(cleanMobile) != null) {
            return@withContext AuthResult.Error("An account with this mobile number already exists. Please log in.")
        }

        val prefix = if (role == UserRole.OWNER) "owner" else "student"
        val newUser = User(
            id = "${prefix}_${System.currentTimeMillis()}",
            name = cleanName,
            email = cleanEmail,
            mobile = "+91 $cleanMobile",
            passwordHash = hashPassword(password),
            role = role,
            isVerified = true,
            profileImage = null
        )

        userDao.insertUser(UserEntity.fromDomainUser(newUser))
        saveSession(newUser)
        AuthResult.Success(newUser)
    }

    suspend fun completeProfile(
        name: String,
        email: String,
        mobile: String,
        role: UserRole,
        profileImage: String? = null
    ): AuthResult<User> = withContext(Dispatchers.IO) {
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanMobile = normalizeMobile(mobile)

        if (cleanName.isBlank()) {
            return@withContext AuthResult.Error("Full Name is required.")
        }
        if (cleanEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return@withContext AuthResult.Error("Please enter a valid email address.")
        }

        val existingWithEmail = userDao.getUserByEmail(cleanEmail)
        if (existingWithEmail != null && existingWithEmail.mobile != cleanMobile) {
            return@withContext AuthResult.Error("Email $cleanEmail is already associated with another account.")
        }

        val prefix = if (role == UserRole.OWNER) "owner" else "student"
        val newUser = User(
            id = "${prefix}_${System.currentTimeMillis()}",
            name = cleanName,
            email = cleanEmail,
            mobile = "+91 $cleanMobile",
            passwordHash = hashPassword("PurpleOtpUser${System.currentTimeMillis()}"),
            role = role,
            isVerified = true,
            profileImage = profileImage
        )

        userDao.insertUser(UserEntity.fromDomainUser(newUser))
        saveSession(newUser)
        AuthResult.Success(newUser)
    }

    suspend fun sendPasswordResetToken(email: String): AuthResult<String> = withContext(Dispatchers.IO) {
        ensureSeeded()
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return@withContext AuthResult.Error("Please enter a valid registered email address.")
        }

        val user = userDao.getUserByEmail(cleanEmail)
            ?: return@withContext AuthResult.Error("No account found for $cleanEmail.")

        val token = (100000..999999).random().toString()
        activeResetTokens[cleanEmail] = PasswordResetSession(
            email = cleanEmail,
            token = token,
            createdAt = System.currentTimeMillis()
        )

        AuthResult.Success(token)
    }

    suspend fun resetPassword(
        email: String,
        token: String,
        newPassword: String,
        confirmPassword: String
    ): AuthResult<Unit> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanToken = token.trim()

        val session = activeResetTokens[cleanEmail]
            ?: return@withContext AuthResult.Error("No reset request found for this email. Please request a new link.")

        val now = System.currentTimeMillis()
        if (now - session.createdAt > 300000) { // 5 minutes
            activeResetTokens.remove(cleanEmail)
            return@withContext AuthResult.Error("Reset token has expired. Please request a new reset link.")
        }

        if (session.token != cleanToken) {
            return@withContext AuthResult.Error("Invalid reset code. Please check and try again.")
        }

        if (newPassword.length < 6) {
            return@withContext AuthResult.Error("New password must be at least 6 characters.")
        }

        if (newPassword != confirmPassword) {
            return@withContext AuthResult.Error("Passwords do not match.")
        }

        val userEntity = userDao.getUserByEmail(cleanEmail)
            ?: return@withContext AuthResult.Error("User record not found.")

        val updatedEntity = userEntity.copy(
            passwordHash = hashPassword(newPassword),
            updatedAt = System.currentTimeMillis()
        )
        userDao.updateUser(updatedEntity)
        activeResetTokens.remove(cleanEmail)

        AuthResult.Success(Unit)
    }

    suspend fun restoreActiveSession(): User? = withContext(Dispatchers.IO) {
        val session = userDao.getActiveSession() ?: return@withContext null
        val userEntity = userDao.getUserById(session.userId) ?: return@withContext null
        userEntity.toDomainUser()
    }

    suspend fun saveSession(user: User) = withContext(Dispatchers.IO) {
        userDao.saveActiveSession(
            SessionEntity(
                id = 1,
                userId = user.id,
                email = user.email,
                role = user.role.name,
                token = "pr_token_${user.id}_${System.currentTimeMillis()}",
                loggedInAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        userDao.clearActiveSession()
    }

    private fun normalizeMobile(input: String): String {
        val digitsOnly = input.replace(Regex("[^0-9]"), "")
        return if (digitsOnly.length > 10) {
            digitsOnly.takeLast(10)
        } else {
            digitsOnly
        }
    }
}
