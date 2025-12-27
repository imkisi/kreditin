package com.example.kreditin.ui.data.transaction

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val creditorName: String,
    val creditorAddress: String,
    val creditorPhone: String,
    val motorcycleName: String,
    val motorcyclePrice: Double,
    val downPayment: Double,
    val interestRate: Double,
    val tenure: Int,
    val loanPrincipal: Double,
    val totalLoanAmount: Double,
    val monthlyPayment: Double,
    val timestamp: Long = System.currentTimeMillis()
)