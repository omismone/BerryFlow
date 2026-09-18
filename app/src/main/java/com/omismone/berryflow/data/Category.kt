package com.omismone.berryflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Int,
    val emoji: String,
    // True only for the single "Default" category that transactions fall back
    // to when their category is deleted. It is editable like any other
    // category, but it can never be deleted.
    val isDefault: Boolean = false
)