package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Query("SELECT * FROM recent_locations ORDER BY timestamp DESC LIMIT 5")
    suspend fun getRecentLocations(): List<RecentLocationEntity>

    @Query("SELECT * FROM recent_locations ORDER BY timestamp DESC LIMIT 5")
    fun observeRecentLocations(): Flow<List<RecentLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentLocation(location: RecentLocationEntity)

    @Delete
    suspend fun deleteRecentLocation(location: RecentLocationEntity)

    @Query("DELETE FROM recent_locations WHERE placeId = :placeId")
    suspend fun deleteRecentLocationById(placeId: String)

    @Query("DELETE FROM recent_locations")
    suspend fun clearRecentLocations()
}
