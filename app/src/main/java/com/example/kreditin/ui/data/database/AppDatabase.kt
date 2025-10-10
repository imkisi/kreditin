package com.example.kreditin.ui.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kreditin.ui.data.motorcycle.Motorcycle
import com.example.kreditin.ui.data.motorcycle.MotorcycleDao
import com.example.kreditin.ui.data.staff.Staff
import com.example.kreditin.ui.data.staff.StaffDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(entities = [Motorcycle::class, Staff::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun motorcycleDao(): MotorcycleDao
    abstract fun staffDao(): StaffDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        suspend fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            val instance = withContext(Dispatchers.IO) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
            }

            return synchronized(this) {
                val existingInstance = INSTANCE
                if (existingInstance != null) {
                    existingInstance
                } else {
                    INSTANCE = instance
                    instance
                }
            }
        }
    }
}
