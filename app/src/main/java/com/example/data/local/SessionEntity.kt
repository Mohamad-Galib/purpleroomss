package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_session")
data class SessionEntity(
    @PrimaryKey val id: Int = 1,
    val userId: String,
    val email: String,
    val role: String,
    val token: String,
    val loggedInAt: Long = System.currentTimeMillis()
)
