// app/src/main/java/com/example/travel_footprint_android/presentation/viewmodel/LightenViewModel.kt
package com.example.travel_footprint_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.data.dao.ProvinceCityCount
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.domain.usecase.AppService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LightenViewModel @Inject constructor(
    private val appService: AppService
) : ViewModel() {

    // 删除重复定义的数据类，改为从 AppService 导入
    // import com.example.travel_footprint_android.data.repository.LightedProvince
    // import com.example.travel_footprint_android.data.repository.ProvinceCityCount

    /**
     * 面板状态
     */
    enum class PanelState {
        COLLAPSED,   // 收起状态
        EXPANDED,    // 展开状态
        EDIT_MODE    // 编辑模式
    }

    /**
     * UI 状态
     */
    data class LightenUiState(
        val isLoading: Boolean = false,
        // 点亮城市
        val lightedCities: List<LightedCity> = emptyList(),
        val lightedCityCount: Int = 0,
        // 点亮省份（使用 AppService 返回的类型）
        val lightedProvinces: List<LightedProvince> = emptyList(),
        val lightedProvinceCount: Int = 0,
        // 省份统计（使用 AppService 返回的类型）
        val provinceCityCount: List<ProvinceCityCount> = emptyList(),
        // 编辑模式
        val selectedCities: Set<String> = emptySet(),
        val lightedTime: String = "",
        val panelState: PanelState = PanelState.COLLAPSED,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(LightenUiState())
    val uiState: StateFlow<LightenUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    // ==================== 数据加载 ====================

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 并行加载所有数据
                launch { loadLightedCities() }
                launch { loadLightedProvinces() }
                launch { loadProvinceCityCount() }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "加载失败"
                    )
                }
            }
        }
    }

    private suspend fun loadLightedCities() {
        appService.getAllLightedCities().collect { cities ->
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    lightedCities = cities,
                    lightedCityCount = cities.size
                )
            }
        }
    }

    private suspend fun loadLightedProvinces() {
        val provinces = appService.getLightedProvinces()
        val provinceCount = appService.getLightedProvinceCount()
        _uiState.update { state ->
            state.copy(
                lightedProvinces = provinces,
                lightedProvinceCount = provinceCount
            )
        }
    }

    private suspend fun loadProvinceCityCount() {
        val stats = appService.getLightedCitiesCountByProvince()
        _uiState.update { state ->
            state.copy(provinceCityCount = stats)
        }
    }

    // ==================== 点亮/取消点亮 ====================

    fun lightCity(
        cityName: String,
        cityAdcode: String,
        provinceName: String,
        provinceAdcode: String,
        latitude: Double,
        longitude: Double,
        remark: String = ""
    ) {
        viewModelScope.launch {
            try {
                appService.lightCity(
                    cityAdcode = cityAdcode,
                    cityName = cityName,
                    provinceAdcode = provinceAdcode,
                    provinceName = provinceName,
                    latitude = latitude,
                    longitude = longitude,
                    remark = remark
                )
                // 数据会自动通过 Flow 更新，不需要手动刷新
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message ?: "点亮失败")
                }
            }
        }
    }

    fun unlightCity(cityAdcode: String) {
        viewModelScope.launch {
            try {
                appService.unlightCity(cityAdcode)
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message ?: "取消点亮失败")
                }
            }
        }
    }

    // ==================== 查询方法 ====================

    fun isCityLighted(cityAdcode: String): Boolean {
        return _uiState.value.lightedCities.any { it.cityAdcode == cityAdcode }
    }

    fun isProvinceLighted(provinceAdcode: String): Boolean {
        return _uiState.value.lightedProvinces.any { it.provinceAdcode == provinceAdcode }
    }

    fun getCityCountByProvince(provinceAdcode: String): Int {
        return _uiState.value.provinceCityCount
            .find { it.provinceAdcode == provinceAdcode }
            ?.cityCount ?: 0
    }

    fun getCitiesByProvince(provinceAdcode: String): List<LightedCity> {
        return _uiState.value.lightedCities.filter { it.provinceAdcode == provinceAdcode }
    }

    // ==================== UI 状态控制 ====================

    fun updatePanelState(state: PanelState) {
        _uiState.update { it.copy(panelState = state) }
    }

    fun enterEditMode() {
        val currentSelected = _uiState.value.lightedCities.map { it.cityAdcode }.toSet()
        _uiState.update { state ->
            state.copy(
                panelState = PanelState.EDIT_MODE,
                selectedCities = currentSelected
            )
        }
    }

    fun exitEditMode() {
        _uiState.update { state ->
            state.copy(
                panelState = PanelState.COLLAPSED,
                selectedCities = emptySet()
            )
        }
    }

    fun toggleCitySelection(cityAdcode: String) {
        val current = _uiState.value.selectedCities.toMutableSet()
        if (current.contains(cityAdcode)) {
            current.remove(cityAdcode)
        } else {
            current.add(cityAdcode)
        }
        _uiState.update { it.copy(selectedCities = current) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refresh() {
        loadAllData()
    }
}