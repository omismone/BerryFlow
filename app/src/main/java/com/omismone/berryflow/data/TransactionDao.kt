package com.omismone.berryflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Query("SELECT * FROM transactions ORDER BY date DESC, id ASC")
    fun getAll(): Flow<List<Transaction>>
}