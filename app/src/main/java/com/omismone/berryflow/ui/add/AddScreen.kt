package com.omismone.berryflow.ui.add

import com.omismone.berryflow.ui.theme.AppTheme
import com.omismone.berryflow.ui.theme.ScreenTopBar
import com.omismone.berryflow.ui.theme.TopBarIconButton
import com.omismone.berryflow.ui.theme.TopBarNav
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omismone.berryflow.data.Category
import com.omismone.berryflow.ui.theme.clickableNoRipple
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    categories: List<Category>,
    initialCategory: Category,
    isEditMode: Boolean = false,
    initialAmount: Double = 0.0,
    initialName: String = "",
    initialIsIncome: Boolean = false,
    initialDate: LocalDate = LocalDate.now(),
    onBackClick: () -> Unit,
    onSaveClick: (amount: Double, name: String, isIncome: Boolean, category: Category, date: LocalDate) -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // The whole draft is saveable so it survives rotation; nothing reaches the
    // database until Save (or the OK key) is pressed.
    var amountEntry by rememberSaveable(stateSaver = AmountEntry.Saver) {
        mutableStateOf(
            if (initialAmount > 0.0) AmountEntry.existing(formatPlainAmount(initialAmount))
            else AmountEntry()
        )
    }
    var transactionName by rememberSaveable { mutableStateOf(initialName) }
    var isIncome by rememberSaveable { mutableStateOf(initialIsIncome) }
    var selectedEpochDay by rememberSaveable { mutableLongStateOf(initialDate.toEpochDay()) }
    var selectedCategoryId by rememberSaveable { mutableLongStateOf(initialCategory.id) }
    val selectedDate = LocalDate.ofEpochDay(selectedEpochDay)
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId } ?: initialCategory
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var hasSubmitted by remember { mutableStateOf(false) }

    fun onDigitPress(digit: String) {
        amountEntry = amountEntry.digit(digit)
    }

    fun onDotPress() {
        amountEntry = amountEntry.dot()
    }

    fun onBackspacePress() {
        amountEntry = amountEntry.backspace()
    }

    fun onOkPress() {
        if (hasSubmitted) return
        val amount = parseAmount(amountEntry.text)
        if (amount <= 0.0) {
            Toast.makeText(context, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }
        hasSubmitted = true
        onSaveClick(amount, transactionName, isIncome, selectedCategory, selectedDate)
        Toast.makeText(
            context,
            if (isEditMode) "Transaction updated" else "Transaction saved",
            Toast.LENGTH_SHORT
        ).show()
    }

    val displayAmount = amountEntry.display

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .navigationBarsPadding()
    ) {
        ScreenTopBar(
            title = if (isEditMode) "edit transaction" else "new transaction",
            nav = TopBarNav.Back,
            onNavClick = onBackClick
        ) {
            if (isEditMode) {
                TopBarIconButton(Icons.Default.Delete, "Delete transaction", { showDeleteConfirm = true })
            }
            TopBarIconButton(Icons.Default.Save, "Save", { onOkPress() })
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "€",
                color = AppTheme.colors.secondaryText,
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 18.dp)
            )
            // Tapping the amount clears it, so it can be retyped from scratch
            // without pressing backspace digit by digit.
            Text(
                text = displayAmount,
                color = AppTheme.colors.primaryText,
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .defaultMinSize(minWidth = 96.dp) // keeps the layout (and the tap target) when empty
                    .clickableNoRipple { amountEntry = amountEntry.clear() }
            )
            IconButton(
                onClick = { onBackspacePress() },
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = AppTheme.colors.secondaryText,
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
                .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = AppTheme.colors.secondaryText,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.CenterStart)
                )
                if (transactionName.isEmpty()) {
                    Text(
                        text = selectedCategory.name.lowercase(),
                        color = AppTheme.colors.secondaryText,
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
                        color = AppTheme.colors.primaryText,
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
                    date = selectedDate,
                    onClick = { showDatePicker = true }
                )
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
                                selectedCategoryId = category.id
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
            onOkPress = { onOkPress() }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedEpochDay = java.time.Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .toEpochDay()
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

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete transaction?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    if (!hasSubmitted) {
                        hasSubmitted = true
                        showDeleteConfirm = false
                        onDeleteClick()
                        Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TypeToggleButton(isIncome: Boolean, onToggle: () -> Unit) {
    val color = if (isIncome) AppTheme.colors.income else AppTheme.colors.expense
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
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(8.dp))
            .clickable { onClick() }
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
        Text(text = formatDateLabel(date) + " ", color = AppTheme.colors.secondaryText, fontSize = 14.sp)
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = AppTheme.colors.secondaryText,
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
        Text(text = category.name.lowercase(), color = AppTheme.colors.primaryText, fontSize = 15.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = AppTheme.colors.primaryText,
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
                    .background(AppTheme.colors.okKey)
                    .clickable { onOkPress() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
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
            .background(AppTheme.colors.keyBackground)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 24.sp, color = AppTheme.colors.primaryText)
    }
}

private fun formatPlainAmount(amount: Double): String =
    String.format(Locale.US, "%.2f", amount)

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