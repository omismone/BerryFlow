package com.omismone.berryflow.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omismone.berryflow.data.BerryFlowRepository
import com.omismone.berryflow.data.Category
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// Single shared source of the category list, created once for the whole
// app instead of per-screen, so screens reached via animated navigation
// (like Add) don't wait on a fresh Room query before rendering - this was
// causing a visible stutter on the enter animation.
class AppViewModel(repository: BerryFlowRepository) : ViewModel() {
    val categories: StateFlow<List<Category>> = repository.userCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class AppViewModelFactory(private val repository: BerryFlowRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AppViewModel(repository) as T
    }
}