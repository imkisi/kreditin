package com.example.kreditin.ui.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kreditin.ui.data.creditor.Creditor
import com.example.kreditin.ui.data.creditor.CreditorDao
import com.example.kreditin.ui.data.motorcycle.Motorcycle
import com.example.kreditin.ui.data.motorcycle.MotorcycleDao
import com.example.kreditin.ui.data.staff.Staff
import com.example.kreditin.ui.data.staff.StaffDao

@Database(entities = [Motorcycle::class, Staff::class, Creditor::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun motorcycleDao(): MotorcycleDao
    abstract fun staffDao(): StaffDao
    abstract fun creditorDao(): CreditorDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kreditin_database" // Use a consistent database name
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return the instance
                instance
            }
        }
    }
}
