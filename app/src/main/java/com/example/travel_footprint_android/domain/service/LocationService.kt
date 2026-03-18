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
     * 格式化地址
     */
    private fun formatAddress(address: Address): String {
        val parts = mutableListOf<String>()

        // 优先使用详细地址
        if (!address.getAddressLine(0).isNullOrBlank()) {
            return address.getAddressLine(0)
        }

        // 否则拼接各字段
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