package com.omismone.berryflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.core.view.WindowCompat
import com.omismone.berryflow.data.AppContainer
import com.omismone.berryflow.ui.navigation.BerryFlowApp
import com.omismone.berryflow.ui.theme.Typography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        setContent {
            MaterialTheme(typography = Typography) {
                BerryFlowApp(AppContainer.getRepository(applicationContext))
            }
        }
    }
}