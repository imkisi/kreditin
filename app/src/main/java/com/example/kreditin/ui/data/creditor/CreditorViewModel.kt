package com.example.kreditin.ui.data.creditor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kreditin.ui.data.database.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CreditorViewModel(application: Application) : AndroidViewModel(application) {

    private val creditorDaoFlow = flow {
        emit(AppDatabase.getDatabase(application).creditorDao())
    }

    val allCreditors: StateFlow<List<Creditor>> = creditorDaoFlow.flatMapLatest { dao ->
        dao.getAllCreditors()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addCreditor(creditor: Creditor) {
        viewModelScope.launch {
            creditorDaoFlow.collect { dao ->
                dao.insertCreditor(creditor)
            }
        }
    }

    fun deleteCreditor(creditor: Creditor) {
        viewModelScope.launch {
            creditorDaoFlow.collect { dao ->
                dao.deleteCreditor(creditor)
            }
        }
    }

    fun getCreditorById(id: Int): Flow<Creditor?> {
        return creditorDaoFlow.flatMapLatest { dao ->
            dao.getCreditorById(id)
        }
    }

    fun updateCreditor(creditor: Creditor) {
        viewModelScope.launch {
            creditorDaoFlow.collect { dao ->
                dao.updateCreditor(creditor)
            }
        }
    }
}
