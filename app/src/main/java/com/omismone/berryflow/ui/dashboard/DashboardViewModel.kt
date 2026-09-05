package com.omismone.berryflow.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omismone.berryflow.data.BerryFlowRepository
import com.omismone.berryflow.data.Category
import com.omismone.berryflow.data.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    repository: BerryFlowRepository
) : ViewModel() {

    val categories: StateFlow<List<Category>> = repository.userCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balance: StateFlow<Double> = repository.displayedBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}

class DashboardViewModelFactory(
    private val repository: BerryFlowRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DashboardViewModel(repository) as T
    }
}