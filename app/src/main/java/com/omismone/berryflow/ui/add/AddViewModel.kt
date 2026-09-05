package com.omismone.berryflow.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omismone.berryflow.data.BerryFlowRepository
import com.omismone.berryflow.data.Category
import com.omismone.berryflow.data.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// transactionId == null -> creating a new transaction.
// transactionId != null -> editing that existing transaction.
class AddViewModel(
    private val repository: BerryFlowRepository,
    private val transactionId: Long?
) : ViewModel() {

    val isEditMode: Boolean = transactionId != null

    val categories: StateFlow<List<Category>> = repository.userCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editingTransaction = MutableStateFlow<Transaction?>(null)
    val editingTransaction: StateFlow<Transaction?> = _editingTransaction

    init {
        if (transactionId != null) {
            viewModelScope.launch {
                _editingTransaction.value = repository.getTransactionById(transactionId)
            }
        }
    }

    fun saveTransaction(
        amount: Double,
        name: String,
        isIncome: Boolean,
        category: Category,
        dateMillis: Long
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                id = transactionId ?: 0,
                amount = amount,
                isIncome = isIncome,
                categoryId = category.id,
                date = dateMillis,
                name = name.trim().ifEmpty { null }
            )
            if (isEditMode) repository.updateTransaction(transaction)
            else repository.addTransaction(transaction)
        }
    }

    fun deleteTransaction() {
        val current = _editingTransaction.value ?: return
        viewModelScope.launch { repository.deleteTransaction(current) }
    }
}

class AddViewModelFactory(
    private val repository: BerryFlowRepository,
    private val transactionId: Long?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AddViewModel(repository, transactionId) as T
    }
}