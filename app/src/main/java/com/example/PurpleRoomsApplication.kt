package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.AuthRepository

class PurpleRoomsApplication : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var locationRepository: com.example.data.repository.LocationRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        authRepository = AuthRepository(database.userDao())
        locationRepository = com.example.data.repository.LocationRepository(database.locationDao())
    }

    companion object {
        var instance: PurpleRoomsApplication? = null
            private set
    }
}
