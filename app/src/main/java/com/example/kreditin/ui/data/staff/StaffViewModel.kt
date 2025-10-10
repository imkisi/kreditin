package com.example.kreditin.ui.data.staff

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kreditin.ui.data.database.AppDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StaffViewModel(application: Application) : AndroidViewModel(application) {

    private val staffDaoFlow = flow {
        emit(AppDatabase.getDatabase(application).staffDao())
    }

    val allStaff: StateFlow<List<Staff>> = staffDaoFlow.flatMapLatest { dao ->
        dao.getAllStaff()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addStaff(staff: Staff) {
        viewModelScope.launch {
            staffDaoFlow.collect { dao ->
                dao.insertStaff(staff)
            }
        }
    }

    fun deleteStaff(staff: Staff) {
        viewModelScope.launch {
            staffDaoFlow.collect { dao ->
                dao.deleteStaff(staff)
            }
        }
    }
}