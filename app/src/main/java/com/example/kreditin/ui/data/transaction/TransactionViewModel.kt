package com.example.kreditin.ui.data.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kreditin.ui.data.database.AppDatabase
import kotlinx.coroutines.launch

class TransactionViewModel(private val database: AppDatabase) : ViewModel() {
    // ... data Flow lainnya ...

    fun saveTransaction(transaction: Transaction) {
        viewModelScope.launch {
            database.transactionDao().insertTransaction(transaction)
        }
    }
}