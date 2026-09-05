package com.omismone.berryflow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category): Long

    @Insert
    suspend fun insertAll(categories: List<Category>)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    // User-editable categories only, excludes the Default category.
    @Query("SELECT * FROM categories WHERE isDefault = 0")
    fun getUserCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultCategory(): Category?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}