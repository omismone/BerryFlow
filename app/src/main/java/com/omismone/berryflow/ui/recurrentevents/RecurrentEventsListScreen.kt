package com.omismone.berryflow.ui.recurrentevents

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
import com.omismone.berryflow.ui.theme.TopBarButtonPadding
import java.util.Locale

private val SecondaryTextColor = Color(0xFF9E9E9E)
private val BorderColor = Color(0xFFE0E0E0)
private val DeleteModeActiveColor = Color(0xFFE53935)

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
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = TopBarButtonPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onHomeClick) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Back to Dashboard",
                    tint = SecondaryTextColor,
                    modifier = Modifier.size(25.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { deleteModeActive = !deleteModeActive }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Toggle delete mode",
                        tint = if (deleteModeActive) DeleteModeActiveColor else SecondaryTextColor,
                        modifier = Modifier.size(23.dp)
                    )
                }
                IconButton(onClick = onAddClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add recurrent event",
                        tint = SecondaryTextColor,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        if (events.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recurrent events yet",
                    color = SecondaryTextColor,
                    fontSize = 16.sp
                )
            }
        } else {
            EventsTableHeader()

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    val category = categoriesById[event.categoryId]
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
        Text(text = "Name", color = SecondaryTextColor, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(
            text = "Amount", color = SecondaryTextColor, fontSize = 16.sp,
            modifier = Modifier.width(90.dp), textAlign = TextAlign.Center
        )
        Text(
            text = "Frequency", color = SecondaryTextColor, fontSize = 16.sp,
            modifier = Modifier.width(90.dp), textAlign = TextAlign.Center
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(1.dp)
            .background(BorderColor)
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
                color = if (deleteModeActive) DeleteModeActiveColor else Color.Black,
                fontSize = 17.sp
            )
        }

        Text(
            text = formatSignedAmount(event.amount, event.isIncome),
            color = if (deleteModeActive) DeleteModeActiveColor else Color.Black,
            fontSize = 15.sp,
            modifier = Modifier.width(90.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = Frequency.valueOf(event.frequency).label,
            color = if (deleteModeActive) DeleteModeActiveColor else SecondaryTextColor,
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