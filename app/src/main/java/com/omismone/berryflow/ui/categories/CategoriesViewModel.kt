package com.omismone.berryflow.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omismone.berryflow.data.BerryFlowRepository
import com.omismone.berryflow.data.Category
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val repository: BerryFlowRepository
) : ViewModel() {

    val categories: StateFlow<List<Category>> = repository.userCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(category: Category) {
        viewModelScope.launch { repository.addCategory(category) }
    }

    fun renameCategory(category: Category, newName: String) {
        viewModelScope.launch { repository.updateCategory(category.copy(name = newName)) }
    }

    fun recolorCategory(category: Category, newColor: Int) {
        viewModelScope.launch { repository.updateCategory(category.copy(color = newColor)) }
    }

    fun reemojiCategory(category: Category, newEmoji: String) {
        viewModelScope.launch { repository.updateCategory(category.copy(emoji = newEmoji)) }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }
}

class CategoriesViewModelFactory(
    private val repository: BerryFlowRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CategoriesViewModel(repository) as T
    }
}