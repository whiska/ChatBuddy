package com.weijia.chatbuddy

import android.app.Application
import androidx.room.Room
import com.weijia.chatbuddy.models.AppDatabase

class ChatBuddyApplication : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "chatbuddy-database"
        ).build()
    }
}