package com.omismone.berryflow.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omismone.berryflow.data.Category
import com.omismone.berryflow.data.Transaction
import com.omismone.berryflow.ui.theme.TopBarButtonPadding
import com.omismone.berryflow.ui.theme.clickableNoRipple
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale
import androidx.compose.foundation.shape.RoundedCornerShape

// Colors used for negative/positive daily balances
private val NegativeColor = Color(0xFFE53935)
private val PositiveColor = Color(0xFF43A047)
private val SecondaryTextColor = Color(0xFF9E9E9E)

@Composable
fun DashboardScreen(
    balance: Double,
    categories: List<Category>,
    transactions: List<Transaction>,
    onAddClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onInsightsClick: () -> Unit,
    onRecurrentEventsClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onAdjustBalanceClick: () -> Unit,
    onManageDataClick: () -> Unit
) {
    val categoriesById = categories.associateBy { it.id }

    // Group transactions by day, most recent day first.
    // Within a day, keep insertion order (id ascending), as required.
    val transactionsByDay: List<Pair<LocalDate, List<Transaction>>> = transactions
        .groupBy { it.toLocalDate() }
        .toSortedMap(compareByDescending { it })
        .map { (date, dayTransactions) ->
            date to dayTransactions.sortedBy { it.id }
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        item {
            DashboardTopBar(
                balance = balance,
                onAddClick = onAddClick,
                onInsightsClick = onInsightsClick,
                onRecurrentEventsClick = onRecurrentEventsClick,
                onCategoriesClick = onCategoriesClick,
                onAdjustBalanceClick = onAdjustBalanceClick,
                onManageDataClick = onManageDataClick
            )
        }

        transactionsByDay.forEach { (date, dayTransactions) ->
            item {
                DayHeader(
                    date = date,
                    transactions = dayTransactions
                )
            }

            items(dayTransactions) { transaction ->
                val category = categoriesById[transaction.categoryId]

                if (category != null) {
                    TransactionRow(
                        transaction = transaction,
                        category = category,
                        onClick = { onTransactionClick(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardTopBar(
    balance: Double,
    onAddClick: () -> Unit,
    onInsightsClick: () -> Unit,
    onRecurrentEventsClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onAdjustBalanceClick: () -> Unit,
    onManageDataClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = TopBarButtonPadding
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                IconButton(
                    onClick = { showMenu = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = SecondaryTextColor,
                        modifier = Modifier.size(25.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("insights", fontSize = 16.sp) },
                        leadingIcon = { Text("📊") },
                        onClick = {
                            showMenu = false
                            onInsightsClick()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("recurrent events", fontSize = 16.sp) },
                        leadingIcon = { Text("🔁") },
                        onClick = {
                            showMenu = false
                            onRecurrentEventsClick()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("categories", fontSize = 16.sp) },
                        leadingIcon = { Text("🏷️") },
                        onClick = {
                            showMenu = false
                            onCategoriesClick()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("adjust balance", fontSize = 16.sp) },
                        leadingIcon = { Text("💰") },
                        onClick = {
                            showMenu = false
                            onAdjustBalanceClick()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("manage data", fontSize = 16.sp) },
                        leadingIcon = { Text("📁") },
                        onClick = {
                            showMenu = false
                            onManageDataClick()
                        }
                    )
                }
            }

            IconButton(
                onClick = onAddClick
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = SecondaryTextColor,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "€ ${formatPlainAmount(balance)}",
            fontSize = 50.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DayHeader(
    date: LocalDate,
    transactions: List<Transaction>
) {
    val netBalance = transactions.sumOf {
        if (it.isIncome) it.amount else -it.amount
    }

    val (balanceText, balanceColor) = formatDailyBalance(netBalance)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDayLabel(date),
                color = SecondaryTextColor,
                fontSize = 16.sp
            )

            Text(
                text = balanceText,
                color = balanceColor,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        HorizontalDividerLine(
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HorizontalDividerLine(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(1.dp)
            .background(Color(0xFFE0E0E0))
    )
}

@Composable
private fun TransactionRow(
    transaction: Transaction,
    category: Category,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickableNoRipple(onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Color(category.color).copy(alpha = 0.25f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.emoji,
                    fontSize = 21.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = category.name.lowercase(),
                color = Color.Black,
                fontSize = 17.sp
            )
        }

        Text(
            text = formatSignedAmount(
                transaction.amount,
                transaction.isIncome
            ),
            color = Color.Black,
            fontSize = 17.sp
        )
    }
}

// --- Formatting helpers ---

private fun formatPlainAmount(amount: Double): String {
    return String.format(Locale.US, "%.2f", amount)
}

private fun formatSignedAmount(
    amount: Double,
    isIncome: Boolean
): String {
    val sign = if (isIncome) "+" else "-"
    return "$sign ${formatPlainAmount(amount)} €"
}

private fun formatDailyBalance(
    netAmount: Double
): Pair<String, Color> {
    val sign = if (netAmount >= 0) "+" else "-"
    val text = "$sign ${formatPlainAmount(kotlin.math.abs(netAmount))} €"
    val color = if (netAmount >= 0) {
        PositiveColor
    } else {
        NegativeColor
    }

    return text to color
}

private fun formatDayLabel(date: LocalDate): String {
    val today = LocalDate.now()

    return when (date) {
        today -> "Today, ${date.dayOfMonth} ${date.monthAbbreviation()}"

        today.minusDays(1) ->
            "Yesterday, ${date.dayOfMonth} ${date.monthAbbreviation()}"

        else ->
            "${date.dayOfWeekAbbreviation()}, ${date.dayOfMonth} ${date.monthAbbreviation()}"
    }
}

private fun LocalDate.monthAbbreviation(): String =
    month.getDisplayName(
        JavaTextStyle.SHORT,
        Locale.ENGLISH
    )

private fun LocalDate.dayOfWeekAbbreviation(): String =
    dayOfWeek.getDisplayName(
        JavaTextStyle.SHORT,
        Locale.ENGLISH
    )

private fun Transaction.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()