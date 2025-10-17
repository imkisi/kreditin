package com.example.kreditin.ui.data.creditor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.kreditin.ui.data.database.AppDatabase
import kotlinx.coroutines.flow.flow

class CreditorViewModel (application: Application) : AndroidViewModel(application) {
    private val creditorDaoFlow = flow {
        emit(AppDatabase.getDatabase(application).creditorDao())
    }

    val allCreditor = creditorDaoFlow
}