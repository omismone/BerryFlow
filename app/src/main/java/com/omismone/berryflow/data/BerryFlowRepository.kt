package com.omismone.berryflow.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

class BerryFlowRepository(
    private val categoryDao: CategoryDao,
    private val balanceDao: BalanceDao,
    private val transactionDao: TransactionDao,
    private val recurrentEventDao: RecurrentEventDao
) {
    val userCategories: Flow<List<Category>> = categoryDao.getUserCategories()

    suspend fun ensureCategoriesSeeded() {
        if (categoryDao.count() == 0) {
            categoryDao.insert(CategorySeed.defaultCategory)
            categoryDao.insertAll(CategorySeed.baseCategories)
        }
    }

    suspend fun addCategory(category: Category) {
        categoryDao.insert(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(category: Category) {
        val defaultCategory = categoryDao.getDefaultCategory()
        if (defaultCategory != null) {
            transactionDao.reassignCategory(category.id, defaultCategory.id)
        }
        categoryDao.delete(category)
    }

    // Raw manually-set base amount, without transactions factored in.
    private val baseBalance: Flow<Double> = balanceDao.get().map { it?.amount ?: 0.0 }

    val isBalanceSet: Flow<Boolean> = balanceDao.get().map { it?.isSet ?: false }

    // What's actually shown everywhere: base + all income - all expenses.
    val displayedBalance: Flow<Double> = combine(baseBalance, transactionDao.getAll()) { base, transactions ->
        base + transactions.sumOf { if (it.isIncome) it.amount else -it.amount }
    }

    // Used by Adjust Balance: the user types the TOTAL they want to see, not
    // the internal base amount. We back-calculate the base so that
    // base + current transactions total = the amount they entered, instead
    // of resetting as if this were a brand-new starting point (existing
    // transactions are untouched and keep counting).
    suspend fun setDisplayedBalance(desiredTotal: Double) {
        val transactionsNet = transactionDao.getNetTotal()
        val newBase = desiredTotal - transactionsNet
        balanceDao.upsert(Balance(amount = newBase, isSet = true))
    }

    val allTransactions: Flow<List<Transaction>> = transactionDao.getAll()

    suspend fun getTransactionById(id: Long): Transaction? = transactionDao.getById(id)

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    val allRecurrentEvents: Flow<List<RecurrentEvent>> = recurrentEventDao.getAll()

    suspend fun getRecurrentEventById(id: Long): RecurrentEvent? = recurrentEventDao.getById(id)

    suspend fun addRecurrentEvent(event: RecurrentEvent) {
        recurrentEventDao.insert(event)
    }

    suspend fun updateRecurrentEvent(event: RecurrentEvent) {
        recurrentEventDao.update(event)
    }

    suspend fun deleteRecurrentEvent(event: RecurrentEvent) {
        recurrentEventDao.delete(event)
    }
}