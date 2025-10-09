package com.example.kreditin.ui.data.motorcycle

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MotorcycleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotorcycle(motorcycle: Motorcycle)

    @Delete
    suspend fun deleteMotorcycle(motorcycle: Motorcycle)

    @Query("SELECT * FROM motorcycle")
    fun getAllMotorcycles(): Flow<List<Motorcycle>>
}