package com.omismone.berryflow.ui.theme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable

// The first composable that reads window insets makes Compose register its
// listeners (and request a new insets pass), and the last one leaving removes
// them. Keeping a reader at the app root avoids doing that every time a screen
// that uses insets opens, in the middle of its enter animation.
@Composable
@Suppress("UNUSED_EXPRESSION")
fun KeepWindowInsetsRegistered() {
    WindowInsets.navigationBars
}