package com.omismone.berryflow.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.omismone.berryflow.data.BerryFlowRepository
import com.omismone.berryflow.data.Category
import com.omismone.berryflow.data.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// transactionId == null -> creating a new transaction.
// transactionId != null -> editing that existing transaction.
class AddViewModel(
    private val repository: BerryFlowRepository,
    private val transactionId: Long?
) : ViewModel() {

    val isEditMode: Boolean = transactionId != null

    private val _editingTransaction = MutableStateFlow<Transaction?>(null)
    val editingTransaction: StateFlow<Transaction?> = _editingTransaction

    init {
        if (transactionId != null) {
            viewModelScope.launch {
                _editingTransaction.value = repository.getTransactionById(transactionId)
            }
        }
    }

    // Persists the draft. Returns false (and writes nothing) if the amount
    // isn't a positive, finite number. Suspends until the write is done, so
    // the caller can close the screen knowing the data is stored.
    suspend fun saveTransaction(
        amount: Double,
        name: String,
        isIncome: Boolean,
        category: Category,
        dateMillis: Long
    ): Boolean {
        if (!amount.isFinite() || amount <= 0.0) return false
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
        return true
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