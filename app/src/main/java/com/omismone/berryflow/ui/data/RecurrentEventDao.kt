package com.omismone.berryflow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurrentEventDao {
    @Insert
    suspend fun insert(event: RecurrentEvent): Long

    @Update
    suspend fun update(event: RecurrentEvent)

    @Delete
    suspend fun delete(event: RecurrentEvent)

    @Query("SELECT * FROM recurrent_events WHERE id = :id")
    suspend fun getById(id: Long): RecurrentEvent?

    @Query("SELECT * FROM recurrent_events ORDER BY id ASC")
    fun getAll(): Flow<List<RecurrentEvent>>
}