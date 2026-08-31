package com.omismone.berryflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "balance")
data class Balance(
    @PrimaryKey
    val id: Int = 0, // always 0 -> there is only one row
    val amount: Double,
    val isSet: Boolean // false until the user set the balance on the first access
)