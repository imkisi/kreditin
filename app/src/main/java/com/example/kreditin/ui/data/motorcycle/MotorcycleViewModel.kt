package com.example.kreditin.ui.data.motorcycle

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

class MotorcycleViewModel(application: Application) : AndroidViewModel(application) {

    private val motorcycleDaoFlow = flow {
        emit(AppDatabase.getDatabase(application).motorcycleDao())
    }

    val allMotorcycles: StateFlow<List<Motorcycle>> = motorcycleDaoFlow.flatMapLatest { dao ->
        dao.getAllMotorcycles()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addMotorcycle(motorcycle: Motorcycle) {
        viewModelScope.launch {
            motorcycleDaoFlow.collect { dao ->
                dao.insertMotorcycle(motorcycle)
            }
        }
    }

    fun deleteMotorcycle(motorcycle: Motorcycle) {
        viewModelScope.launch {
            motorcycleDaoFlow.collect { dao ->
                dao.deleteMotorcycle(motorcycle)
            }
        }
    }

    fun getMotorcycleById(id: Int): Flow<Motorcycle?> {
        return motorcycleDaoFlow.flatMapLatest { dao ->
            dao.getMotorcycleById(id)
        }
    }

    fun updateMotorcycle(motorcycle: Motorcycle) {
        viewModelScope.launch {
            motorcycleDaoFlow.collect { dao ->
                dao.updateMotorcycle(motorcycle)
            }
        }
    }

}