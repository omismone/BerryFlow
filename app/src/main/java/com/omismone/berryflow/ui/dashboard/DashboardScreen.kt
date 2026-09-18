package com.omismone.berryflow.ui.dashboard

import com.omismone.berryflow.ui.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omismone.berryflow.data.Category
import com.omismone.berryflow.data.Transaction
import com.omismone.berryflow.ui.theme.topBarInsets
import com.omismone.berryflow.ui.theme.clickableNoRipple
import java.util.Locale
import androidx.compose.foundation.shape.RoundedCornerShape

// Colors used for negative/positive daily balances

// Balance, categories and transactions are null until the database has
// delivered them. Nothing but the background is drawn until then, so the
// Dashboard never shows placeholder values (a 0.00 balance, "no transactions")
// in place of the real data.
@Composable
fun DashboardScreen(
    balance: Double?,
    categories: List<Category>?,
    transactions: List<Transaction>?,
    onAddClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onRecurrentEventsClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onAdjustBalanceClick: () -> Unit,
    onManageDataClick: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    balanceHidden: Boolean,
    onToggleBalanceHidden: () -> Unit
) {
    if (balance == null || categories == null || transactions == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
        )
        return
    }

    DashboardContent(
        balance = balance,
        categories = categories,
        transactions = transactions,
        onAddClick = onAddClick,
        onTransactionClick = onTransactionClick,
        onRecurrentEventsClick = onRecurrentEventsClick,
        onCategoriesClick = onCategoriesClick,
        onAdjustBalanceClick = onAdjustBalanceClick,
        onManageDataClick = onManageDataClick,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme,
        balanceHidden = balanceHidden,
        onToggleBalanceHidden = onToggleBalanceHidden
    )
}

@Composable
private fun DashboardContent(
    balance: Double,
    categories: List<Category>,
    transactions: List<Transaction>,
    onAddClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onRecurrentEventsClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onAdjustBalanceClick: () -> Unit,
    onManageDataClick: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    balanceHidden: Boolean,
    onToggleBalanceHidden: () -> Unit
) {
    val categoriesById = categories.associateBy { it.id }
    val defaultCategory = categories.firstOrNull { it.isDefault }

    var period by rememberSaveable { mutableStateOf(DashboardPeriod.DAY) }
    var filterCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    val filterCategory = resolveCategoryFilter(filterCategoryId, categories)

    // A filter on a category that no longer exists is dropped.
    LaunchedEffect(filterCategoryId, categories) {
        if (filterCategoryId != null && categories.isNotEmpty() && filterCategory == null) {
            filterCategoryId = null
        }
    }

    // Most recent period first. The list and every total come from the same
    // groups, which already apply the period and the category filter.
    val groups = remember(transactions, period, filterCategory?.id) {
        groupTransactions(transactions, period, filterCategory?.id)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        // The top bar spans the full width like on the other screens; the
        // other items add the horizontal content margin themselves.
        contentPadding = PaddingValues(
            bottom = 8.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        item {
            DashboardTopBar(
                balance = balance,
                onAddClick = onAddClick,
                onRecurrentEventsClick = onRecurrentEventsClick,
                onCategoriesClick = onCategoriesClick,
                onAdjustBalanceClick = onAdjustBalanceClick,
                onManageDataClick = onManageDataClick,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                balanceHidden = balanceHidden,
                onToggleBalanceHidden = onToggleBalanceHidden
            )
        }

        item {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PeriodSelector(
                    period = period,
                    onPeriodChange = { period = it }
                )
                if (filterCategory != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CategoryFilterChip(
                        category = filterCategory,
                        onClear = { filterCategoryId = null },
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }

        if (groups.isEmpty()) {
            item {
                Text(
                    text = if (filterCategory != null) {
                        "No transactions in ${filterCategory.name.lowercase()}"
                    } else {
                        "No transactions yet"
                    },
                    color = AppTheme.colors.secondaryText,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 40.dp)
                )
            }
        }

        groups.forEach { group ->
            item(key = "header-${period.name}-${group.label}") {
                PeriodHeader(label = group.label, netTotal = group.netTotal)
            }

            items(group.transactions, key = { it.id }) { transaction ->
                val category = categoriesById[transaction.categoryId] ?: defaultCategory

                if (category != null) {
                    TransactionRow(
                        transaction = transaction,
                        category = category,
                        // A day header already tells the date; longer periods don't.
                        showDate = period != DashboardPeriod.DAY,
                        onTransactionClick = { onTransactionClick(transaction) },
                        onCategoryClick = {
                            filterCategoryId = toggleCategoryFilter(filterCategoryId, category.id)
                        }
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
    onRecurrentEventsClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onAdjustBalanceClick: () -> Unit,
    onManageDataClick: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    balanceHidden: Boolean,
    onToggleBalanceHidden: () -> Unit
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
                .topBarInsets(),
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
                        tint = AppTheme.colors.secondaryText,
                        modifier = Modifier.size(25.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
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

                    // Shows the theme you would switch to.
                    DropdownMenuItem(
                        text = { Text(if (isDarkTheme) "light theme" else "dark theme", fontSize = 16.sp) },
                        leadingIcon = { Text(if (isDarkTheme) "☀️" else "🌙") },
                        onClick = {
                            showMenu = false
                            onToggleTheme()
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
                    tint = AppTheme.colors.secondaryText,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tapping the balance hides it (or shows it again); the choice is
        // remembered. Only the text changes, so the layout stays the same.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = balanceText(balance, balanceHidden),
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.primaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clickableNoRipple(
                        onClickLabel = if (balanceHidden) "Show balance" else "Hide balance",
                        onClick = onToggleBalanceHidden
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun PeriodSelector(
    period: DashboardPeriod,
    onPeriodChange: (DashboardPeriod) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(8.dp))
                .clickable { showMenu = true }
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = AppTheme.colors.secondaryText,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = period.label + " ", color = AppTheme.colors.secondaryText, fontSize = 14.sp)
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = AppTheme.colors.secondaryText,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DashboardPeriod.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onPeriodChange(option)
                        showMenu = false
                    }
                )
            }
        }
    }
}

// Shown next to the period selector while a category filter is active.
// Tapping it removes the filter.
@Composable
private fun CategoryFilterChip(
    category: Category,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(category.color).copy(alpha = 0.25f))
            .clickableNoRipple(onClickLabel = "Remove category filter", onClick = onClear)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = category.emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = category.name.lowercase(),
            color = AppTheme.colors.primaryText,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove category filter",
            tint = AppTheme.colors.primaryText,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun PeriodHeader(
    label: String,
    netTotal: Double
) {
    val (balanceText, balanceColor) = formatDailyBalance(netTotal, AppTheme.colors.income, AppTheme.colors.expense)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = AppTheme.colors.secondaryText,
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
            .background(AppTheme.colors.border)
    )
}

@Composable
private fun TransactionRow(
    transaction: Transaction,
    category: Category,
    showDate: Boolean,
    onTransactionClick: () -> Unit,
    onCategoryClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // The category square (emoji on its color) filters by that category.
        // The 40dp square sits in a 48dp touch target (shifted so the square
        // keeps its position) to meet the minimum touch size.
        Box(
            modifier = Modifier
                .offset(x = (-4).dp)
                .size(48.dp)
                .clickableNoRipple(
                    onClickLabel = "Filter by ${category.name.lowercase()}",
                    onClick = onCategoryClick
                ),
            contentAlignment = Alignment.Center
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
        }

        Spacer(modifier = Modifier.width(4.dp))

        // The transaction text (up to the amount) opens the transaction.
        Column(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .clickableNoRipple(onClickLabel = "Edit transaction", onClick = onTransactionClick),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = transaction.name?.takeIf { it.isNotBlank() } ?: category.name.lowercase(),
                color = AppTheme.colors.primaryText,
                fontSize = 17.sp
            )
            if (showDate) {
                Text(
                    text = formatDayLabel(transaction.toLocalDate()),
                    color = AppTheme.colors.secondaryText,
                    fontSize = 13.sp
                )
            }
        }

        Text(
            text = formatSignedAmount(
                transaction.amount,
                transaction.isIncome
            ),
            color = AppTheme.colors.primaryText,
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
    netAmount: Double,
    positiveColor: Color,
    negativeColor: Color
): Pair<String, Color> {
    val sign = if (netAmount >= 0) "+" else "-"
    val text = "$sign ${formatPlainAmount(kotlin.math.abs(netAmount))} €"
    val color = if (netAmount >= 0) positiveColor else negativeColor

    return text to color
}