package com.omismone.berryflow.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import androidx.room.withTransaction

class BerryFlowRepository(
    private val database: AppDatabase,
    private val categoryDao: CategoryDao,
    private val balanceDao: BalanceDao,
    private val transactionDao: TransactionDao,
    private val recurrentEventDao: RecurrentEventDao
) {
    val categories: Flow<List<Category>> = categoryDao.getAll()

    suspend fun ensureCategoriesSeeded() {
        if (categoryDao.count() == 0) {
            categoryDao.insert(CategorySeed.defaultCategory)
            categoryDao.insertAll(CategorySeed.baseCategories)
        }
    }

    suspend fun addCategory(category: Category): Long {
        return categoryDao.insert(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.update(category)
    }

    // Reassigns everything that references the category (transactions and
    // recurrent events) to Default and deletes it, atomically. The Default
    // category itself can't be deleted.
    suspend fun deleteCategory(category: Category) {
        database.withTransaction {
            val defaultCategory = categoryDao.getDefaultCategory() ?: return@withTransaction
            if (category.isDefault || category.id == defaultCategory.id) return@withTransaction
            transactionDao.reassignCategory(category.id, defaultCategory.id)
            recurrentEventDao.reassignCategory(category.id, defaultCategory.id)
            categoryDao.delete(category)
        }
    }

    // Points any transaction/recurrent event whose category no longer exists
    // at Default, so nothing is counted in totals but missing from the lists.
    suspend fun repairOrphanedCategoryReferences() {
        database.withTransaction { repairOrphansInTransaction() }
    }

    private suspend fun repairOrphansInTransaction() {
        // Default is the fallback for every record, so it must exist even if
        // a damaged or hand-edited backup left it out.
        val defaultCategory = categoryDao.getDefaultCategory()
            ?: CategorySeed.defaultCategory.let { it.copy(id = categoryDao.insert(it)) }
        transactionDao.reassignOrphaned(defaultCategory.id)
        recurrentEventDao.reassignOrphaned(defaultCategory.id)
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

    // Checks every recurrent event and creates any transactions that are
    // "due" (occurrence date <= today) but haven't been generated yet.
    // Runs on app startup no background scheduling, just catch-up on open.
    suspend fun generatePendingRecurrentTransactions() {
        val today = java.time.LocalDate.now()
        val zone = java.time.ZoneId.systemDefault()

        recurrentEventDao.getAllOnce().forEach { event ->
            val frequency = Frequency.valueOf(event.frequency)
            var nextDate = if (event.lastGeneratedDate != null) {
                frequency.nextOccurrenceAfter(millisToLocalDate(event.lastGeneratedDate, zone))
            } else {
                millisToLocalDate(event.startDate, zone)
            }

            val newTransactions = mutableListOf<Transaction>()
            var newLastGeneratedDate = event.lastGeneratedDate

            while (!nextDate.isAfter(today)) {
                newTransactions.add(
                    Transaction(
                        amount = event.amount,
                        isIncome = event.isIncome,
                        categoryId = event.categoryId,
                        date = localDateToMillis(nextDate, zone),
                        name = event.name
                    )
                )
                newLastGeneratedDate = localDateToMillis(nextDate, zone)
                nextDate = frequency.nextOccurrenceAfter(nextDate)
            }

            if (newTransactions.isNotEmpty()) {
                transactionDao.insertAll(newTransactions)
                recurrentEventDao.update(event.copy(lastGeneratedDate = newLastGeneratedDate))
            }
        }
    }

    private fun millisToLocalDate(millis: Long, zone: java.time.ZoneId): java.time.LocalDate =
        java.time.Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    private fun localDateToMillis(date: java.time.LocalDate, zone: java.time.ZoneId): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    suspend fun exportDataAsJson(): String {
        return buildBackupJson(
            categories = categoryDao.getAllOnce(),
            transactions = transactionDao.getAllOnce(),
            recurrentEvents = recurrentEventDao.getAllOnce(),
            balance = balanceDao.getOnce()
        )
    }

    // Replaces everything atomically: either the whole import succeeds, or
    // (on a malformed file / parse error) nothing is touched.
    suspend fun importDataFromJson(json: String) {
        val parsed = parseBackupJson(json)
        database.withTransaction {
            categoryDao.deleteAll()
            transactionDao.deleteAll()
            recurrentEventDao.deleteAll()
            balanceDao.deleteAll()

            categoryDao.insertAll(parsed.categories)
            transactionDao.insertAll(parsed.transactions)
            recurrentEventDao.insertAll(parsed.recurrentEvents)
            parsed.balance?.let { balanceDao.upsert(it) }
            repairOrphansInTransaction()
        }
    }

    suspend fun eraseAllData() {
        database.withTransaction {
            categoryDao.deleteAll()
            transactionDao.deleteAll()
            recurrentEventDao.deleteAll()
            balanceDao.deleteAll()
        }
        // Without this the app would be left with no categories at all.
        ensureCategoriesSeeded()
    }
}