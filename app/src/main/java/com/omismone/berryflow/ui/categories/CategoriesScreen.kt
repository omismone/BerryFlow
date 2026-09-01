package com.omismone.berryflow.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omismone.berryflow.data.Category
import com.omismone.berryflow.ui.theme.TopBarButtonPadding

private val SecondaryTextColor = Color(0xFF9E9E9E)
private val BorderColor = Color(0xFFE0E0E0)
private val DeleteModeActiveColor = Color(0xFFE53935)

private val NewCategoryDefaultColor = 0xFFBDBDBD.toInt()
private const val NewCategoryDefaultEmoji = "❓"
private const val NewCategoryDefaultName = "New Category"

// Fixed palette offered in the color picker. Pastel tones consistent with the
// rest of the app; final base-category colors will be refined later.
private val ColorPalette = listOf(
    0xFFF48FB1.toInt(), // pink
    0xFFE57373.toInt(), // red
    0xFFFFB74D.toInt(), // orange
    0xFF64B5F6.toInt(), // blue
    0xFF81C784.toInt(), // green
    0xFFBA68C8.toInt(), // purple
    0xFF4DB6AC.toInt(), // teal
    0xFFFFD54F.toInt(), // yellow
    0xFFA1887F.toInt(), // brown
    0xFFBDBDBD.toInt()  // gray
)

@Composable
fun CategoriesScreen(
    onHomeClick: () -> Unit
) {
    var categories by remember { mutableStateOf(fakeCategoriesForTesting()) }
    var nextId by remember { mutableStateOf(100L) }

    var editingNameId by remember { mutableStateOf<Long?>(null) }
    var editingEmojiId by remember { mutableStateOf<Long?>(null) }
    var colorPickerTargetId by remember { mutableStateOf<Long?>(null) }
    var deleteTargetId by remember { mutableStateOf<Long?>(null) }
    var showDuplicateNewCategoryDialog by remember { mutableStateOf(false) }
    var deleteModeActive by remember { mutableStateOf(false) }

    fun updateCategory(id: Long, transform: (Category) -> Category) {
        categories = categories.map { if (it.id == id) transform(it) else it }
    }

    fun addNewCategory() {
        if (categories.any { it.name == NewCategoryDefaultName }) {
            showDuplicateNewCategoryDialog = true
            return
        }
        val newCategory = Category(
            id = nextId,
            name = NewCategoryDefaultName,
            color = NewCategoryDefaultColor,
            emoji = NewCategoryDefaultEmoji
        )
        nextId += 1
        categories = listOf(newCategory) + categories
    }

    fun deleteCategory(id: Long) {
        categories = categories.filterNot { it.id == id }
    }

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
                IconButton(onClick = { addNewCategory() }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add category",
                        tint = SecondaryTextColor,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        CategoriesTableHeader()

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            items(categories, key = { it.id }) { category ->
                CategoryRow(
                    category = category,
                    isEditingName = editingNameId == category.id,
                    isEditingEmoji = editingEmojiId == category.id,
                    deleteModeActive = deleteModeActive,
                    onNameClick = {
                        if (deleteModeActive) deleteTargetId = category.id
                        else editingNameId = category.id
                    },
                    onNameChange = { newName ->
                        updateCategory(category.id) { it.copy(name = newName) }
                    },
                    onNameDone = { editingNameId = null },
                    onEmojiClick = {
                        if (deleteModeActive) deleteTargetId = category.id
                        else editingEmojiId = category.id
                    },
                    onEmojiChange = { newEmoji ->
                        updateCategory(category.id) { it.copy(emoji = newEmoji) }
                    },
                    onEmojiDone = { editingEmojiId = null },
                    onColorClick = {
                        if (deleteModeActive) deleteTargetId = category.id
                        else colorPickerTargetId = category.id
                    }
                )
            }
        }
    }

    // Color picker dialog
    colorPickerTargetId?.let { targetId ->
        AlertDialog(
            onDismissRequest = { colorPickerTargetId = null },
            confirmButton = {},
            title = { Text("Choose a color") },
            text = {
                ColorPickerGrid { pickedColor ->
                    updateCategory(targetId) { it.copy(color = pickedColor) }
                    colorPickerTargetId = null
                }
            }
        )
    }

    // Delete confirmation dialog
    deleteTargetId?.let { targetId ->
        AlertDialog(
            onDismissRequest = { deleteTargetId = null },
            title = { Text("Delete category?") },
            text = { Text("Transactions in this category will be moved to Default.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteCategory(targetId)
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

    // "New Category" already exists warning
    if (showDuplicateNewCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showDuplicateNewCategoryDialog = false },
            title = { Text("Unedited category found") },
            text = { Text("You already have a \"new category\".") },
            confirmButton = {
                TextButton(onClick = { showDuplicateNewCategoryDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun CategoriesTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Name",
            color = SecondaryTextColor,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Color",
            color = SecondaryTextColor,
            fontSize = 16.sp,
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Emoji",
            color = SecondaryTextColor,
            fontSize = 16.sp,
            modifier = Modifier.width(50.dp),
            textAlign = TextAlign.Center
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
private fun CategoryRow(
    category: Category,
    isEditingName: Boolean,
    isEditingEmoji: Boolean,
    deleteModeActive: Boolean,
    onNameClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onNameDone: () -> Unit,
    onEmojiClick: () -> Unit,
    onEmojiChange: (String) -> Unit,
    onEmojiDone: () -> Unit,
    onColorClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (isEditingName) {
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
                BasicTextField(
                    value = category.name,
                    onValueChange = onNameChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 17.sp, color = Color.Black),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onNameDone() }),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth()
                )
            } else {
                Text(
                    text = category.name.lowercase(),
                    color = if (deleteModeActive) DeleteModeActiveColor else Color.Black,
                    fontSize = 17.sp,
                    modifier = Modifier.clickable { onNameClick() }
                )
            }
        }

        Box(
            modifier = Modifier.width(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(category.color).copy(alpha = 0.25f))
                    .clickable { onColorClick() }
            )
        }

        Box(
            modifier = Modifier.width(50.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isEditingEmoji) {
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
                BasicTextField(
                    value = category.emoji,
                    onValueChange = onEmojiChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onEmojiDone() }),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .width(40.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(category.color).copy(alpha = 0.25f))
                        .clickable { onEmojiClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = category.emoji, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun ColorPickerGrid(onColorPicked: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ColorPalette.chunked(5).forEach { rowColors ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowColors.forEach { colorInt ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(colorInt))
                            .clickable { onColorPicked(colorInt) }
                    )
                }
            }
        }
    }
}

// Temporary fake categories, same ones used across Dashboard/Add for
// consistency during testing. Will be replaced by real Room-backed data,
// with the full base-category list, once we wire up persistence.
private fun fakeCategoriesForTesting(): List<Category> = listOf(
    Category(id = 1, name = "groceries", color = 0xFFF48FB1.toInt(), emoji = "🛍️"),
    Category(id = 2, name = "fuel", color = 0xFFE57373.toInt(), emoji = "⛽"),
    Category(id = 3, name = "gift", color = 0xFFFFB74D.toInt(), emoji = "🎁"),
    Category(id = 4, name = "paycheck", color = 0xFF64B5F6.toInt(), emoji = "💰"),
    Category(id = 5, name = "entertainment", color = 0xFFBA68C8.toInt(), emoji = "🎬"),
    Category(id = 6, name = "health", color = 0xFF4DB6AC.toInt(), emoji = "💊")
)