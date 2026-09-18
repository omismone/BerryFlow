package com.omismone.berryflow.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

fun Modifier.clickableNoRipple(onClickLabel: String? = null, onClick: () -> Unit): Modifier = composed {
    clickable(
        onClickLabel = onClickLabel,
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
}