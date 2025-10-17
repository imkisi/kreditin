package com.example.kreditin.ui.data.creditor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Creditor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val position: String,
    val email: String,
    val phone: String,
    val address: String
)
