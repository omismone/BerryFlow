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

private val ColorPalette = listOf(
    0xFFF48FB1.toInt(), 0xFFE57373.toInt(), 0xFFFFB74D.toInt(), 0xFF64B5F6.toInt(),
    0xFF81C784.toInt(), 0xFFBA68C8.toInt(), 0xFF4DB6AC.toInt(), 0xFFFFD54F.toInt(),
    0xFFA1887F.toInt(), 0xFFBDBDBD.toInt()
)

@Composable
fun CategoriesScreen(
    categories: List<Category>,
    onHomeClick: () -> Unit,
    onAddCategory: (Category) -> Unit,
    onRenameCategory: (Category, String) -> Unit,
    onRecolorCategory: (Category, Int) -> Unit,
    onReemojiCategory: (Category, String) -> Unit,
    onDeleteCategory: (Category) -> Unit
) {
    var editingNameId by remember { mutableStateOf<Long?>(null) }
    var editingEmojiId by remember { mutableStateOf<Long?>(null) }
    var colorPickerTargetId by remember { mutableStateOf<Long?>(null) }
    var deleteTargetId by remember { mutableStateOf<Long?>(null) }
    var showDuplicateNewCategoryDialog by remember { mutableStateOf(false) }
    var deleteModeActive by remember { mutableStateOf(false) }

    fun requestAddNewCategory() {
        if (categories.any { it.name == NewCategoryDefaultName }) {
            showDuplicateNewCategoryDialog = true
            return
        }

        onAddCategory(
            Category(
                name = NewCategoryDefaultName,
                color = NewCategoryDefaultColor,
                emoji = NewCategoryDefaultEmoji
            )
        )
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

                IconButton(onClick = { requestAddNewCategory() }) {
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
            items(
                items = categories.sortedByDescending { it.name == NewCategoryDefaultName },
                key = { it.id }
            ) { category ->
                CategoryRow(
                    category = category,
                    isEditingName = editingNameId == category.id,
                    isEditingEmoji = editingEmojiId == category.id,
                    deleteModeActive = deleteModeActive,
                    onNameClick = {
                        if (deleteModeActive) {
                            deleteTargetId = category.id
                        } else {
                            editingNameId = category.id
                        }
                    },
                    onNameCommit = { newName ->
                        onRenameCategory(category, newName)
                        editingNameId = null
                    },
                    onEmojiClick = {
                        if (deleteModeActive) {
                            deleteTargetId = category.id
                        } else {
                            editingEmojiId = category.id
                        }
                    },
                    onEmojiCommit = { newEmoji ->
                        onReemojiCategory(category, newEmoji)
                        editingEmojiId = null
                    },
                    onColorClick = {
                        if (deleteModeActive) {
                            deleteTargetId = category.id
                        } else {
                            colorPickerTargetId = category.id
                        }
                    }
                )
            }
        }
    }

    colorPickerTargetId?.let { targetId ->
        val target = categories.first { it.id == targetId }

        AlertDialog(
            onDismissRequest = { colorPickerTargetId = null },
            confirmButton = {},
            title = { Text("Choose a color") },
            text = {
                ColorPickerGrid { pickedColor ->
                    onRecolorCategory(target, pickedColor)
                    colorPickerTargetId = null
                }
            }
        )
    }

    deleteTargetId?.let { targetId ->
        val target = categories.first { it.id == targetId }

        AlertDialog(
            onDismissRequest = { deleteTargetId = null },
            title = { Text("Delete category?") },
            text = { Text("Transactions in this category will be moved to Default.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCategory(target)
                        deleteTargetId = null
                        deleteModeActive = false
                    }
                ) {
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

    if (showDuplicateNewCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showDuplicateNewCategoryDialog = false },
            title = { Text("Unedited category found") },
            text = {
                Text(
                    "You already have a \"New Category\" — please rename it before adding another one."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDuplicateNewCategoryDialog = false
                    }
                ) {
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
    onNameCommit: (String) -> Unit,
    onEmojiClick: () -> Unit,
    onEmojiCommit: (String) -> Unit,
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
                var text by remember(category.id) {
                    mutableStateOf(category.name)
                }

                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 17.sp,
                        color = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onNameCommit(text)
                        }
                    ),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth()
                )
            } else {
                Text(
                    text = category.name.lowercase(),
                    color = if (deleteModeActive) {
                        DeleteModeActiveColor
                    } else {
                        Color.Black
                    },
                    fontSize = 17.sp,
                    modifier = Modifier.clickable {
                        onNameClick()
                    }
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
                    .background(
                        Color(category.color).copy(alpha = 0.25f)
                    )
                    .clickable {
                        onColorClick()
                    }
            )
        }

        Box(
            modifier = Modifier.width(50.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isEditingEmoji) {
                var text by remember(category.id) {
                    mutableStateOf(category.emoji)
                }

                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onEmojiCommit(text)
                        }
                    ),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .width(40.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Color(category.color).copy(alpha = 0.25f)
                        )
                        .clickable {
                            onEmojiClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.emoji,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorPickerGrid(
    onColorPicked: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ColorPalette.chunked(5).forEach { rowColors ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowColors.forEach { colorInt ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(colorInt))
                            .clickable {
                                onColorPicked(colorInt)
                            }
                    )
                }
            }
        }
    }
}