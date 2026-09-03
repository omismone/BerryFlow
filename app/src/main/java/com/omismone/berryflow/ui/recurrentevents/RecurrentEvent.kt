package com.omismone.berryflow.ui.recurrentevents

import com.omismone.berryflow.data.Category
import java.time.LocalDate

// Temporary UI-layer model for a recurring transaction. Will likely become
// (or be backed by) a Room entity once persistence is wired up.
data class RecurrentEvent(
    val id: Long,
    val amount: Double,
    val isIncome: Boolean,
    val category: Category,
    val startDate: LocalDate,
    val frequency: Frequency
)

enum class Frequency(val label: String) {
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    QUARTERLY("quarterly"),
    SEMI_ANNUAL("semi-annual"),
    YEARLY("yearly")
}