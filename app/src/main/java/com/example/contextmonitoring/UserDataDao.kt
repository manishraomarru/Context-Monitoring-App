package com.example.contextmonitoring

import androidx.room.Update
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Dao

@Dao
interface UserDataDao {
    @Insert
    fun insert(userInfo: UserData): Long

    @Update
    fun update(userInfo: UserData): Int

    @Query("SELECT COUNT(*) FROM UserData")
    fun count(): Int

    // Get the latest data row
    @Query("SELECT * FROM UserData where timestamp=(SELECT MAX(timestamp) FROM UserData)")
    fun getRecentData(): UserData?

}
