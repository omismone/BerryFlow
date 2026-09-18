package com.omismone.berryflow.ui.categories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.omismone.berryflow.data.BerryFlowRepository
import com.omismone.berryflow.data.Category

// Holds the draft of the category being created (original == null) or edited.
// Nothing reaches the database until save() succeeds.
class CategoryEditorViewModel(
    private val repository: BerryFlowRepository,
    val original: Category?
) : ViewModel() {

    var name by mutableStateOf(original?.name ?: "")
        private set
    var emoji by mutableStateOf(original?.emoji ?: DefaultCategoryEmoji)
        private set
    var color by mutableStateOf(original?.color ?: NewCategoryColor)
        private set

    var nameError by mutableStateOf<CategoryNameError?>(null)
        private set
    private var saving = false

    val isDirty: Boolean
        get() = if (original == null) {
            name.isNotBlank() || emoji != DefaultCategoryEmoji || color != NewCategoryColor
        } else {
            name != original.name || emoji != original.emoji || color != original.color
        }

    fun onNameChange(value: String) {
        name = value
        nameError = null
    }

    fun onEmojiChange(value: String) {
        // Clearing the field goes back to the default emoji, so a category
        // always has one.
        emoji = lastGrapheme(value).ifEmpty { DefaultCategoryEmoji }
    }

    fun onColorChange(value: Int) {
        color = value
    }

    // Validates and persists the draft. Returns the saved category's id, or
    // null if the name is invalid (the error is then exposed to the UI and
    // nothing is written).
    suspend fun save(existingCategories: List<Category>): Long? {
        val trimmedName = name.trim()
        nameError = validateCategoryName(trimmedName, existingCategories, original?.id)
        if (nameError != null || saving) return null

        saving = true
        return try {
            if (original == null) {
                repository.addCategory(Category(name = trimmedName, color = color, emoji = emoji))
            } else {
                repository.updateCategory(original.copy(name = trimmedName, color = color, emoji = emoji))
                original.id
            }
        } finally {
            saving = false
        }
    }
}

class CategoryEditorViewModelFactory(
    private val repository: BerryFlowRepository,
    private val original: Category?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CategoryEditorViewModel(repository, original) as T
    }
}