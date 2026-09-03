package com.omismone.berryflow.ui.recurrentevents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omismone.berryflow.data.Category
import com.omismone.berryflow.ui.theme.TopBarButtonPadding
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

private val SecondaryTextColor = Color(0xFF9E9E9E)
private val BorderColor = Color(0xFFE0E0E0)
private val IncomeColor = Color(0xFF43A047)
private val ExpenseColor = Color(0xFFE53935)
private val KeyBackgroundColor = Color(0xFFECECEC)
private val OkKeyBackgroundColor = Color(0xFF424242)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrentEventsScreen(
    categories: List<Category>,
    initialCategory: Category,
    onDiscardClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var transactionName by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var frequency by remember { mutableStateOf(Frequency.MONTHLY) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showFrequencyMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    fun onDigitPress(digit: String) {
        val dotIndex = amountInput.indexOf('.')
        if (dotIndex != -1) {
            val decimalsTyped = amountInput.length - dotIndex - 1
            if (decimalsTyped >= 2) return
        }
        amountInput += digit
    }

    fun onDotPress() {
        if (amountInput.contains('.')) return
        amountInput = if (amountInput.isEmpty()) "0." else "$amountInput."
    }

    fun onBackspacePress() {
        if (amountInput.isEmpty()) return
        amountInput = amountInput.dropLast(1)
    }

    val displayAmount = amountInput.ifEmpty { "0.00" }

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
            IconButton(onClick = onDiscardClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Discard and go back",
                    tint = SecondaryTextColor,
                    modifier = Modifier.size(25.dp)
                )
            }
            IconButton(onClick = onSaveClick) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save",
                    tint = SecondaryTextColor,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(70.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "€",
                color = SecondaryTextColor,
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 18.dp)
            )
            Text(
                text = displayAmount,
                color = Color.Black,
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { onBackspacePress() },
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    tint = SecondaryTextColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = SecondaryTextColor,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.CenterStart)
                )
                if (transactionName.isEmpty()) {
                    Text(
                        text = selectedCategory.name.lowercase(),
                        color = SecondaryTextColor,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                BasicTextField(
                    value = transactionName,
                    onValueChange = { transactionName = it },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                TypeToggleButton(
                    isIncome = isIncome,
                    onToggle = { isIncome = !isIncome }
                )
                Spacer(modifier = Modifier.height(8.dp))
                DateButton(
                    date = startDate,
                    onClick = { showDatePicker = true }
                )
                Spacer(modifier = Modifier.height(8.dp))
                FrequencyButton(
                    frequency = frequency,
                    onClick = { showFrequencyMenu = true }
                )
                DropdownMenu(
                    expanded = showFrequencyMenu,
                    onDismissRequest = { showFrequencyMenu = false }
                ) {
                    Frequency.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                frequency = option
                                showFrequencyMenu = false
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.offset(y = 18.dp)) {
                CategoryButton(
                    category = selectedCategory,
                    onClick = { showCategoryMenu = true }
                )
                DropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name.lowercase()) },
                            leadingIcon = { Text(category.emoji) },
                            onClick = {
                                selectedCategory = category
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        NumericKeypad(
            onDigitPress = { onDigitPress(it) },
            onDotPress = { onDotPress() },
            onOkPress = { /* behavior not defined yet */ }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        startDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun TypeToggleButton(isIncome: Boolean, onToggle: () -> Unit) {
    val color = if (isIncome) IncomeColor else ExpenseColor
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, color, RoundedCornerShape(8.dp))
            .clickable { onToggle() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (isIncome) "income" else "expense",
            color = color,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun DateButton(date: LocalDate, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
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
        Text(text = formatDateLabel(date) + " ", color = SecondaryTextColor, fontSize = 14.sp)
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = SecondaryTextColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun FrequencyButton(frequency: Frequency, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
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
        Text(text = frequency.label + " ", color = SecondaryTextColor, fontSize = 14.sp)
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = SecondaryTextColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CategoryButton(category: Category, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(category.color).copy(alpha = 0.25f))
            .clickable { onClick() }
            .padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = category.emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = category.name.lowercase(), color = Color.Black, fontSize = 15.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun NumericKeypad(
    onDigitPress: (String) -> Unit,
    onDotPress: () -> Unit,
    onOkPress: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KeypadRow {
            KeypadKey("1", modifier = Modifier.weight(1f)) { onDigitPress("1") }
            KeypadKey("2", modifier = Modifier.weight(1f)) { onDigitPress("2") }
            KeypadKey("3", modifier = Modifier.weight(1f)) { onDigitPress("3") }
        }
        KeypadRow {
            KeypadKey("4", modifier = Modifier.weight(1f)) { onDigitPress("4") }
            KeypadKey("5", modifier = Modifier.weight(1f)) { onDigitPress("5") }
            KeypadKey("6", modifier = Modifier.weight(1f)) { onDigitPress("6") }
        }
        KeypadRow {
            KeypadKey("7", modifier = Modifier.weight(1f)) { onDigitPress("7") }
            KeypadKey("8", modifier = Modifier.weight(1f)) { onDigitPress("8") }
            KeypadKey("9", modifier = Modifier.weight(1f)) { onDigitPress("9") }
        }
        KeypadRow {
            KeypadKey(".", modifier = Modifier.weight(1f)) { onDotPress() }
            KeypadKey("0", modifier = Modifier.weight(1f)) { onDigitPress("0") }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OkKeyBackgroundColor)
                    .clickable { onOkPress() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "OK",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun KeypadRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun KeypadKey(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(KeyBackgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 24.sp, color = Color.Black)
    }
}

private fun formatDateLabel(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Today, ${date.dayOfMonth} ${date.monthAbbreviation()}"
        today.minusDays(1) -> "Yesterday, ${date.dayOfMonth} ${date.monthAbbreviation()}"
        else -> "${date.dayOfWeekAbbreviation()}, ${date.dayOfMonth} ${date.monthAbbreviation()}"
    }
}

private fun LocalDate.monthAbbreviation(): String =
    month.getDisplayName(JavaTextStyle.SHORT, Locale.ENGLISH)

private fun LocalDate.dayOfWeekAbbreviation(): String =
    dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.ENGLISH)