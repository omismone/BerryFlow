package com.omismone.berryflow.data

import java.time.LocalDate

enum class Frequency(val label: String) {
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    QUARTERLY("quarterly"),
    SEMI_ANNUAL("semi-annual"),
    YEARLY("yearly");

    fun nextOccurrenceAfter(date: LocalDate): LocalDate = when (this) {
        WEEKLY -> date.plusWeeks(1)
        MONTHLY -> date.plusMonths(1)
        QUARTERLY -> date.plusMonths(3)
        SEMI_ANNUAL -> date.plusMonths(6)
        YEARLY -> date.plusYears(1)
    }
}