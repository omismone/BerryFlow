package com.omismone.berryflow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    @Query("SELECT * FROM transactions ORDER BY date DESC, id ASC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT COALESCE(SUM(CASE WHEN isIncome THEN amount ELSE -amount END), 0.0) FROM transactions")
    suspend fun getNetTotal(): Double

    // Used when a category is deleted: moves its transactions to Default.
    @Query("UPDATE transactions SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignCategory(oldCategoryId: Long, newCategoryId: Long)
}