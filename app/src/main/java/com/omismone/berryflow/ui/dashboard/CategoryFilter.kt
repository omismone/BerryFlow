package com.omismone.berryflow.ui.dashboard

import com.omismone.berryflow.data.Category

// Tapping a category square while that category is already the filter removes
// the filter; otherwise it becomes the filter.
fun toggleCategoryFilter(current: Long?, tapped: Long): Long? =
    if (current == tapped) null else tapped

// The category the filter refers to, or null when there is no filter or the
// category no longer exists (e.g. it was deleted while filtering).
fun resolveCategoryFilter(filterId: Long?, categories: List<Category>): Category? =
    filterId?.let { id -> categories.firstOrNull { it.id == id } }