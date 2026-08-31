package com.omismone.berryflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val isIncome: Boolean,
    val categoryId: Long,
    val date: Long // epoch millis
)