package com.omismone.berryflow.ui.recurrentevents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omismone.berryflow.data.BerryFlowRepository
import com.omismone.berryflow.data.Category
import com.omismone.berryflow.data.Frequency
import com.omismone.berryflow.data.RecurrentEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// eventId == null -> creating a new recurrent event.
// eventId != null -> editing that existing event.
class RecurrentEventsViewModel(
    private val repository: BerryFlowRepository,
    private val eventId: Long?
) : ViewModel() {

    val isEditMode: Boolean = eventId != null

    val categories: StateFlow<List<Category>> = repository.userCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editingEvent = MutableStateFlow<RecurrentEvent?>(null)
    val editingEvent: StateFlow<RecurrentEvent?> = _editingEvent

    init {
        if (eventId != null) {
            viewModelScope.launch {
                _editingEvent.value = repository.getRecurrentEventById(eventId)
            }
        }
    }

    fun saveEvent(
        amount: Double,
        name: String,
        isIncome: Boolean,
        category: Category,
        startDateMillis: Long,
        frequency: Frequency
    ) {
        viewModelScope.launch {
            val event = RecurrentEvent(
                id = eventId ?: 0,
                amount = amount,
                isIncome = isIncome,
                categoryId = category.id,
                startDate = startDateMillis,
                frequency = frequency.name,
                name = name.trim().ifEmpty { null }
            )
            if (isEditMode) repository.updateRecurrentEvent(event)
            else repository.addRecurrentEvent(event)
        }
    }

    fun deleteEvent() {
        val current = _editingEvent.value ?: return
        viewModelScope.launch { repository.deleteRecurrentEvent(current) }
    }
}

class RecurrentEventsViewModelFactory(
    private val repository: BerryFlowRepository,
    private val eventId: Long?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RecurrentEventsViewModel(repository, eventId) as T
    }
}