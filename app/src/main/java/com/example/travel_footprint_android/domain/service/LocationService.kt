// app/src/main/java/com/example/travel_footprint_android/domain/service/LocationService.kt
package com.example.travel_footprint_android.domain.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.travel_footprint_android.utils.LocationUtils
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationService @Inject constructor(
    private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

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
     * 获取当前位置
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                continuation.resume(location)
            }.addOnFailureListener {
                continuation.resume(null)
            }
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }

    // ==================== 综合获取方法 ====================

    /**
     * 获取当前位置的详细信息
     */
    suspend fun getCurrentLocationDetail(): LocationDetail? {
        val location = getCurrentLocation() ?: return null
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
     * 获取当前位置的省份
     */
    suspend fun getCurrentProvince(): String {
        val location = getCurrentLocation() ?: return "未知"
        return getProvince(location.latitude, location.longitude)
    }

    /**
     * 获取当前位置的城市
     */
    suspend fun getCurrentCity(): String {
        val location = getCurrentLocation() ?: return "未知"
        return getCity(location.latitude, location.longitude)
    }

    /**
     * 获取当前位置的区县
     */
    suspend fun getCurrentDistrict(): String {
        val location = getCurrentLocation() ?: return ""
        return getDistrict(location.latitude, location.longitude)
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
     * 计算两点距离
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        return LocationUtils.calculateDistance(lat1, lon1, lat2, lon2)
    }

    /**
     * 检查位置权限
     */
    fun hasLocationPermission(): Boolean {
        return android.Manifest.permission.ACCESS_FINE_LOCATION.let { permission ->
            PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(context, permission)
        }
    }
}