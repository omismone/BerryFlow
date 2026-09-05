package com.omismone.berryflow.ui.recurrentevents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omismone.berryflow.data.BerryFlowRepository
import com.omismone.berryflow.data.Category
import com.omismone.berryflow.data.RecurrentEvent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecurrentEventsListViewModel(
    private val repository: BerryFlowRepository
) : ViewModel() {
    val categories: StateFlow<List<Category>> = repository.userCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val events: StateFlow<List<RecurrentEvent>> = repository.allRecurrentEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteEvent(event: RecurrentEvent) {
        viewModelScope.launch { repository.deleteRecurrentEvent(event) }
    }
}

class RecurrentEventsListViewModelFactory(
    private val repository: BerryFlowRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RecurrentEventsListViewModel(repository) as T
    }
}