// app/src/main/java/com/example/travel_footprint_android/presentation/viewmodel/LightenViewModel.kt
package com.example.travel_footprint_android.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travel_footprint_android.data.dao.LightedProvince
import com.example.travel_footprint_android.data.dao.ProvinceCityCount
import com.example.travel_footprint_android.data.entity.CheckInRecordEntity
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
import kotlinx.coroutines.flow.firstOrNull
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
    // 所有省份列表（从数据库获取）
    private val _allProvinces = MutableStateFlow<List<Province>>(emptyList())
    val allProvinces: StateFlow<List<Province>> = _allProvinces.asStateFlow()

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
        startContinuousCollectors()
        startCheckInRecordCollector()
    }


    // ==================== 数据加载 ====================

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                launch { loadProvinceCityCount() }
                // 等待首次 Flow 发射以确保初始数据已加载
                appService.getAllLightedCities().first()
                appService.getLightedProvinces().first()
                loadAllProvincesList()

                _uiState.update { it.copy(isLoading = false) }
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

    // 启动所有连续 Flow 收集器（实时响应数据库变化）
    private fun startContinuousCollectors() {
        // 城市代码 - 实时响应
        loadLightedCityCodes()
        // 城市数据 - 实时响应
        viewModelScope.launch {
            appService.getAllLightedCities().collect { cities ->
                _uiState.update { state ->
                    state.copy(
                        lightedCities = cities,
                        lightedCityCount = cities.size
                    )
                }
            }
        }
        // 省份代码 + 省份数据 - 实时响应
        viewModelScope.launch {
            appService.getLightedProvinces().collect { provinces ->
                _lightedProvinceCodes.value = provinces.map { it.provinceAdcode }.toSet()
                _uiState.update { state ->
                    state.copy(
                        lightedProvinces = provinces,
                        lightedProvinceCount = provinces.size
                    )
                }
            }
        }
    }

    // 加载已点亮省份代码
    private fun loadLightedProvinceCodes() {
        viewModelScope.launch {
            appService.getLightedProvinces().collect { provinces ->
                _lightedProvinceCodes.value = provinces.map { it.provinceAdcode }.toSet()
            }
        }
    }

    private fun loadLightedCities() {
        viewModelScope.launch {
            try {
                appService.getAllLightedCities().collect { cities ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            lightedCities = cities,
                            lightedCityCount = cities.size
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("LightenViewModel", "加载点亮城市失败", e)
            }
        }
    }

    private fun loadLightedProvinces() {
        viewModelScope.launch {
            appService.getLightedProvinces().collect { provinces ->
                Log.d("LightenViewModel", "省份数量: ${provinces.size}")
                _uiState.update { state ->
                    state.copy(
                        lightedProvinces = provinces,
                        lightedProvinceCount = provinces.size
                    )
                }
            }
        }
    }

    private suspend fun loadProvinceCityCount() {
        val stats = appService.getLightedCitiesCountByProvince()
        _uiState.update { state ->
            state.copy(provinceCityCount = stats)
        }
    }

    //调度接口，根据传入对象自行选择点亮方式
    fun LightedRecord(adcode: String, name: String) {
        viewModelScope.launch {
            try {
                // 剥离 _partN 后缀（如 130200_part1 -> 130200）
                val cleanAdcode = adcode.replace(Regex("_part\\d+$"), "")
                val cleanName = name.replace(Regex("_part\\d+$"), "")

                when {
                    // 省级：XX0000 格式
                    cleanAdcode.matches(Regex("\\d{2}0000")) -> {
                        appService.lightProvince(
                            provinceAdcode = cleanAdcode,
                            provinceName = cleanName,
                            remark = "从地图选择点亮"
                        )
                    }
                    // 市级：XXYY00 格式
                    cleanAdcode.matches(Regex("\\d{4}00")) && !cleanAdcode.matches(Regex("\\d{2}0000")) -> {
                        val city = getCityInfo(cleanAdcode)
                        val provinces = appService.getAllProvinces().first()
                        val provinceName = city?.provinceAdcode?.let { adcode ->
                            provinces.find { it.adcode == adcode }?.name
                        } ?: "未知省份"

                        if (city != null) {
                            appService.lightCity(
                                cityAdcode = city.adcode,
                                cityName = city.name,
                                provinceAdcode = city.provinceAdcode,
                                provinceName = "$provinceName",
                                latitude = city.centerLat,
                                longitude = city.centerLng,
                                remark = "从地图选择点亮（城市模式）"
                            )
                        } else {
                            appService.lightCity(
                                cityAdcode = cleanAdcode,
                                cityName = cleanName,
                                provinceAdcode = "",
                                provinceName = "",
                                latitude = 0.0,
                                longitude = 0.0,
                                remark = "从地图选择点亮（城市模式）"
                            )
                        }
                    }
                }
                // 数据会自动通过 Flow 更新，无需手动刷新
            } catch (e: Exception) {
                Log.e("LightenViewModel", "点亮失败", e)
                _uiState.update { state ->
                    state.copy(error = e.message ?: "点亮失败")
                }
            }
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
// ==================== 省份相关数据加载方法（内部使用） ====================

    /**
     * 加载省份代码数据
     */
    private suspend fun loadProvinceCodes() {
        try {
            val provinces = appService.getLightedProvinces().first()
            _lightedProvinceCodes.value = provinces.map { it.provinceAdcode }.toSet()
            Log.d("LightenViewModel", "省份代码加载完成: ${_lightedProvinceCodes.value.size}个")
        } catch (e: Exception) {
            Log.e("LightenViewModel", "加载省份代码失败", e)
            throw e
        }
    }

    /**
     * 加载省份点亮数据（详细信息）
     */
    private suspend fun loadProvincesData() {
        try {
            val provinces = appService.getLightedProvinces().first()
            Log.d("LightenViewModel", "省份数量: ${provinces.size}")
            _uiState.update { state ->
                state.copy(
                    lightedProvinces = provinces,
                    lightedProvinceCount = provinces.size
                )
            }
        } catch (e: Exception) {
            Log.e("LightenViewModel", "加载省份数据失败", e)
            throw e
        }
    }

    /**
     * 加载所有省份列表（全部省份，不限于已点亮）
     */
    private suspend fun loadAllProvincesList() {
        try {
            val provinces = appService.getAllProvinces().first()
            _allProvinces.value = provinces
            Log.d("LightenViewModel", "所有省份加载完成: ${provinces.size}个")
        } catch (e: Exception) {
            Log.e("LightenViewModel", "加载所有省份失败", e)
            throw e
        }
    }

// ==================== 城市相关数据加载方法（内部使用） ====================

    /**
     * 加载城市代码数据
     */
    private suspend fun loadCityCodes() {
        try {
            val cities = appService.getAllLightedCities().first()
            _lightedCityCodes.value = cities.map { it.cityAdcode }.toSet()
            Log.d("LightenViewModel", "城市代码加载完成: ${_lightedCityCodes.value.size}个")
        } catch (e: Exception) {
            Log.e("LightenViewModel", "加载城市代码失败", e)
            throw e
        }
    }

    /**
     * 加载城市点亮数据（详细信息）
     */
    private suspend fun loadCitiesData() {
        try {
            val cities = appService.getAllLightedCities().first()
            _uiState.update { state ->
                state.copy(
                    lightedCities = cities,
                    lightedCityCount = cities.size
                )
            }
            Log.d("点亮城市数量", "${cities.size}")
        } catch (e: Exception) {
            Log.e("LightenViewModel", "加载城市数据失败", e)
            throw e
        }
    }

    /**
     * 加载某个省份下的所有城市
     */
    private suspend fun loadCitiesByProvince(provinceAdcode: String): List<City> {
        return try {
            val cities = appService.getCitiesByProvince(provinceAdcode).first()
            Log.d("LightenViewModel", "省份${provinceAdcode}下的城市: ${cities.size}个")
            cities
        } catch (e: Exception) {
            Log.e("LightenViewModel", "加载省份城市失败", e)
            emptyList()
        }
    }

// ==================== 统计数据加载方法（内部使用） ====================

    /**
     * 加载各省份的城市点亮统计
     */
    private suspend fun loadProvinceStatistics() {
        try {
            val stats = appService.getLightedCitiesCountByProvince()
            _uiState.update { state ->
                state.copy(provinceCityCount = stats)
            }
            Log.d("LightenViewModel", "省份统计数据加载完成")
        } catch (e: Exception) {
            Log.e("LightenViewModel", "加载省份统计数据失败", e)
            throw e
        }
    }

// ==================== 公开的刷新方法 ====================

// ---- 省份相关公开方法 ----

    /**
     * 刷新省份相关数据（代码 + 数据 + 统计）
     */
    fun refreshProvinceRelatedData() {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true, error = null) }

            try {
                val deferredResults = listOf(
                    async { loadProvinceCodes() },
                    async { loadProvincesData() },
                    async { loadProvinceStatistics() }
                )
                deferredResults.awaitAll()

                _uiState.update { state -> state.copy(isLoading = false) }
                Log.d("LightenViewModel", "省份数据刷新完成")
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(isLoading = false, error = e.message ?: "刷新省份数据失败")
                }
                Log.e("LightenViewModel", "刷新省份数据失败", e)
            }
        }
    }

    /**
     * 只刷新省份代码（用于快速判断省份是否点亮）
     */
    fun refreshProvinceCodesOnly() {
        viewModelScope.launch {
            try {
                loadProvinceCodes()
                Log.d("LightenViewModel", "省份代码刷新完成")
            } catch (e: Exception) {
                Log.e("LightenViewModel", "刷新省份代码失败", e)
            }
        }
    }

    /**
     * 只刷新省份点亮数据（详细信息）
     */
    fun refreshProvincesDataOnly() {
        viewModelScope.launch {
            try {
                loadProvincesData()
                Log.d("LightenViewModel", "省份数据刷新完成")
            } catch (e: Exception) {
                Log.e("LightenViewModel", "刷新省份数据失败", e)
            }
        }
    }

// ---- 城市相关公开方法 ----

    /**
     * 刷新城市相关数据（代码 + 数据）
     */
    fun refreshCityRelatedData() {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true, error = null) }

            try {
                val deferredResults = listOf(
                    async { loadCityCodes() },
                    async { loadCitiesData() }
                )
                deferredResults.awaitAll()

                _uiState.update { state -> state.copy(isLoading = false) }
                Log.d("LightenViewModel", "城市数据刷新完成")
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(isLoading = false, error = e.message ?: "刷新城市数据失败")
                }
                Log.e("LightenViewModel", "刷新城市数据失败", e)
            }
        }
    }

    /**
     * 只刷新城市代码（用于快速判断城市是否点亮）
     */
    fun refreshCityCodesOnly() {
        viewModelScope.launch {
            try {
                loadCityCodes()
                Log.d("LightenViewModel", "城市代码刷新完成")
            } catch (e: Exception) {
                Log.e("LightenViewModel", "刷新城市代码失败", e)
            }
        }
    }

    /**
     * 只刷新城市点亮数据（详细信息）
     */
    fun refreshCitiesDataOnly() {
        viewModelScope.launch {
            try {
                loadCitiesData()
                Log.d("LightenViewModel", "城市数据刷新完成")
            } catch (e: Exception) {
                Log.e("LightenViewModel", "刷新城市数据失败", e)
            }
        }
    }

// ---- 综合刷新方法 ----

    /**
     * 刷新所有数据（完整刷新）
     */
    fun refreshAllData() {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true, error = null) }

            try {
                val deferredResults = listOf(
                    // 省份相关
                    async { loadProvinceCodes() },
                    async { loadProvincesData() },
                    // 城市相关
                    async { loadCityCodes() },
                    async { loadCitiesData() },
                    // 统计相关
                    async { loadProvinceStatistics() }
                )
                deferredResults.awaitAll()

                _uiState.update { state -> state.copy(isLoading = false) }
                Log.d("LightenViewModel", "所有数据刷新完成")
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(isLoading = false, error = e.message ?: "刷新失败")
                }
                Log.e("LightenViewModel", "刷新所有数据失败", e)
            }
        }
    }

    /**
     * 刷新省份和城市相关数据（不包括统计）
     */
    fun refreshProvinceAndCityData() {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true, error = null) }

            try {
                val deferredResults = listOf(
                    // 省份相关
                    async { loadProvinceCodes() },
                    async { loadProvincesData() },
                    // 城市相关
                    async { loadCityCodes() },
                    async { loadCitiesData() }
                )
                deferredResults.awaitAll()

                _uiState.update { state -> state.copy(isLoading = false) }
                Log.d("LightenViewModel", "省份和城市数据刷新完成")
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(isLoading = false, error = e.message ?: "刷新失败")
                }
                Log.e("LightenViewModel", "刷新省份和城市数据失败", e)
            }
        }
    }

// ==================== 查询方法（按类型区分） ====================

    // 省份查询
    fun isProvinceLighted(provinceAdcode: String): Boolean {
        return _lightedProvinceCodes.value.contains(provinceAdcode)
    }

    fun getLightedProvinceCount(): Int {
        return _uiState.value.lightedProvinceCount
    }

    fun getAllProvinces(): Flow<List<Province>> {
        return appService.getAllProvinces()
    }

    // 城市查询
    fun isCityLighted(cityAdcode: String): Boolean {
        return _lightedCityCodes.value.contains(cityAdcode)
    }

    fun getLightedCityCount(): Int {
        return _uiState.value.lightedCityCount
    }

    fun getCitiesByProvince(provinceAdcode: String): Flow<List<City>> {
        return if (provinceAdcode.isBlank()) {
            flowOf(emptyList())
        } else {
            appService.getCitiesByProvince(provinceAdcode)
        }
    }

    // 统计查询
    fun getProvinceCityCount(provinceAdcode: String): Int {
        return _uiState.value.provinceCityCount
            .find { it.provinceAdcode == provinceAdcode }
            ?.cityCount ?: 0
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

    // ==================== 打卡记录相关 ====================

    private val _checkInRecords = MutableStateFlow<List<CheckInRecordEntity>>(emptyList())
    val checkInRecords: StateFlow<List<CheckInRecordEntity>> = _checkInRecords.asStateFlow()

    fun addCheckInRecord(cityAdcode: String, cityName: String, note: String, tags: List<String> = emptyList()) {
        viewModelScope.launch {
            try {
                appService.addCheckInRecord(cityAdcode, cityName, note, tags)
                // 数据会自动通过 Flow 更新，无需手动刷新
            } catch (e: Exception) {
                Log.e("LightenViewModel", "添加打卡记录失败", e)
            }
        }
    }

    fun deleteCheckInRecordsByCity(adcode: String) {
        viewModelScope.launch {
            try {
                appService.deleteCheckInRecordsByCity(adcode)
            } catch (e: Exception) {
                Log.e("LightenViewModel", "删除打卡记录失败", e)
            }
        }
    }

    fun startCheckInRecordCollector() {
        viewModelScope.launch {
            appService.getAllCheckInRecords().collect { records ->
                _checkInRecords.value = records
            }
        }
    }
}