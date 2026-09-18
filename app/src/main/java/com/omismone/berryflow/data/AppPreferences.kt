package com.omismone.berryflow.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// User preferences (as opposed to the financial data in the Room database):
// they are not part of the JSON backup and are untouched by "erase all data".
// Values are read once at startup and kept in memory as StateFlows, so the UI
// reacts immediately, and written to SharedPreferences on every change.
class AppPreferences(private val prefs: SharedPreferences) {

    private val _darkTheme = MutableStateFlow(prefs.getBoolean(KEY_DARK_THEME, false))
    val darkTheme: StateFlow<Boolean> = _darkTheme

    private val _balanceHidden = MutableStateFlow(prefs.getBoolean(KEY_BALANCE_HIDDEN, false))
    val balanceHidden: StateFlow<Boolean> = _balanceHidden

    fun setBalanceHidden(hidden: Boolean) {
        _balanceHidden.value = hidden
        prefs.edit { putBoolean(KEY_BALANCE_HIDDEN, hidden) }
    }

    fun setDarkTheme(enabled: Boolean) {
        _darkTheme.value = enabled
        prefs.edit { putBoolean(KEY_DARK_THEME, enabled) }
    }

    companion object {
        private const val FILE_NAME = "berryflow_preferences"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_BALANCE_HIDDEN = "balance_hidden"

        @Volatile
        private var instance: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences =
            instance ?: synchronized(this) {
                instance ?: AppPreferences(
                    context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
                ).also { instance = it }
            }
    }
}