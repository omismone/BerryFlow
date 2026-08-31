package com.omismone.berryflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.omismone.berryflow.data.Category
import com.omismone.berryflow.data.Transaction
import com.omismone.berryflow.ui.dashboard.DashboardScreen
import com.omismone.berryflow.ui.theme.Typography
import java.time.LocalDate
import java.time.ZoneId
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        setContent {
            MaterialTheme(typography = Typography) {
                DashboardScreen(
                    balance = fakeBalance,
                    categories = fakeCategories,
                    transactions = fakeTransactions,
                    onMenuClick = {},
                    onAddClick = {},
                    onTransactionClick = {}
                )
            }
        }
    }
}

// Temporary fake data, used only to preview the Dashboard layout.
// Will be replaced once Room data and the Categories screen are wired up.

private val fakeBalance = 2323.20

private val fakeCategories = listOf(
    Category(id = 1, name = "groceries", color = 0xFFF48FB1.toInt(), emoji = "🛍️"),
    Category(id = 2, name = "fuel", color = 0xFFE57373.toInt(), emoji = "⛽"),
    Category(id = 3, name = "gifts", color = 0xFFFFB74D.toInt(), emoji = "🎁"),
    Category(id = 4, name = "paycheck", color = 0xFF64B5F6.toInt(), emoji = "💰")
)

private fun epochMillisFor(date: LocalDate): Long =
    date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private val today = LocalDate.now()

private val fakeTransactions = buildList {
    var id = 1L
    for (dayOffset in 0..14) {
        val date = today.minusDays(dayOffset.toLong())
        add(Transaction(id = id++, amount = 23.0, isIncome = false, categoryId = 1, date = epochMillisFor(date)))
        add(Transaction(id = id++, amount = 50.0, isIncome = false, categoryId = 2, date = epochMillisFor(date)))
        if (dayOffset % 3 == 0) {
            add(Transaction(id = id++, amount = 1500.0, isIncome = true, categoryId = 4, date = epochMillisFor(date)))
        }
        if (dayOffset % 4 == 0) {
            add(Transaction(id = id++, amount = 18.20, isIncome = false, categoryId = 3, date = epochMillisFor(date)))
        }
    }
}