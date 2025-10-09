package com.example.kreditin.ui.data.motorcycle

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Motorcycle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val model: String,
    val year: Int,
    val color: String,
    val price: Int
)
