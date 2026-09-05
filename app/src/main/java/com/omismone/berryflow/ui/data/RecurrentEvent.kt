package com.omismone.berryflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurrent_events")
data class RecurrentEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val isIncome: Boolean,
    val categoryId: Long,
    val startDate: Long, // epoch millis, day only (execution assumed 9:00 AM, not yet implemented)
    // Stored as the Frequency enum's name (e.g. "WEEKLY") to avoid adding a
    // Room TypeConverter just for one enum field.
    val frequency: String,
    val name: String? = null
)