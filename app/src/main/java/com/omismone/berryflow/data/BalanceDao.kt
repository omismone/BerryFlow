package com.omismone.berryflow.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BalanceDao {
    @Upsert
    suspend fun upsert(balance: Balance)

    @Query("SELECT * FROM balance WHERE id = 0")
    fun get(): Flow<Balance?>

    @Query("SELECT * FROM balance WHERE id = 0")
    suspend fun getOnce(): Balance?

    @Query("DELETE FROM balance")
    suspend fun deleteAll()
}