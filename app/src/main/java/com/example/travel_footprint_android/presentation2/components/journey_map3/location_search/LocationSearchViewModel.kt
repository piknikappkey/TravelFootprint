package com.example.travel_footprint_android.presentation2.components.journey_map3.location_search

/**
 * LocationSearchViewModel - 地点搜索 ViewModel
 *
 * ====== 用途 ======
 * 本文件是"地点搜索功能"的 ViewModel 层，负责管理与地点搜索相关的所有业务逻辑和 UI 状态。
 * 用户通过搜索框输入关键词 → 获取搜索建议列表 → 选择建议或当前位置 → 获取选中地点的经纬度坐标，
 * 整个过程的状态均由此 ViewModel 统一管理。
 *
 * ====== 主要功能 ======
 * 1. 关键词搜索建议 —— 使用高德 Inputtips API，根据用户输入的关键词实时返回地点建议列表。
 * 2. 地理编码（地址 → 坐标）—— 用户选择搜索建议时，若该建议不含坐标信息，
 *    则通过 GeocodeSearch.getFromLocationNameAsyn() 将地址文本转为经纬度。
 * 3. 反地理编码（坐标 → 地址）—— 用户选择"当前定位"时，
 *    通过 GeocodeSearch.getFromLocationAsyn() 将经纬度转为详细地址文本。
 * 4. UI 状态管理 —— 通过 5 组 StateFlow 对外暴露搜索文本、建议列表、选中位置、建议列表显隐、搜索中状态。
 *
 * ====== 关联组件 ======
 * - Composable 搜索界面（如 LocationSearchDialog / LocationSearchSheet）：
 *   通过 collectAsState() 收集本 ViewModel 的 StateFlow，驱动 UI 渲染。
 * - 父级地图 Composable（如 JourneyMapScreen）：
 *   通过 setOnLocationSelectedCallback() 注册回调，接收选中地点的 LatLng 以在地图上标记。
 *
 * ====== 简单实现逻辑 ======
 * 1. 使用 @HiltViewModel 注解，通过 Hilt 自动注入 Application 实例。
 * 2. 继承 AndroidViewModel 以获取 Application Context，用于初始化高德 SDK 服务。
 * 3. 使用 MutableStateFlow + 公有的不可变 StateFlow（asStateFlow()）对外暴露状态，
 *    确保状态只能在 ViewModel 内部更新。
 * 4. 高德 SDK 的网络请求（Inputtips 和 GeocodeSearch）均为异步回调模式，
 *    ViewModel 在回调中通过 viewModelScope.launch + withContext(Dispatchers.Main)
 *    将结果安全地切回主线程更新 StateFlow。
 * 5. 外部 Composable 通过回调函数 onLocationSelectedCallback 接收最终选中的坐标。
 */

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeQuery
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.amap.api.services.help.Tip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// 使用 Hilt 注解，由 Hilt 框架自动提供 Application 实例并管理生命周期
@HiltViewModel
class LocationSearchViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    // --- UI 状态：用户输入的搜索关键词文本 ---
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    // --- UI 状态：高德 Inputtips API 返回的搜索建议列表（Tip 为高德 SDK 建议项数据类） ---
    private val _searchSuggestions = MutableStateFlow<List<Tip>>(emptyList())
    val searchSuggestions: StateFlow<List<Tip>> = _searchSuggestions.asStateFlow()

    // --- UI 状态：用户最终选中的地点信息（含名称、地址、经纬度） ---
    private val _selectedLocation = MutableStateFlow<LocationInfo?>(null)
    val selectedLocation: StateFlow<LocationInfo?> = _selectedLocation.asStateFlow()

    // --- UI 状态：是否显示搜索建议下拉列表 ---
    private val _showSuggestions = MutableStateFlow(false)
    val showSuggestions: StateFlow<Boolean> = _showSuggestions.asStateFlow()

    // --- UI 状态：是否正在执行搜索请求（用于 loading 展示和节流） ---
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // --- 高德 SDK 实例：Inputtips（关键词搜索建议）、GeocodeSearch（地理编码/反编码）、当前查询参数 ---
    private var inputtips: Inputtips? = null
    private var geocodeSearch: GeocodeSearch? = null
    private var currentQuery: InputtipsQuery? = null

    // --- 外部回调：地点选择完成后通知调用方（Composable 父组件），传递选中的 LatLng ---
    private var onLocationSelectedCallback: ((LatLng) -> Unit)? = null

    // 初始化块：创建 GeocodeSearch 实例并注册地理编码/反地理编码的异步回调监听器
    init {
        // 创建高德地理编码搜索器，需要 Application Context 初始化
        geocodeSearch = GeocodeSearch(application.applicationContext)
        // 注册双向地理编码监听器（地址↔坐标相互转换的回调）
        geocodeSearch?.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
            // 地理编码回调（地址 → 坐标）：用户在 selectLocation() 中触发，
            // 当 Tip 不含坐标信息时，通过地址文本查询经纬度
            override fun onGeocodeSearched(result: GeocodeResult?, rCode: Int) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        // rCode == 1000 表示高德 API 请求成功
                        if (rCode == 1000 && result != null && result.geocodeAddressList.isNotEmpty()) {
                            val address = result.geocodeAddressList[0]
                            val latLng = LatLng(address.latLonPoint.latitude, address.latLonPoint.longitude)
                            // 更新选中地点状态
                            _selectedLocation.value = LocationInfo(
                                name = address.formatAddress,
                                address = address.formatAddress,
                                latitude = address.latLonPoint.latitude,
                                longitude = address.latLonPoint.longitude
                            )
                            // 通过回调将结果传递到 UI 层（如地图标记）
                            onLocationSelectedCallback?.invoke(latLng)
                        }
                    }
                }
            }

            // 反地理编码回调（坐标 → 地址）：用户在 selectCurrentLocation() 中触发，
            // 将经纬度转为可读的地址文本
            override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (rCode == 1000 && result != null && result.regeocodeAddress != null) {
                            val address = result.regeocodeAddress
                            val latLng = LatLng(result.regeocodeQuery.point.latitude, result.regeocodeQuery.point.longitude)
                            // 更新选中地点状态
                            _selectedLocation.value = LocationInfo(
                                name = address.formatAddress,
                                address = address.formatAddress,
                                latitude = latLng.latitude,
                                longitude = latLng.longitude
                            )
                            onLocationSelectedCallback?.invoke(latLng)
                        }
                    }
                }
            }
        })
    }

    // 注册位置选择完成后的回调函数，由外部 Composable 调用（如在地图上标记位置）
    fun setOnLocationSelectedCallback(callback: (LatLng) -> Unit) {
        this.onLocationSelectedCallback = callback
    }

    // 外部 Composable 调用此方法来更新搜索文本，会触发关键词搜索建议请求或清空建议列表
    // @param text 用户输入的搜索关键词
    fun updateSearchText(text: String) {
        _searchText.value = text
        if (text.isNotEmpty()) {
            performSearch(text)
            _showSuggestions.value = true
        } else {
            _searchSuggestions.value = emptyList()
            _showSuggestions.value = false
        }
    }

    // 内部方法：调用高德 Inputtips API 异步获取搜索建议
    // 使用 _isSearching 做请求节流，防止重复请求
    // 在 IO 协程中发起网络请求，结果通过回调切回 Main 线程更新 StateFlow
    private fun performSearch(keyword: String) {
        // 如果正在搜索中则直接返回，避免重复请求
        if (_isSearching.value) return

        _isSearching.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 构造 Inputtips 查询参数（关键词 + 空城市表示全国搜索）
                currentQuery = InputtipsQuery(keyword, "")
                // 限制在当前城市范围内搜索
                currentQuery?.cityLimit = true

                // 创建 Inputtips 实例并注册异步监听器
                inputtips = Inputtips(getApplication<Application>().applicationContext, currentQuery)
                inputtips?.setInputtipsListener { tips, rCode ->
                    viewModelScope.launch {
                        withContext(Dispatchers.Main) {
                            _isSearching.value = false
                            // rCode == 1000 表示 API 请求成功，更新建议列表
                            if (rCode == 1000 && tips != null) {
                                _searchSuggestions.value = tips
                            } else {
                                _searchSuggestions.value = emptyList()
                            }
                        }
                    }
                }
                // 发起异步请求
                inputtips?.requestInputtipsAsyn()
            } catch (e: Exception) {
                // 异常时清空建议并重置搜索状态
                _searchSuggestions.value = emptyList()
                _isSearching.value = false
            }
        }
    }

    // 用户从搜索建议列表中选择一个地点：
    // 优先使用 Tip 自带的坐标信息，若无坐标则通过地理编码 API 获取
    // @param tip 高德 SDK 返回的搜索建议项
    fun selectLocation(tip: Tip) {
        // 将选中地点的名称填入搜索框，隐藏建议列表
        _searchText.value = tip.name
        _showSuggestions.value = false

        // 如果 Tip 已有坐标信息，直接使用并回调
        if (tip.point != null) {
            val latLng = LatLng(tip.point.latitude, tip.point.longitude)
            _selectedLocation.value = LocationInfo(
                name = tip.name,
                address = tip.district ?: tip.address ?: "",
                latitude = latLng.latitude,
                longitude = latLng.longitude
            )
            onLocationSelectedCallback?.invoke(latLng)
        } else {
            // 否则通过地理编码（地址→坐标）异步查询
            val query = GeocodeQuery(tip.name, "")
            geocodeSearch?.getFromLocationNameAsyn(query)
        }
    }

    // 用户选择"使用当前定位"时调用，通过反地理编码将经纬度转为地址文本
    // @param latLng 当前设备的经纬度坐标
    fun selectCurrentLocation(latLng: LatLng) {
        // 将 LatLng 转为高德 SDK 的 LatLonPoint，并构造反地理编码查询（半径 100 米，高德坐标系）
        val latLonPoint = LatLonPoint(latLng.latitude, latLng.longitude)
        val query = RegeocodeQuery(latLonPoint, 100f, GeocodeSearch.AMAP)
        geocodeSearch?.getFromLocationAsyn(query)
    }

    // 重置所有搜索状态，清除选中地点、搜索文本、建议列表、关闭下拉
    fun clearSelection() {
        _selectedLocation.value = null
        _searchText.value = ""
        _searchSuggestions.value = emptyList()
        _showSuggestions.value = false
    }

    // 关闭搜索建议下拉列表（用户点击外部区域或手动关闭时调用）
    fun dismissSuggestions() {
        _showSuggestions.value = false
    }

    // 位置信息数据类：封装选中的地点名称、详细地址、纬度和经度
    data class LocationInfo(
        val name: String,      // 地点名称（如"故宫博物院"）
        val address: String,   // 详细地址描述
        val latitude: Double,  // 纬度
        val longitude: Double  // 经度
    )
}