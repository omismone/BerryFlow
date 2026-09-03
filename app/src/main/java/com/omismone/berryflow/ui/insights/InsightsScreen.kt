package com.omismone.berryflow.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omismone.berryflow.data.Category
import com.omismone.berryflow.data.Transaction
import com.omismone.berryflow.ui.theme.TopBarButtonPadding
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

private val SecondaryTextColor = Color(0xFF9E9E9E)
private val NegativeColor = Color(0xFFE53935)
private val PositiveColor = Color(0xFF43A047)
private val BorderColor = Color(0xFFE0E0E0)

private enum class InsightsPeriod(val label: String) {
    WEEK("week"),
    MONTH("month"),
    YEAR("year")
}

@Composable
fun InsightsScreen(
    categories: List<Category>,
    transactions: List<Transaction>,
    onHomeClick: () -> Unit
) {
    var period by remember { mutableStateOf(InsightsPeriod.MONTH) }
    var showPeriodMenu by remember { mutableStateOf(false) }

    val categoriesById = categories.associateBy { it.id }

    // Sort once by date ascending, so "first appearance" order within a
    // period can be derived just by walking this list in order.
    val sortedTransactions = transactions.sortedBy { it.date }

    val groups: List<PeriodGroup> = when (period) {
        InsightsPeriod.WEEK -> groupByWeek(sortedTransactions)
        InsightsPeriod.MONTH -> groupByMonth(sortedTransactions)
        InsightsPeriod.YEAR -> groupByYear(sortedTransactions)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = TopBarButtonPadding)
            ) {
                IconButton(
                    onClick = onHomeClick,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Back to Dashboard",
                        tint = SecondaryTextColor,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "insights",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                        .clickable { showPeriodMenu = true }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = SecondaryTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = period.label + " ", color = SecondaryTextColor, fontSize = 14.sp)
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = SecondaryTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(
                    expanded = showPeriodMenu,
                    onDismissRequest = { showPeriodMenu = false }
                ) {
                    InsightsPeriod.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                period = option
                                showPeriodMenu = false
                            }
                        )
                    }
                }
            }
        }

        groups.forEach { group ->
            item {
                PeriodHeader(group)
            }
            items(group.categoryTotals) { categoryTotal ->
                val category = categoriesById[categoryTotal.categoryId]
                if (category != null) {
                    CategoryTotalRow(category = category, netAmount = categoryTotal.netAmount)
                }
            }
        }
    }
}

@Composable
private fun PeriodHeader(group: PeriodGroup) {
    val (totalText, totalColor) = formatSignedTotal(group.netTotal)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = group.label, color = SecondaryTextColor, fontSize = 16.sp)
            Text(text = totalText, color = totalColor, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderColor)
        )
    }
}

@Composable
private fun CategoryTotalRow(category: Category, netAmount: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(category.color).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = category.emoji, fontSize = 21.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = category.name.lowercase(), color = Color.Black, fontSize = 17.sp)
        }

        Text(text = formatSignedAmount(netAmount), color = Color.Black, fontSize = 17.sp)
    }
}

// --- Grouping logic ---

private data class CategoryTotal(val categoryId: Long, val netAmount: Double)
private data class PeriodGroup(val label: String, val netTotal: Double, val categoryTotals: List<CategoryTotal>)

private fun Transaction.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()

private fun Transaction.signedAmount(): Double = if (isIncome) amount else -amount

// Builds one PeriodGroup per distinct key, with categories ordered by their
// first appearance within that period (transactions passed in must already
// be sorted by date ascending).
private fun buildGroups(
    transactions: List<Transaction>,
    keyOf: (LocalDate) -> Comparable<*>,
    labelOf: (LocalDate) -> String
): List<PeriodGroup> {
    val byKey = transactions.groupBy { keyOf(it.toLocalDate()) }
    return byKey.entries
        .sortedByDescending { it.key.toString() } // keys are formatted so string-sort matches chronological order
        .map { (_, txs) ->
            val label = labelOf(txs.first().toLocalDate())
            val netTotal = txs.sumOf { it.signedAmount() }
            val categoryOrder = LinkedHashSet<Long>()
            txs.forEach { categoryOrder.add(it.categoryId) }
            val categoryTotals = categoryOrder.map { categoryId ->
                CategoryTotal(
                    categoryId = categoryId,
                    netAmount = txs.filter { it.categoryId == categoryId }.sumOf { it.signedAmount() }
                )
            }
            PeriodGroup(label = label, netTotal = netTotal, categoryTotals = categoryTotals)
        }
}

private fun groupByMonth(transactions: List<Transaction>): List<PeriodGroup> =
    buildGroups(
        transactions,
        keyOf = { "%04d-%02d".format(it.year, it.monthValue) },
        labelOf = { "${it.month.getDisplayName(JavaTextStyle.FULL, Locale.ENGLISH)} ${it.year}" }
    )

private fun groupByYear(transactions: List<Transaction>): List<PeriodGroup> =
    buildGroups(
        transactions,
        keyOf = { "%04d".format(it.year) },
        labelOf = { "${it.year}" }
    )

private fun mondayOf(date: LocalDate): LocalDate =
    date.minusDays((date.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())

private fun groupByWeek(transactions: List<Transaction>): List<PeriodGroup> =
    buildGroups(
        transactions,
        keyOf = { val monday = mondayOf(it); "%04d-%02d-%02d".format(monday.year, monday.monthValue, monday.dayOfMonth) },
        labelOf = {
            val monday = mondayOf(it)
            val weekNumber = ((monday.dayOfMonth - 1) / 7) + 1
            val monthName = monday.month.getDisplayName(JavaTextStyle.FULL, Locale.ENGLISH)
            "Week $weekNumber, $monthName ${monday.year}"
        }
    )

// --- Formatting helpers ---

private fun formatSignedAmount(amount: Double): String {
    val sign = if (amount >= 0) "+" else "-"
    return "$sign ${String.format(Locale.US, "%.2f", kotlin.math.abs(amount))} €"
}

private fun formatSignedTotal(amount: Double): Pair<String, Color> {
    val text = formatSignedAmount(amount)
    val color = if (amount >= 0) PositiveColor else NegativeColor
    return text to color
}