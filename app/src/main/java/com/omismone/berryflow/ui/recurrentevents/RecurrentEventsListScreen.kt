package com.omismone.berryflow.ui.recurrentevents

import com.omismone.berryflow.ui.theme.AppTheme
import com.omismone.berryflow.ui.theme.ScreenTopBar
import com.omismone.berryflow.ui.theme.TopBarIconButton
import com.omismone.berryflow.ui.theme.TopBarNav
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omismone.berryflow.data.Category
import com.omismone.berryflow.data.Frequency
import com.omismone.berryflow.data.RecurrentEvent
import java.util.Locale


@Composable
fun RecurrentEventsListScreen(
    events: List<RecurrentEvent>,
    categories: List<Category>,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit,
    onEventClick: (RecurrentEvent) -> Unit,
    onDeleteEvent: (RecurrentEvent) -> Unit
) {
    val categoriesById = categories.associateBy { it.id }
    var deleteModeActive by remember { mutableStateOf(false) }
    var deleteTargetId by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
    ) {
        ScreenTopBar(title = "recurrent events", nav = TopBarNav.Home, onNavClick = onHomeClick) {
            TopBarIconButton(
                Icons.Default.Delete,
                "Toggle delete mode",
                { deleteModeActive = !deleteModeActive },
                tint = if (deleteModeActive) AppTheme.colors.expense else AppTheme.colors.secondaryText
            )
            TopBarIconButton(Icons.Default.Add, "Add recurrent event", onAddClick)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (events.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recurrent events yet",
                    color = AppTheme.colors.secondaryText,
                    fontSize = 16.sp
                )
            }
        } else {
            EventsTableHeader()

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = 8.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
            ) {
                items(events, key = { it.id }) { event ->
                    val category = categoriesById[event.categoryId] ?: categories.firstOrNull { it.isDefault }
                    if (category != null) {
                        EventRow(
                            event = event,
                            category = category,
                            deleteModeActive = deleteModeActive,
                            onClick = {
                                if (deleteModeActive) deleteTargetId = event.id
                                else onEventClick(event)
                            }
                        )
                    }
                }
            }
        }
    }

    deleteTargetId?.let { targetId ->
        val target = events.first { it.id == targetId }
        AlertDialog(
            onDismissRequest = { deleteTargetId = null },
            title = { Text("Delete recurrent event?") },
            text = { Text("This recurring transaction will no longer be created.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteEvent(target)
                    deleteTargetId = null
                    deleteModeActive = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTargetId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EventsTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Name", color = AppTheme.colors.secondaryText, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(
            text = "Amount", color = AppTheme.colors.secondaryText, fontSize = 16.sp,
            modifier = Modifier.width(90.dp), textAlign = TextAlign.Center
        )
        Text(
            text = "Frequency", color = AppTheme.colors.secondaryText, fontSize = 16.sp,
            modifier = Modifier.width(90.dp), textAlign = TextAlign.Center
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(1.dp)
            .background(AppTheme.colors.border)
    )
}

@Composable
private fun EventRow(
    event: RecurrentEvent,
    category: Category,
    deleteModeActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(category.color).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = category.emoji, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = event.name?.takeIf { it.isNotBlank() } ?: category.name.lowercase(),
                color = if (deleteModeActive) AppTheme.colors.expense else AppTheme.colors.primaryText,
                fontSize = 17.sp
            )
        }

        Text(
            text = formatSignedAmount(event.amount, event.isIncome),
            color = if (deleteModeActive) AppTheme.colors.expense else AppTheme.colors.primaryText,
            fontSize = 15.sp,
            modifier = Modifier.width(90.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = Frequency.valueOf(event.frequency).label,
            color = if (deleteModeActive) AppTheme.colors.expense else AppTheme.colors.secondaryText,
            fontSize = 15.sp,
            modifier = Modifier.width(90.dp),
            textAlign = TextAlign.Center
        )
    }
}

private fun formatSignedAmount(amount: Double, isIncome: Boolean): String {
    val sign = if (isIncome) "+" else "-"
    return "$sign ${String.format(Locale.US, "%.2f", amount)} €"
}