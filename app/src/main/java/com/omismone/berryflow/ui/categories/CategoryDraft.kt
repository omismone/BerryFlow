package com.omismone.berryflow.ui.categories

import com.omismone.berryflow.data.Category
import java.text.BreakIterator

enum class CategoryNameError { EMPTY, DUPLICATE }

// Emoji of a newly created category. It is a real value of the category (and
// what the editor's preview shows), not a placeholder.
const val DefaultCategoryEmoji = "❓"

// Soft mid-tone colors in the same family as the seeded categories (they are
// shown as 25% tints over the background, so they read on light and dark).
// One row per hue group: warm, green/cyan, blue/purple/pink, neutrals.
val CategoryColorPalette = listOf(
    0xFFE57373.toInt(), 0xFFFF8A65.toInt(), 0xFFFFB74D.toInt(), 0xFFFFD54F.toInt(), 0xFFDCE775.toInt(),
    0xFFAED581.toInt(), 0xFF81C784.toInt(), 0xFF4DB6AC.toInt(), 0xFF4DD0E1.toInt(), 0xFF4FC3F7.toInt(),
    0xFF64B5F6.toInt(), 0xFF7986CB.toInt(), 0xFF9575CD.toInt(), 0xFFBA68C8.toInt(), 0xFFF48FB1.toInt(),
    0xFFF06292.toInt(), 0xFFA1887F.toInt(), 0xFF90A4AE.toInt(), 0xFFFFCCBC.toInt(), 0xFFBDBDBD.toInt()
)

// Color preselected for a category being created.
val NewCategoryColor = 0xFFBDBDBD.toInt()

// Names are unique case-insensitively across all categories (Default included).
// editingId excludes the category being edited from the duplicate check.
fun validateCategoryName(name: String, categories: List<Category>, editingId: Long?): CategoryNameError? {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return CategoryNameError.EMPTY
    val isDuplicate = categories.any {
        it.id != editingId && it.name.trim().equals(trimmed, ignoreCase = true)
    }
    return if (isDuplicate) CategoryNameError.DUPLICATE else null
}

// Default first (as it is the fallback category), then alphabetical,
// case-insensitive.
fun sortCategories(categories: List<Category>): List<Category> =
    categories.sortedWith(
        compareByDescending<Category> { it.isDefault }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name.trim() }
            .thenBy { it.id }
    )

// The category "emoji" is whatever the user types on the keyboard (an emoji or
// any character), limited to a single visible character (grapheme cluster).
// Keeps the LAST one typed, so typing a new one replaces the previous one.
// Returns "" for blank input.
fun lastGrapheme(text: String): String {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return ""
    val iterator = BreakIterator.getCharacterInstance()
    iterator.setText(trimmed)
    val end = iterator.last()
    val start = iterator.previous()
    return trimmed.substring(start, end)
}