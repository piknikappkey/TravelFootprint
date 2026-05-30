package com.example.travel_footprint_android.presentation2.components.journey_panel2.footprint_panel.footprint_details

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener

/**
 * LocationRecorder - 位置记录组件
 *
 * 功能：当 isRecord 为 true 时，持续获取用户定位并输出到控制台
 * 实现方法：
 *  - 使用高德地图定位 SDK
 *  - 通过 AMapLocationClient 实现持续定位
 *  - 当 isRecord 状态变化时启动/停止定位
 *  - 将经纬度信息输出到 Logcat 控制台
 */

@Composable
fun LocationRecorder(
    isRecord: Boolean,
    updateLocation: (Double, Double) -> Unit = { latitude, longitude -> },
) {
    val context = LocalContext.current
    var locationClient: AMapLocationClient? = null

    // 创建定位客户端
    DisposableEffect(isRecord) {
        if (isRecord) {
            locationClient = createLocationClient(context) { location ->
                if (location.errorCode == 0) {
                    Log.d("LocationRecorder", "定位成功 - 纬度: ${location.latitude}, 经度: ${location.longitude}")
                    updateLocation(location.latitude, location.longitude) // 将位置信息传出
                } else {
                    Log.e("LocationRecorder", "定位失败 - 错误码: ${location.errorCode}, 错误信息: ${location.errorInfo}")
                }
            }
            locationClient?.startLocation()
            Log.d("LocationRecorder", "开始持续定位...")
        }

        onDispose {
            locationClient?.stopLocation()
            locationClient?.onDestroy()
            Log.d("LocationRecorder", "停止定位并释放资源")
        }
    }

    // 监听 isRecord 状态变化
    LaunchedEffect(isRecord) {
        if (isRecord) {
            locationClient?.startLocation()
            Log.d("LocationRecorder", "重新开始持续定位")
        } else {
            locationClient?.stopLocation()
            Log.d("LocationRecorder", "暂停定位")
        }
    }
}

/**
 * 创建定位客户端并配置参数
 */
private fun createLocationClient(
    context: Context,
    onLocationChanged: (AMapLocation) -> Unit
): AMapLocationClient {
    val locationClient = AMapLocationClient(context)

    // 配置定位参数
    val option = AMapLocationClientOption().apply {
        // 设置定位模式为高精度模式
        locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        // 设置连续定位间隔（毫秒）
        interval = 2000 // 每3秒获取一次定位
        // 设置是否返回地址信息
        isNeedAddress = true
        // 设置是否单次定位
        isOnceLocation = false
        // 设置是否开启WiFi扫描
        isWifiScan = true
        // 设置是否使用缓存
        isLocationCacheEnable = true
    }

    locationClient.setLocationOption(option)

    // 设置定位监听器
    locationClient.setLocationListener(AMapLocationListener { location ->
        onLocationChanged(location)
    })

    return locationClient
}
