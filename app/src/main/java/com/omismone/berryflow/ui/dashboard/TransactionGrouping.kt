package com.omismone.berryflow.ui.dashboard

import com.omismone.berryflow.data.Transaction
import com.omismone.berryflow.ui.sortedByMagnitudeDescending
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

// Time frequencies the Dashboard can group its transactions by.
enum class DashboardPeriod(val label: String) {
    DAY("day"),
    WEEK("week"),
    MONTH("month"),
    YEAR("year")
}

// One period (a day, week, month or year) with its net total and its
// transactions, largest amount first.
data class TransactionGroup(
    val label: String,
    val netTotal: Double,
    val transactions: List<Transaction>
)

// Single source for what the Dashboard shows: the totals and the lists of every
// group are computed here from the same filtered transactions, so a
// transaction can't count in a total yet be missing from the list.
// categoryId == null means all categories.
fun groupTransactions(
    transactions: List<Transaction>,
    period: DashboardPeriod,
    categoryId: Long? = null,
    zone: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(zone)
): List<TransactionGroup> {
    val visible = if (categoryId == null) transactions else transactions.filter { it.categoryId == categoryId }

    return visible
        .groupBy { period.startOf(it.toLocalDate(zone)) }
        .toSortedMap(compareByDescending { it }) // most recent period first
        .map { (start, periodTransactions) ->
            TransactionGroup(
                label = period.labelFor(start, today),
                netTotal = periodTransactions.sumOf { it.signedAmount() },
                // Within a period: largest amount first, ties in insertion order.
                transactions = periodTransactions
                    .sortedBy { it.id }
                    .sortedByMagnitudeDescending { it.amount }
            )
        }
}

fun Transaction.signedAmount(): Double = if (isIncome) amount else -amount

fun Transaction.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(date).atZone(zone).toLocalDate()

// First day of the period the date belongs to (weeks start on Monday).
private fun DashboardPeriod.startOf(date: LocalDate): LocalDate = when (this) {
    DashboardPeriod.DAY -> date
    DashboardPeriod.WEEK -> date.minusDays((date.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
    DashboardPeriod.MONTH -> date.withDayOfMonth(1)
    DashboardPeriod.YEAR -> date.withDayOfYear(1)
}

private fun DashboardPeriod.labelFor(start: LocalDate, today: LocalDate): String = when (this) {
    DashboardPeriod.DAY -> formatDayLabel(start, today)
    DashboardPeriod.WEEK -> {
        val weekNumber = ((start.dayOfMonth - 1) / 7) + 1
        "Week $weekNumber, ${start.month.getDisplayName(JavaTextStyle.FULL, Locale.ENGLISH)} ${start.year}"
    }
    DashboardPeriod.MONTH -> "${start.month.getDisplayName(JavaTextStyle.FULL, Locale.ENGLISH)} ${start.year}"
    DashboardPeriod.YEAR -> "${start.year}"
}

fun formatDayLabel(date: LocalDate, today: LocalDate = LocalDate.now()): String {
    val monthAbbreviation = date.month.getDisplayName(JavaTextStyle.SHORT, Locale.ENGLISH)
    return when (date) {
        today -> "Today, ${date.dayOfMonth} $monthAbbreviation"
        today.minusDays(1) -> "Yesterday, ${date.dayOfMonth} $monthAbbreviation"
        else -> "${date.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.ENGLISH)}, ${date.dayOfMonth} $monthAbbreviation"
    }
}