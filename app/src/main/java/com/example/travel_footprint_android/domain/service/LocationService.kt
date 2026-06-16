// app/src/main/java/com/example/travel_footprint_android/domain/service/LocationService.kt
package com.example.travel_footprint_android.domain.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.core.content.ContextCompat
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationService @Inject constructor(
    private val context: Context
) {
    private val geocoder by lazy {
        Geocoder(context, Locale.getDefault())
    }

    /**
     * 位置详细信息
     */
    data class LocationDetail(
        val province: String,      // 省份
        val city: String,          // 城市
        val district: String,      // 区县
        val address: String,       // 详细地址
        val latitude: Double,      // 纬度
        val longitude: Double      // 经度
    ) {
        fun getFullAddress(): String = "$province$city$district$address"
        fun getShortAddress(): String = "$province$city"
    }

    /**
     * 获取当前位置（使用高德定位 SDK，兼容中国设备）
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationData? = suspendCancellableCoroutine { continuation ->
        try {
            val locationClient = AMapLocationClient(context)
            val option = AMapLocationClientOption().apply {
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                isOnceLocation = true
                isOnceLocationLatest = true
                isNeedAddress = true
                httpTimeOut = 10000
            }
            locationClient.setLocationOption(option)
            locationClient.setLocationListener { amapLocation ->
                if (amapLocation != null && amapLocation.errorCode == 0) {
                    continuation.resume(
                        LocationData(
                            latitude = amapLocation.latitude,
                            longitude = amapLocation.longitude,
                            province = amapLocation.province ?: "",
                            city = amapLocation.city ?: "",
                            district = amapLocation.district ?: "",
                            address = amapLocation.address ?: ""
                        )
                    )
                } else {
                    val errorCode = amapLocation?.errorCode ?: -1
                    val errorInfo = amapLocation?.errorInfo ?: "未知错误"
                    Log.e("LocationService", "定位失败: errorCode=$errorCode, errorInfo=$errorInfo")
                    continuation.resume(null)
                }
                locationClient.onDestroy()
            }
            locationClient.startLocation()
        } catch (e: Exception) {
            Log.e("LocationService", "定位异常: ${e.message}", e)
            continuation.resume(null)
        }
    }

    /**
     * 高德定位返回的位置数据
     */
    data class LocationData(
        val latitude: Double,
        val longitude: Double,
        val province: String,
        val city: String,
        val district: String,
        val address: String
    )

    // ==================== 综合获取方法 ====================

    /**
     * 获取当前位置的详细信息（优先使用高德返回的地址信息，无需再走 Geocoder）
     */
    suspend fun getCurrentLocationDetail(): LocationDetail? {
        val location = getCurrentLocation() ?: return null
        // 高德定位已返回地址信息，直接使用，无需 Geocoder 二次查询
        if (location.address.isNotBlank()) {
            return LocationDetail(
                province = location.province.ifBlank { "未知" },
                city = location.city.ifBlank { "未知" },
                district = location.district,
                address = location.address,
                latitude = location.latitude,
                longitude = location.longitude
            )
        }
        return getLocationDetail(location.latitude, location.longitude)
    }

    /**
     * 根据经纬度获取位置详细信息
     */
    suspend fun getLocationDetail(latitude: Double, longitude: Double): LocationDetail {
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                parseLocationDetail(addresses[0], latitude, longitude)
            } else {
                LocationDetail(
                    province = "未知",
                    city = "未知",
                    district = "",
                    address = "$latitude, $longitude",
                    latitude = latitude,
                    longitude = longitude
                )
            }
        } catch (e: Exception) {
            LocationDetail(
                province = "未知",
                city = "未知",
                district = "",
                address = "$latitude, $longitude",
                latitude = latitude,
                longitude = longitude
            )
        }
    }

    // ==================== 单独获取方法 ====================

    /**
     * 单独获取省份
     */
    suspend fun getProvince(latitude: Double, longitude: Double): String {
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].adminArea ?: addresses[0].featureName ?: "未知"
            } else {
                "未知"
            }
        } catch (e: Exception) {
            "未知"
        }
    }

    /**
     * 单独获取城市
     */
    suspend fun getCity(latitude: Double, longitude: Double): String {
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].locality ?: addresses[0].subAdminArea ?: "未知"
            } else {
                "未知"
            }
        } catch (e: Exception) {
            "未知"
        }
    }

    /**
     * 单独获取区县
     */
    suspend fun getDistrict(latitude: Double, longitude: Double): String {
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].subLocality ?: ""
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 获取当前位置的省份（优先使用高德返回的地址信息）
     */
    suspend fun getCurrentProvince(): String {
        val location = getCurrentLocation()
        if (location != null && location.province.isNotBlank()) return location.province
        if (location != null) return getProvince(location.latitude, location.longitude)
        return "未知"
    }

    /**
     * 获取当前位置的城市（优先使用高德返回的地址信息）
     */
    suspend fun getCurrentCity(): String {
        val location = getCurrentLocation()
        if (location != null && location.city.isNotBlank()) return location.city
        if (location != null) return getCity(location.latitude, location.longitude)
        return "未知"
    }

    /**
     * 获取当前位置的区县（优先使用高德返回的地址信息）
     */
    suspend fun getCurrentDistrict(): String {
        val location = getCurrentLocation()
        if (location != null && location.district.isNotBlank()) return location.district
        if (location != null) return getDistrict(location.latitude, location.longitude)
        return ""
    }

    // ==================== 原有方法 ====================

    /**
     * 逆地理编码：坐标转地址
     */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): String {
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                formatAddress(addresses[0])
            } else {
                "$latitude, $longitude"
            }
        } catch (e: Exception) {
            "$latitude, $longitude"
        }
    }

    /**
     * 解析位置详细信息
     */
    private fun parseLocationDetail(
        address: Address,
        latitude: Double,
        longitude: Double
    ): LocationDetail {
        val province = address.adminArea ?: address.featureName ?: "未知"
        val city = address.locality ?: address.subAdminArea ?: province
        val district = address.subLocality ?: ""
        val detailedAddress = address.getAddressLine(0) ?: "$latitude, $longitude"

        return LocationDetail(
            province = province,
            city = city,
            district = district,
            address = detailedAddress,
            latitude = latitude,
            longitude = longitude
        )
    }

    /**
     * 格式化地址
     */
    private fun formatAddress(address: Address): String {
        val parts = mutableListOf<String>()
        if (!address.getAddressLine(0).isNullOrBlank()) {
            return address.getAddressLine(0)
        }
        if (!address.countryName.isNullOrBlank()) parts.add(address.countryName)
        if (!address.adminArea.isNullOrBlank()) parts.add(address.adminArea)
        if (!address.locality.isNullOrBlank()) parts.add(address.locality)
        if (!address.subLocality.isNullOrBlank()) parts.add(address.subLocality)
        if (!address.thoroughfare.isNullOrBlank()) parts.add(address.thoroughfare)
        return if (parts.isNotEmpty()) parts.joinToString(" ") else "${address.latitude}, ${address.longitude}"
    }

    /**
     * 检查位置权限（同时检查精确定位和粗略定位）
     */
    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }
}