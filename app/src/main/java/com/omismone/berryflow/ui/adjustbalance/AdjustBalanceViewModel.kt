package com.omismone.berryflow.ui.adjustbalance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omismone.berryflow.data.BerryFlowRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdjustBalanceViewModel(
    private val repository: BerryFlowRepository
) : ViewModel() {
    val currentBalance: StateFlow<Double> = repository.displayedBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun saveBalance(amount: Double) {
        viewModelScope.launch { repository.setDisplayedBalance(amount) }
    }
}

class AdjustBalanceViewModelFactory(
    private val repository: BerryFlowRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AdjustBalanceViewModel(repository) as T
    }
}