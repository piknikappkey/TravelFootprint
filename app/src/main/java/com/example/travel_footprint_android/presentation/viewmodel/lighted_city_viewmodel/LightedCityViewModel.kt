package com.example.travel_footprint_android.presentation.viewmodel.lighted_city_viewmodel

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

    // 所有已点亮城市
    val allLightedCities: StateFlow<List<LightedCity>> =
        appService.getAllLightedCities()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // 已点亮城市数量
    private val _cityCount = MutableStateFlow(0)
    val cityCount: StateFlow<Int> = _cityCount.asStateFlow()

    // 已点亮城市代码集合
    private val _lightedCityCodes = MutableStateFlow<Set<String>>(emptySet())
    val lightedCityCodes: StateFlow<Set<String>> = _lightedCityCodes.asStateFlow()

    // 已点亮省份代码集合
    private val _lightedProvinceCodes = MutableStateFlow<Set<String>>(emptySet())
    val lightedProvinceCodes: StateFlow<Set<String>> = _lightedProvinceCodes.asStateFlow()

    // 已点亮省份列表
    private val _lightedProvinces = MutableStateFlow<List<com.example.travel_footprint_android.data.dao.LightedProvince>>(emptyList())
    val lightedProvinces: StateFlow<List<com.example.travel_footprint_android.data.dao.LightedProvince>> = _lightedProvinces.asStateFlow()

    init {
        loadCityCount()
        loadLightedCityCodes()
        loadLightedCities()
        loadLightedProvinceCodes()
        loadLightedProvinces()
    }

    private fun loadCityCount() {
        viewModelScope.launch {
            _cityCount.value = appService.getLightedCityCount()
        }
    }

    private fun loadLightedCityCodes() {
        viewModelScope.launch {
            appService.getAllLightedCities().collect { cities ->
                _lightedCityCodes.value = cities.map { it.cityAdcode }.toSet()
            }
        }
    }

    private fun loadLightedCities() {
        viewModelScope.launch {
            appService.getAllLightedCities().collect { cities ->
                // 已经通过 allLightedCities 暴露，这里不需要额外处理
            }
        }
    }

    private fun loadLightedProvinceCodes() {
        viewModelScope.launch {
            appService.getLightedProvinces().collect { provinces ->
                _lightedProvinceCodes.value = provinces.map { it.provinceAdcode }.toSet()
            }
        }
    }

    private fun loadLightedProvinces() {
        viewModelScope.launch {
            appService.getLightedProvinces().collect { provinces ->
                _lightedProvinces.value = provinces
            }
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
            refreshAllData()
        }
    }

    fun lightCity(city: com.example.travel_footprint_android.data.entity.City) {
        viewModelScope.launch {
            appService.lightCity(
                cityAdcode = city.adcode,
                cityName = city.name,
                provinceAdcode = city.provinceAdcode,
                provinceName = "",
                latitude = city.centerLat,
                longitude = city.centerLng
            )
            refreshAllData()
        }
    }

    fun unlightCity(cityAdcode: String) {
        viewModelScope.launch {
            appService.unlightCity(cityAdcode)
            refreshAllData()
        }
    }

    fun lightProvince(province: com.example.travel_footprint_android.data.entity.Province) {
        viewModelScope.launch {
            appService.lightProvince(
                provinceAdcode = province.adcode,
                provinceName = province.name
            )
            refreshAllData()
        }
    }

    fun unlightProvince(provinceAdcode: String) {
        viewModelScope.launch {
            appService.unlightProvince(provinceAdcode)
            refreshAllData()
        }
    }

    suspend fun isCityLighted(cityAdcode: String): Boolean {
        return appService.isCityLighted(cityAdcode)
    }

    fun applyLightingChanges(
        selectedCityCodes: Set<String>,
        unselectedCityCodes: Set<String>,
        selectedProvinceCodes: Set<String>,
        unselectedProvinceCodes: Set<String>
    ) {
        viewModelScope.launch {
            // 1. 新增点亮城市
            selectedCityCodes.forEach { cityCode ->
                val city = appService.getCityByAdcode(cityCode)
                city?.let {
                    appService.lightCity(
                        cityAdcode = it.adcode,
                        cityName = it.name,
                        provinceAdcode = it.provinceAdcode,
                        provinceName = "",
                        latitude = it.centerLat,
                        longitude = it.centerLng
                    )
                }
            }

            // 2. 取消点亮城市
            unselectedCityCodes.forEach { cityCode ->
                appService.unlightCity(cityCode)
            }

            // 3. 新增点亮省份
            selectedProvinceCodes.forEach { provinceCode ->
                val province = appService.getProvinceByAdcode(provinceCode)
                province?.let {
                    appService.lightProvince(
                        provinceAdcode = it.adcode,
                        provinceName = it.name
                    )
                }
            }

            // 4. 取消点亮省份
            unselectedProvinceCodes.forEach { provinceCode ->
                appService.unlightProvince(provinceCode)
            }

            // 5. 刷新所有数据
            refreshAllData()
        }
    }

    private fun refreshAllData() {
        viewModelScope.launch {
            loadCityCount()
            loadLightedCityCodes()
            loadLightedProvinceCodes()
            loadLightedProvinces()
        }
    }
}