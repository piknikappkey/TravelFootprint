// app/src/main/java/com/example/travel_footprint_android/presentation/viewmodel/LightenViewModel.kt
package com.example.travel_footprint_android.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.data.dao.ProvinceCityCount
import com.example.travel_footprint_android.data.entity.City
import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.data.entity.Province
import com.example.travel_footprint_android.domain.usecase.AppService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LightenViewModel @Inject constructor(
    private val appService: AppService
) : ViewModel() {

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
        try {
            val cities = appService.getAllLightedCities().first()  // 改用 first()
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    lightedCities = cities,
                    lightedCityCount = cities.size
                )
            }
            Log.d("点亮城市数量","${cities.size}")
        } catch (e: Exception) {
            Log.e("LightenViewModel", "加载点亮城市失败", e)
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

    //调度接口，根据传入对象自行选择点亮方式
    fun LightedRecord(adcode: String,name: String){
        viewModelScope.launch {
            when {
                // 省级：XX0000 格式
                adcode.matches(Regex("\\d{2}0000")) -> {
                    lightProvince(
                        provinceName = name,
                        provinceAdcode = adcode,
                        remark = "从地图选择点亮"
                    )
                }
                // 市级：XXYY00 格式
                adcode.matches(Regex("\\d{4}00")) && !adcode.matches(Regex("\\d{2}0000")) -> {
                    lightCityByAdcode(
                        cityAdcode = adcode,
                        cityName = name
                    )
                }
            }
            refreshAllData()
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
                Log.d("独立点亮","$cityName")
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

    // 点亮省份 - 简化版，直接使用 adcode 和 name
    fun lightProvince(provinceAdcode: String, provinceName: String, remark: String = "") {
        viewModelScope.launch {
            try {
                val result = appService.lightProvince(
                    provinceAdcode = provinceAdcode,
                    provinceName = provinceName,
                    remark = remark
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

    //点亮城市通过城市码
    fun lightCityByAdcode(
        cityAdcode: String,
        cityName: String
    ) {
        viewModelScope.launch {
            // 尝试从已缓存的城市数据中获取完整信息
            val city = getCityInfo(cityAdcode)
            /**
             *  val adcode: String,        // 城市代码 (如: 110100)
             *     val name: String,          // 城市名称 (如: 北京市)
             *     val provinceAdcode: String,// 所属省份代码
             *     val centerLat: Double,     // 中心纬度
             *     val centerLng: Double,     // 中心经度
             *     val sortOrder: Int = 0     // 排序顺序
             */
            if (city != null) {
                lightCity(
                    cityAdcode = city.adcode,
                    cityName = city.name,
                    provinceAdcode = city.provinceAdcode,
                    provinceName = "默认省份",
                    latitude = city.centerLat,
                    longitude = city.centerLng,
                    remark = "从地图选择点亮（城市模式）"
                )
            } else {
                // 降级：只保存基本信息
                lightCity(
                    cityAdcode = cityAdcode,
                    cityName = cityName,
                    provinceAdcode = "",
                    provinceName = "",
                    latitude = 0.0,
                    longitude = 0.0,
                    remark = "从地图选择点亮（城市模式）"
                )
            }
        }
    }

    // 添加一个简单的城市缓存
    private val cityCache = mutableMapOf<String, City>()

    fun cacheCity(city: City) {
        cityCache[city.adcode] = city
    }

    private suspend fun getCityInfo(adcode: String): City? {
        // 先从缓存获取
        cityCache[adcode]?.let { return it }
        // 再从数据库获取
        return appService.getCityByAdcode(adcode)
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
            _uiState.update { state ->
                state.copy(isLoading = true)  // 添加加载状态
            }

            try {
                // 并发执行所有加载任务
                val deferredResults = listOf(
                    async { loadLightedCityCodes() },
                    async { loadLightedProvinceCodes() },
                    async { loadLightedCities() },
                    async { loadLightedProvinces() },
                    async { loadProvinceCityCount() }
                )

                // 等待所有任务完成
                deferredResults.awaitAll()

                _uiState.update { state ->
                    state.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(isLoading = false, error = e.message)
                }
                Log.e("LightenViewModel", "Refresh failed", e)
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

    fun getCitiesByProvince(provinceAdcode: String): Flow<List<City>> {
        return if (provinceAdcode.isBlank()) {
            flowOf(emptyList())
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