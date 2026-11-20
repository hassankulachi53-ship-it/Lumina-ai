package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppLanguage
import com.example.model.CurrencyOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.luminaDataStore: DataStore<Preferences> by preferencesDataStore(name = "lumina_prefs")

data class LocalPrefsState(
    val darkThemeEnabled: Boolean = true,
    val currency: CurrencyOption = CurrencyOption.PKR,
    val language: AppLanguage = AppLanguage.EN,
    val notificationsEnabled: Boolean = true,
    val autoSuggestEnabled: Boolean = false,
    val dailyAutoWallpaperEnabled: Boolean = false,
    val onboardingCompleted: Boolean = false
)

class UserPreferencesStore(private val context: Context) {

    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val CURRENCY = stringPreferencesKey("currency")
        val LANGUAGE = stringPreferencesKey("language")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val AUTO_SUGGEST = booleanPreferencesKey("auto_suggest")
        val DAILY_WALLPAPER = booleanPreferencesKey("daily_wallpaper")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val prefsFlow: Flow<LocalPrefsState> = context.luminaDataStore.data.map { prefs ->
        LocalPrefsState(
            darkThemeEnabled = prefs[Keys.DARK_THEME] ?: true,
            currency = CurrencyOption.fromRaw(prefs[Keys.CURRENCY]),
            language = AppLanguage.fromRaw(prefs[Keys.LANGUAGE]),
            notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
            autoSuggestEnabled = prefs[Keys.AUTO_SUGGEST] ?: false,
            dailyAutoWallpaperEnabled = prefs[Keys.DAILY_WALLPAPER] ?: false,
            onboardingCompleted = prefs[Keys.ONBOARDING_DONE] ?: false
        )
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.luminaDataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setCurrency(currency: CurrencyOption) {
        context.luminaDataStore.edit { it[Keys.CURRENCY] = currency.name }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.luminaDataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.luminaDataStore.edit { it[Keys.NOTIFICATIONS] = enabled }
    }

    suspend fun setAutoSuggestEnabled(enabled: Boolean) {
        context.luminaDataStore.edit { it[Keys.AUTO_SUGGEST] = enabled }
    }

    suspend fun setDailyAutoWallpaperEnabled(enabled: Boolean) {
        context.luminaDataStore.edit { it[Keys.DAILY_WALLPAPER] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.luminaDataStore.edit { it[Keys.ONBOARDING_DONE] = completed }
    }
}
