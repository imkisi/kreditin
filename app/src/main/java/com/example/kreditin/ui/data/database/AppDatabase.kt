package com.example.kreditin.ui.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kreditin.ui.data.motorcycle.Motorcycle
import com.example.kreditin.ui.data.motorcycle.MotorcycleDao
import com.example.kreditin.ui.data.staff.Staff
import com.example.kreditin.ui.data.staff.StaffDao

@Database(entities = [Motorcycle::class, Staff::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun motorcycleDao(): MotorcycleDao
    abstract fun staffDao(): StaffDao

}