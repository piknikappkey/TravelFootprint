package com.example.travel_footprint_android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.travel_footprint_android.presentation.components.image_random.viewmodel.ImageRainSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.imageRainDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "image_rain_settings"
)

@Singleton
class ImageRainSettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val rainEnabled = booleanPreferencesKey("rain_enabled")
        val isChaos = booleanPreferencesKey("is_chaos")
        val maxImages = intPreferencesKey("max_images")
        val intervalMs = longPreferencesKey("interval_ms")
        val minExistenceTime = intPreferencesKey("min_existence_time")
        val maxExistenceTime = intPreferencesKey("max_existence_time")
        val minSize = intPreferencesKey("min_size")
        val maxSize = intPreferencesKey("max_size")
        val minAngle = intPreferencesKey("min_angle")
        val maxAngle = intPreferencesKey("max_angle")
        val pressScale = floatPreferencesKey("press_scale")
        val rotationSpeed = floatPreferencesKey("rotation_speed")
        val clickEnabled = booleanPreferencesKey("click_enabled")
        val pressEnabled = booleanPreferencesKey("press_enabled")
    }

    val settingsFlow: Flow<ImageRainSettings> = context.imageRainDataStore.data.map { prefs ->
        ImageRainSettings(
            rainEnabled = prefs[Keys.rainEnabled] ?: true,
            isChaos = prefs[Keys.isChaos] ?: false,
            maxImages = prefs[Keys.maxImages] ?: 10,
            intervalMs = prefs[Keys.intervalMs] ?: 1000L,
            minExistenceTime = prefs[Keys.minExistenceTime] ?: 10000,
            maxExistenceTime = prefs[Keys.maxExistenceTime] ?: 20000,
            minSize = prefs[Keys.minSize] ?: 30,
            maxSize = prefs[Keys.maxSize] ?: 50,
            minAngle = prefs[Keys.minAngle] ?: 0,
            maxAngle = prefs[Keys.maxAngle] ?: 360,
            pressScale = prefs[Keys.pressScale] ?: 20f,
            rotationSpeed = prefs[Keys.rotationSpeed] ?: 30f,
            clickEnabled = prefs[Keys.clickEnabled] ?: true,
            pressEnabled = prefs[Keys.pressEnabled] ?: true,
        )
    }

    suspend fun saveSettings(settings: ImageRainSettings) {
        context.imageRainDataStore.edit { prefs ->
            prefs[Keys.rainEnabled] = settings.rainEnabled
            prefs[Keys.isChaos] = settings.isChaos
            prefs[Keys.maxImages] = settings.maxImages
            prefs[Keys.intervalMs] = settings.intervalMs
            prefs[Keys.minExistenceTime] = settings.minExistenceTime
            prefs[Keys.maxExistenceTime] = settings.maxExistenceTime
            prefs[Keys.minSize] = settings.minSize
            prefs[Keys.maxSize] = settings.maxSize
            prefs[Keys.minAngle] = settings.minAngle
            prefs[Keys.maxAngle] = settings.maxAngle
            prefs[Keys.pressScale] = settings.pressScale
            prefs[Keys.rotationSpeed] = settings.rotationSpeed
            prefs[Keys.clickEnabled] = settings.clickEnabled
            prefs[Keys.pressEnabled] = settings.pressEnabled
        }
    }
}
