package com.omismone.berryflow.ui.categories

import com.omismone.berryflow.ui.theme.AppTheme
import com.omismone.berryflow.ui.theme.ScreenTopBar
import com.omismone.berryflow.ui.theme.TopBarIconButton
import com.omismone.berryflow.ui.theme.TopBarNav
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omismone.berryflow.data.Category
import kotlinx.coroutines.launch


// Dedicated editor for creating (original == null) or editing a category.
// All changes are a draft in the ViewModel until Save is pressed.
@Composable
fun CategoryEditScreen(
    viewModel: CategoryEditorViewModel,
    categories: List<Category>,
    onBackClick: () -> Unit,
    onSaved: (categoryId: Long) -> Unit
) {
    val isEditMode = viewModel.original != null
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val nameFocusRequester = remember { FocusRequester() }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    // Colors of the palette, plus the category's current one if it isn't part
    // of the palette (e.g. the seeded categories), so it is never lost.
    val swatches = remember {
        (listOfNotNull(viewModel.original?.color?.takeIf { it !in CategoryColorPalette }) + CategoryColorPalette)
    }

    fun requestClose() {
        if (viewModel.isDirty) showDiscardConfirm = true else onBackClick()
    }

    fun save() {
        focusManager.clearFocus()
        scope.launch { viewModel.save(categories)?.let(onSaved) }
    }

    BackHandler { requestClose() }

    LaunchedEffect(Unit) {
        if (!isEditMode) nameFocusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .navigationBarsPadding()
            .imePadding()
    ) {
        ScreenTopBar(
            title = if (isEditMode) "edit category" else "new category",
            nav = TopBarNav.Back,
            onNavClick = { requestClose() },
            navContentDescription = "Discard and go back"
        ) {
            TopBarIconButton(Icons.Default.Save, "Save", { save() })
        }

        // The content scrolls, so every field stays reachable with the
        // keyboard open. Tapping the background dismisses the keyboard
        // without touching the draft.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            CategoryPreview(
                name = viewModel.name,
                emoji = viewModel.emoji,
                color = viewModel.color
            )

            Spacer(modifier = Modifier.height(32.dp))

            FieldLabel("Name")
            DraftTextField(
                value = viewModel.name,
                onValueChange = viewModel::onNameChange,
                placeholder = "category name",
                modifier = Modifier.fillMaxWidth(),
                focusRequester = nameFocusRequester,
                onDone = { focusManager.clearFocus() }
            )
            viewModel.nameError?.let {
                FieldError(
                    when (it) {
                        CategoryNameError.EMPTY -> "Name can't be empty"
                        CategoryNameError.DUPLICATE -> "A category with this name already exists"
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            FieldLabel("Emoji")
            EmojiSelector(
                emoji = viewModel.emoji,
                onEmojiChange = viewModel::onEmojiChange,
                onDone = { focusManager.clearFocus() }
            )
            Text(
                text = "Tap it, then pick any emoji or character on your keyboard",
                color = AppTheme.colors.secondaryText,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            FieldLabel("Color")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                swatches.chunked(5).forEach { rowColors ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowColors.forEach { colorInt ->
                            ColorSwatch(
                                color = colorInt,
                                selected = colorInt == viewModel.color,
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.onColorChange(colorInt)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text("Discard changes?") },
            text = {
                Text(
                    if (isEditMode) "Your changes to this category won't be saved."
                    else "This category won't be created."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardConfirm = false
                    onBackClick()
                }) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) {
                    Text("Keep editing")
                }
            }
        )
    }
}

@Composable
private fun CategoryPreview(name: String, emoji: String, color: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(color).copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 30.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        val trimmed = name.trim()
        Text(
            text = trimmed.ifEmpty { "category name" }.lowercase(),
            color = if (trimmed.isEmpty()) AppTheme.colors.secondaryText else AppTheme.colors.primaryText,
            fontSize = 20.sp,
            maxLines = 1,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = AppTheme.colors.secondaryText,
        fontSize = 14.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun FieldError(text: String) {
    Text(
        text = text,
        color = AppTheme.colors.expense,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}

// Plain text input over the draft: what is typed is exactly what is stored in
// the draft, and an empty field shows a placeholder instead of real text.
@Composable
private fun DraftTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    onDone: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = AppTheme.colors.secondaryText,
                fontSize = 17.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 17.sp,
                color = AppTheme.colors.primaryText
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
        )
    }
}

// The emoji is chosen by tapping this square and using the keyboard. The text
// input behind it is invisible (no text box, no caret): the square just shows
// the current emoji and is outlined while it is receiving input.
@Composable
private fun EmojiSelector(
    emoji: String,
    onEmojiChange: (String) -> Unit,
    onDone: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var active by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(shape)
            .border(
                width = if (active) 2.dp else 1.dp,
                color = if (active) AppTheme.colors.primaryText else AppTheme.colors.border,
                shape = shape
            )
            .clickable {
                focusRequester.requestFocus()
                keyboard?.show()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 32.sp)
        BasicTextField(
            value = emoji,
            onValueChange = onEmojiChange,
            singleLine = true,
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = Modifier
                .size(1.dp)
                .alpha(0f)
                .focusRequester(focusRequester)
                .onFocusChanged { active = it.isFocused }
        )
    }
}

@Composable
private fun ColorSwatch(color: Int, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(color))
            .then(
                if (selected) Modifier.border(2.dp, AppTheme.colors.primaryText, RoundedCornerShape(8.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
    )
}