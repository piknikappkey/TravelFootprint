package com.example.travel_footprint_android.presentation2.components.journey_map3.location_search

/**
 * LocationSearchViewModel - 地点搜索 ViewModel
 *
 * 功能：处理地点搜索、地理编码/反编码逻辑
 * 实现方法：
 *  - 使用 Hilt 依赖注入 + AndroidViewModel
 *  - Inputtips API 实现关键词搜索建议
 *  - GeocodeSearch 实现地理编码（地址→坐标）和反地理编码（坐标→地址）
 *  - 通过 StateFlow 管理搜索状态和结果
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

@HiltViewModel
class LocationSearchViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _searchSuggestions = MutableStateFlow<List<Tip>>(emptyList())
    val searchSuggestions: StateFlow<List<Tip>> = _searchSuggestions.asStateFlow()

    private val _selectedLocation = MutableStateFlow<LocationInfo?>(null)
    val selectedLocation: StateFlow<LocationInfo?> = _selectedLocation.asStateFlow()

    private val _showSuggestions = MutableStateFlow(false)
    val showSuggestions: StateFlow<Boolean> = _showSuggestions.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var inputtips: Inputtips? = null
    private var geocodeSearch: GeocodeSearch? = null
    private var currentQuery: InputtipsQuery? = null

    private var onLocationSelectedCallback: ((LatLng) -> Unit)? = null

    init {
        // 初始化地理编码搜索器
        geocodeSearch = GeocodeSearch(application.applicationContext)
        geocodeSearch?.setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
            // 地理编码回调：地址 -> 坐标
            override fun onGeocodeSearched(result: GeocodeResult?, rCode: Int) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (rCode == 1000 && result != null && result.geocodeAddressList.isNotEmpty()) {
                            val address = result.geocodeAddressList[0]
                            val latLng = LatLng(address.latLonPoint.latitude, address.latLonPoint.longitude)
                            _selectedLocation.value = LocationInfo(
                                name = address.formatAddress,
                                address = address.formatAddress,
                                latitude = address.latLonPoint.latitude,
                                longitude = address.latLonPoint.longitude
                            )
                            onLocationSelectedCallback?.invoke(latLng)
                        }
                    }
                }
            }

            // 反地理编码回调：坐标 -> 地址
            override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (rCode == 1000 && result != null && result.regeocodeAddress != null) {
                            val address = result.regeocodeAddress
                            val latLng = LatLng(result.regeocodeQuery.point.latitude, result.regeocodeQuery.point.longitude)
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

    /** 设置位置选择回调 */
    fun setOnLocationSelectedCallback(callback: (LatLng) -> Unit) {
        this.onLocationSelectedCallback = callback
    }

    /** 更新搜索文本，触发搜索建议 */
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

    /** 调用高德 Inputtips API 获取搜索建议 */
    private fun performSearch(keyword: String) {
        if (_isSearching.value) return

        _isSearching.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                currentQuery = InputtipsQuery(keyword, "")
                currentQuery?.cityLimit = true

                inputtips = Inputtips(getApplication<Application>().applicationContext, currentQuery)
                inputtips?.setInputtipsListener { tips, rCode ->
                    viewModelScope.launch {
                        withContext(Dispatchers.Main) {
                            _isSearching.value = false
                            if (rCode == 1000 && tips != null) {
                                _searchSuggestions.value = tips
                            } else {
                                _searchSuggestions.value = emptyList()
                            }
                        }
                    }
                }
                inputtips?.requestInputtipsAsyn()
            } catch (e: Exception) {
                _searchSuggestions.value = emptyList()
                _isSearching.value = false
            }
        }
    }

    /** 选择搜索建议 */
    fun selectLocation(tip: Tip) {
        _searchText.value = tip.name
        _showSuggestions.value = false

        // 如果 Tip 已有坐标，直接使用
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
            // 否则通过地理编码获取坐标
            val query = GeocodeQuery(tip.name, "")
            geocodeSearch?.getFromLocationNameAsyn(query)
        }
    }

    /** 选择当前定位位置（反编码） */
    fun selectCurrentLocation(latLng: LatLng) {
        val latLonPoint = LatLonPoint(latLng.latitude, latLng.longitude)
        val query = RegeocodeQuery(latLonPoint, 100f, GeocodeSearch.AMAP)
        geocodeSearch?.getFromLocationAsyn(query)
    }

    /** 清除选择 */
    fun clearSelection() {
        _selectedLocation.value = null
        _searchText.value = ""
        _searchSuggestions.value = emptyList()
        _showSuggestions.value = false
    }

    /** 隐藏建议列表 */
    fun dismissSuggestions() {
        _showSuggestions.value = false
    }

    /** 位置信息数据类 */
    data class LocationInfo(
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double
    )
}