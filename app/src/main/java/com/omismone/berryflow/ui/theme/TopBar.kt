package com.omismone.berryflow.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Layout shared by the top bar of every screen (and the Dashboard's button row),
// so buttons and titles sit in the same place when navigating.
object TopBarDefaults {
    // Space between the bottom of the status bar and the buttons.
    val TopGap = 40.dp

    // Screen edge to the 48dp touch target of the first/last button.
    val ButtonsHorizontalPadding = 12.dp

    // Horizontal margin of titles and screen content.
    val ContentHorizontalPadding = 20.dp

    // Space between the buttons and the title, and between the title and the
    // content below it.
    val TitleTopGap = 16.dp
    val TitleBottomGap = 8.dp

    val IconSize = 25.dp
    val ButtonSize = 48.dp
}

// Places a top row of buttons below the real status bar (safe area) with the
// shared gap and edge padding.
fun Modifier.topBarInsets(): Modifier = this
    .statusBarsPadding()
    .padding(
        top = TopBarDefaults.TopGap,
        start = TopBarDefaults.ButtonsHorizontalPadding,
        end = TopBarDefaults.ButtonsHorizontalPadding
    )

enum class TopBarNav(val contentDescription: String) {
    Back("Back"),
    Home("Back to Dashboard"),
    None("")
}

// Top bar of every screen except the Dashboard: navigation button on the
// left, action buttons on the right, and the screen title below.
@Composable
fun ScreenTopBar(
    title: String,
    nav: TopBarNav,
    onNavClick: () -> Unit = {},
    navContentDescription: String = nav.contentDescription,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .topBarInsets(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (nav) {
                // Keeps the action buttons on the right.
                TopBarNav.None -> Spacer(modifier = Modifier.size(TopBarDefaults.ButtonSize))
                TopBarNav.Back -> TopBarIconButton(Icons.AutoMirrored.Filled.ArrowBack, navContentDescription, onNavClick)
                TopBarNav.Home -> TopBarIconButton(Icons.Default.Home, navContentDescription, onNavClick)
            }

            Row(verticalAlignment = Alignment.CenterVertically, content = actions)
        }

        Text(
            text = title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.primaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = TopBarDefaults.ContentHorizontalPadding,
                    end = TopBarDefaults.ContentHorizontalPadding,
                    top = TopBarDefaults.TitleTopGap,
                    bottom = TopBarDefaults.TitleBottomGap
                )
        )
    }
}

@Composable
fun TopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = AppTheme.colors.secondaryText
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(TopBarDefaults.IconSize)
        )
    }
}