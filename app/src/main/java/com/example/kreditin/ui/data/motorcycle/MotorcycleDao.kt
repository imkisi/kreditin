package com.example.kreditin.ui.data.motorcycle

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MotorcycleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotorcycle(motorcycle: Motorcycle)

    @Update
    suspend fun updateMotorcycle(motorcycle: Motorcycle)

    @Delete
    suspend fun deleteMotorcycle(motorcycle: Motorcycle)

    @Query("SELECT * FROM motorcycle")
    fun getAllMotorcycles(): Flow<List<Motorcycle>>

    @Query("SELECT * FROM motorcycle WHERE id = :id")
    fun getMotorcycleById(id: Int): Flow<Motorcycle?>
}