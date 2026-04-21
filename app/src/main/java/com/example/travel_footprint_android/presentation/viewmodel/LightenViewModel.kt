// app/src/main/java/com/example/travel_footprint_android/presentation/viewmodel/LightenViewModel.kt
package com.example.travel_footprint_android.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.data.dao.ProvinceCityCount
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.data.entity.Province
import com.example.travel_footprint_android.domain.usecase.AppService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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
        val error: String? = null,
        // 获取所有省份（从数据库）

    )


    // 获取所有省份（从数据库）
    val allProvinces: Flow<List<Province>> = appService.getAllProvinces()

    // 已点亮城市代码集合
    private val _lightedCityCodes = MutableStateFlow<Set<String>>(emptySet())
    val lightedCityCodes: StateFlow<Set<String>> = _lightedCityCodes.asStateFlow()

    // 已点亮省份代码集合
    private val _lightedProvinceCodes = MutableStateFlow<Set<String>>(emptySet())
    val lightedProvinceCodes: StateFlow<Set<String>> = _lightedProvinceCodes.asStateFlow()


    private val _uiState = MutableStateFlow(LightenUiState())
    val uiState: StateFlow<LightenUiState> = _uiState.asStateFlow()


    //初始化加载数据
    init {
        loadAllData()
        loadLightedCityCodes()      // 添加这行
        loadLightedProvinceCodes()  // 添加这行
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

    // 加载已点亮城市代码
    private fun loadLightedCityCodes() {
        viewModelScope.launch {
            appService.getAllLightedCities().collect { cities ->
                _lightedCityCodes.value = cities.map { it.cityAdcode }.toSet()
            }
        }
    }

    // 加载已点亮省份代码
    private fun loadLightedProvinceCodes() {
        viewModelScope.launch {
            val provinces = appService.getLightedProvinces()
            _lightedProvinceCodes.value = provinces.map { it.provinceAdcode }.toSet()
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
        Log.d("LightenViewModel", "省份数量: ${provinces.size}")
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
                //重新加载已经点亮省份以及城市代码
                loadLightedCityCodes()
                loadLightedProvinceCodes()
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
                refreshAllData()
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message ?: "取消点亮失败")
                }
            }
        }
    }

    // 点亮省份
    fun lightProvince(province: Province) {
        viewModelScope.launch {
            try {
                val result = appService.lightProvince(
                    provinceAdcode = province.adcode,
                    provinceName = province.name
                )
                if (result > 0) {
                    loadLightedProvinceCodes()
                    loadLightedProvinces()
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message ?: "点亮省份失败")
                }
            }
        }
    }

    fun unlightProvince(provinceAdcode: String) {
        viewModelScope.launch {
            try {
                appService.unlightProvince(provinceAdcode)
                // 刷新数据
                refreshAllData()
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message ?: "取消点亮失败")
                }
            }
        }
    }

    // ==================== 批量保存变更 ====================

    /**
     * 应用点亮变更（保存时调用）
     * @param selectedCityCodes 新增点亮的城市代码
     * @param unselectedCityCodes 取消点亮的城市代码
     * @param selectedProvinceCodes 新增点亮的省份代码
     * @param unselectedProvinceCodes 取消点亮的省份代码
     */
    fun applyLightingChanges(
        selectedCityCodes: Set<String>,
        unselectedCityCodes: Set<String>,
        selectedProvinceCodes: Set<String>,
        unselectedProvinceCodes: Set<String>
    ) {
        viewModelScope.launch {
            try {
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

            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(error = e.message ?: "保存失败")
                }
            }
        }
    }

    /**
     * 刷新所有数据
     */
    fun refreshAllData() {
        viewModelScope.launch {
            loadLightedCityCodes()
            loadLightedProvinceCodes()
            loadLightedCities()
            loadLightedProvinces()
            loadProvinceCityCount()
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

    fun getCitiesByProvince(provinceAdcode: String): Flow<List<com.example.travel_footprint_android.data.entity.City>> {
        return if (provinceAdcode.isBlank()) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            appService.getCitiesByProvince(provinceAdcode)
        }
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