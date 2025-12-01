package com.example.kreditin.ui.data.creditor

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditor(creditor: Creditor)

    @Update
    suspend fun updateCreditor(creditor: Creditor)

    @Delete
    suspend fun deleteCreditor(creditor: Creditor)

    @Query("SELECT * FROM creditor")
    fun getAllCreditors(): Flow<List<Creditor>>

    @Query("SELECT * FROM creditor WHERE id = :id")
    fun getCreditorById(id: Int): Flow<Creditor?>
}
