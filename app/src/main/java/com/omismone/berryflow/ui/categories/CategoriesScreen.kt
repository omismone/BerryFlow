package com.omismone.berryflow.ui.categories

import com.omismone.berryflow.ui.theme.AppTheme
import com.omismone.berryflow.ui.theme.ScreenTopBar
import com.omismone.berryflow.ui.theme.TopBarIconButton
import com.omismone.berryflow.ui.theme.TopBarNav
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omismone.berryflow.data.Category
import kotlinx.coroutines.delay


// Read-only list: creating and editing happen in the category editor screen,
// so rows never change or move while the user is configuring them.
@Composable
fun CategoriesScreen(
    categories: List<Category>,
    // Category just created/edited in the editor: the list scrolls to it (if
    // it isn't already fully visible) and briefly highlights it.
    highlightCategoryId: Long?,
    onHighlightConsumed: () -> Unit,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit,
    onEditCategory: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit
) {
    var deleteTargetId by remember { mutableStateOf<Long?>(null) }
    var deleteModeActive by remember { mutableStateOf(false) }
    var highlightedId by remember { mutableStateOf<Long?>(null) }

    val sortedCategories = remember(categories) { sortCategories(categories) }
    val listState = rememberLazyListState()

    // Runs again when the list changes, so a category that was just saved is
    // found as soon as it reaches the list.
    LaunchedEffect(highlightCategoryId, sortedCategories) {
        val targetId = highlightCategoryId ?: return@LaunchedEffect
        val index = sortedCategories.indexOfFirst { it.id == targetId }
        if (index < 0) return@LaunchedEffect

        if (!listState.isItemFullyVisible(index)) {
            // Keeps a couple of rows above for context.
            listState.animateScrollToItem((index - 2).coerceAtLeast(0))
        }
        highlightedId = targetId
        onHighlightConsumed()
    }

    // One animation for the whole screen, drawn only on the highlighted row.
    val highlightAlpha = remember { Animatable(0f) }
    LaunchedEffect(highlightedId) {
        if (highlightedId != null) {
            highlightAlpha.snapTo(0f)
            highlightAlpha.animateTo(1f, tween(300))
            delay(1000)
            highlightAlpha.animateTo(0f, tween(400))
            highlightedId = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
    ) {
        ScreenTopBar(title = "categories", nav = TopBarNav.Home, onNavClick = onHomeClick) {
            TopBarIconButton(
                Icons.Default.Delete,
                "Toggle delete mode",
                { deleteModeActive = !deleteModeActive },
                tint = if (deleteModeActive) AppTheme.colors.expense else AppTheme.colors.secondaryText
            )
            TopBarIconButton(Icons.Default.Add, "Add category", onAddClick)
        }

        Spacer(modifier = Modifier.height(16.dp))

        CategoriesTableHeader()

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = 8.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            )
        ) {
            items(items = sortedCategories, key = { it.id }) { category ->
                CategoryRow(
                    category = category,
                    highlightAlpha = if (highlightedId == category.id) highlightAlpha else null,
                    deleteModeActive = deleteModeActive,
                    onClick = {
                        if (deleteModeActive) {
                            // Default can't be deleted, so it does nothing here.
                            if (!category.isDefault) deleteTargetId = category.id
                        } else {
                            onEditCategory(category)
                        }
                    }
                )
            }
        }
    }

    deleteTargetId?.let { targetId ->
        val target = categories.firstOrNull { it.id == targetId }
        if (target == null) {
            deleteTargetId = null
            return@let
        }

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
}

private fun LazyListState.isItemFullyVisible(index: Int): Boolean {
    val info = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return false
    return info.offset >= layoutInfo.viewportStartOffset &&
        info.offset + info.size <= layoutInfo.viewportEndOffset
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
            color = AppTheme.colors.secondaryText,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "Color",
            color = AppTheme.colors.secondaryText,
            fontSize = 16.sp,
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Emoji",
            color = AppTheme.colors.secondaryText,
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
            .background(AppTheme.colors.border)
    )
}

@Composable
private fun CategoryRow(
    category: Category,
    highlightAlpha: Animatable<Float, *>?,
    deleteModeActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (highlightAlpha != null) {
                    Modifier.drawBehind {
                        drawRoundRect(
                            color = Color(category.color).copy(alpha = 0.2f * highlightAlpha.value),
                            cornerRadius = CornerRadius(10.dp.toPx())
                        )
                    }
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            // Default is a regular category except that it can't be deleted,
            // so in delete mode it is greyed out with a hint instead of red.
            val locked = deleteModeActive && category.isDefault
            Column {
                Text(
                    text = category.name.lowercase(),
                    color = when {
                        locked -> AppTheme.colors.secondaryText
                        deleteModeActive -> AppTheme.colors.expense
                        else -> AppTheme.colors.primaryText
                    },
                    fontSize = 17.sp
                )
                if (locked) {
                    Text(
                        text = "can't be deleted",
                        color = AppTheme.colors.secondaryText,
                        fontSize = 12.sp
                    )
                }
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
            )
        }

        Box(
            modifier = Modifier.width(50.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(category.color).copy(alpha = 0.25f)),
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