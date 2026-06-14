package com.example.travel_footprint_android.domain.service

import android.content.Context
import android.util.Log
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LocationTracker - 非 Composable 版 GPS 定位器
 *
 * 供 Foreground Service 使用，不依赖 Compose 生命周期。
 * 封装高德地图 AMapLocationClient，提供 start/stop 接口和位置回调。
 */
@Singleton
class LocationTracker @Inject constructor() {

    private var locationClient: AMapLocationClient? = null

    /**
     * 开始持续定位
     * @param context Application Context
     * @param onLocation 定位成功回调 (latitude, longitude)
     */
    fun start(context: Context, onLocation: (Double, Double) -> Unit) {
        stop() // 先清理已有的 client

        locationClient = AMapLocationClient(context).apply {
            setLocationOption(AMapLocationClientOption().apply {
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                interval = 2000
                isNeedAddress = false
                isOnceLocation = false
                isWifiScan = true
                isLocationCacheEnable = true
            })
            setLocationListener(AMapLocationListener { location ->
                if (location.errorCode == 0) {
                    onLocation(location.latitude, location.longitude)
                } else {
                    Log.e("LocationTracker", "定位失败: ${location.errorCode} - ${location.errorInfo}")
                }
            })
            startLocation()
        }
        Log.d("LocationTracker", "开始持续定位")
    }

    /** 停止定位并释放资源 */
    fun stop() {
        locationClient?.stopLocation()
        locationClient?.onDestroy()
        locationClient = null
        Log.d("LocationTracker", "停止定位")
    }
}
