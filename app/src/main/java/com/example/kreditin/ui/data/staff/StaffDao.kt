package com.example.kreditin.ui.data.staff

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: Staff)

    @Update
    suspend fun updateStaff(staff: Staff)

    @Delete
    suspend fun deleteStaff(staff: Staff)

    @Query("SELECT * FROM staff")
    fun getAllStaff(): Flow<List<Staff>>

    @Query("SELECT * FROM staff WHERE id = :id")
    fun getStaffById(id: Int): Flow<Staff?>
}