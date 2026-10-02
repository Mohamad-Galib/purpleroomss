package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.User
import com.example.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val mobile: String,
    val passwordHash: String,
    val role: String,
    val isVerified: Boolean = true,
    val profileImage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomainUser(): User {
        val userRole = try {
            UserRole.valueOf(role)
        } catch (e: Exception) {
            UserRole.STUDENT
        }
        return User(
            id = id,
            name = name,
            email = email,
            mobile = mobile,
            passwordHash = passwordHash,
            role = userRole,
            isVerified = isVerified,
            profileImage = profileImage,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomainUser(user: User): UserEntity {
            return UserEntity(
                id = user.id,
                name = user.name,
                email = user.email.trim().lowercase(),
                mobile = user.mobile.trim(),
                passwordHash = user.passwordHash,
                role = user.role.name,
                isVerified = user.isVerified,
                profileImage = user.profileImage,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}
