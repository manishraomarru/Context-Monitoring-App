package com.example.contextmonitoring

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [UserData::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDataDao(): UserDataDao?

    companion object {
        private var dbInstance: AppDatabase? = null
        @Synchronized
        fun getInstance(context: Context): AppDatabase? {
            if (dbInstance == null) {
                dbInstance = Room
                    .databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app_database"
                    )
                    .build()
            }
            return dbInstance
        }
    }
}