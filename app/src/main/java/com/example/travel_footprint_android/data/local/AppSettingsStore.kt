package com.example.travel_footprint_android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings"
)

data class AppSettings(
    val showWeatherCard: Boolean = true,
)

@Singleton
class AppSettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val showWeatherCard = booleanPreferencesKey("show_weather_card")
    }

    val settingsFlow: Flow<AppSettings> = context.appDataStore.data.map { prefs ->
        AppSettings(
            showWeatherCard = prefs[Keys.showWeatherCard] ?: true,
        )
    }

    suspend fun updateShowWeatherCard(show: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[Keys.showWeatherCard] = show
        }
    }
}
