package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE REPLACE(REPLACE(REPLACE(mobile, ' ', ''), '-', ''), '+91', '') = :cleanDigits OR mobile = :cleanDigits LIMIT 1")
    suspend fun getUserByCleanMobile(cleanDigits: String): UserEntity?

    @Query("SELECT * FROM users WHERE mobile = :mobile OR mobile = :trimmedMobile LIMIT 1")
    suspend fun getUserByMobile(mobile: String, trimmedMobile: String = mobile.replace("+91", "").trim()): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsers(): Int

    @Query("SELECT * FROM active_session WHERE id = 1 LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?

    @Query("SELECT * FROM active_session WHERE id = 1 LIMIT 1")
    fun observeActiveSession(): Flow<SessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActiveSession(session: SessionEntity)

    @Query("DELETE FROM active_session")
    suspend fun clearActiveSession()
}
