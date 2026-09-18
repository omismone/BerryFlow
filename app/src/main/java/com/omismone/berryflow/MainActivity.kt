package com.omismone.berryflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat
import com.omismone.berryflow.data.AppContainer
import com.omismone.berryflow.data.AppPreferences
import com.omismone.berryflow.ui.navigation.BerryFlowApp
import com.omismone.berryflow.ui.theme.BerryFlowTheme
import com.omismone.berryflow.ui.theme.DarkAppColors
import com.omismone.berryflow.ui.theme.LightAppColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = AppPreferences.getInstance(applicationContext)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        setContent {
            val darkTheme by preferences.darkTheme.collectAsState()

            // Everything outside Compose follows the theme too: the window
            // behind the UI and the status/navigation bar icons.
            SideEffect {
                val colors = if (darkTheme) DarkAppColors else LightAppColors
                window.setBackgroundDrawable(colors.background.toArgb().toDrawable())
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }

            BerryFlowTheme(darkTheme = darkTheme) {
                BerryFlowApp(AppContainer.getRepository(applicationContext), preferences)
            }
        }
    }
}