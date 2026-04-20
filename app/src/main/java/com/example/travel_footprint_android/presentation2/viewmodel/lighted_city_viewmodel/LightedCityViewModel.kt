package com.example.travel_footprint_android.presentation2.viewmodel.lighted_city_viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.domain.usecase.AppService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LightedCityViewModel @Inject constructor(
    private val appService: AppService
) : ViewModel() {
    val allLightedCities: StateFlow<List<LightedCity>> =
        appService.getAllLightedCities()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _cityCount = MutableStateFlow(0)
    val cityCount: StateFlow<Int> = _cityCount.asStateFlow()

    init {
        loadCityCount()
    }

    private fun loadCityCount() {
        viewModelScope.launch {
            _cityCount.value = appService.getLightedCityCount()
        }
    }

    fun lightCity(
        cityAdcode: String,
        cityName: String,
        provinceAdcode: String,
        provinceName: String,
        latitude: Double,
        longitude: Double,
        remark: String = ""
    ) {
        viewModelScope.launch {
            appService.lightCity(cityAdcode, cityName, provinceAdcode, provinceName, latitude, longitude, remark)
            loadCityCount()
        }
    }

    fun unlightCity(cityAdcode: String) {
        viewModelScope.launch {
            appService.unlightCity(cityAdcode)
            loadCityCount()
        }
    }

    suspend fun isCityLighted(cityAdcode: String): Boolean {
        return appService.isCityLighted(cityAdcode)
    }
}