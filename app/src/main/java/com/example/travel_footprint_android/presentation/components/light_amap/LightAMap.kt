package com.example.travel_footprint_android.presentation.components.light_amap

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.PolygonOptions
import com.example.travel_footprint_android.presentation2.screen.LightenCityMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


@Composable
fun LightAMap(
    modifier: Modifier = Modifier,
    setLightenCityMode: (LightenCityMode) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context)
    }

    // 处理 MapView 的生命周期
    DisposableEffect(key1 = Unit) {
        mapView.onCreate(Bundle())
        
        // 获取 AMap 实例（高德地图使用 getMap() 方法）
        val aMap = mapView.map
        if (aMap != null) {
            // 配置地图
            configureMap(aMap)
            
            // 加载省市轮廓
            loadProvinceBoundaries(context, aMap)
        }

        // 初始化定位客户端
        val locationClient = AMapLocationClient(context)

        onDispose {
            locationClient.onDestroy()
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize()
    )
}

private fun configureMap(aMap: AMap) {
    // 启用缩放控件
    aMap.uiSettings.isZoomControlsEnabled = true
    // 启用指南针
    aMap.uiSettings.isCompassEnabled = true
    // 启用比例尺
    aMap.uiSettings.isScaleControlsEnabled = true
    // 设置默认缩放级别
    aMap.moveCamera(CameraUpdateFactory.zoomTo(5f))
    // 移动到中国中心位置
    aMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(35.86166, 104.195397)))
    
    // 隐藏所有不需要的图层，只显示基本地图
    aMap.mapType = AMap.MAP_TYPE_NORMAL
    
    // 禁用交通图层
    aMap.isTrafficEnabled = false
    
    // 禁用3D建筑物
    aMap.showBuildings(false)
}

private fun loadProvinceBoundaries(context: android.content.Context, aMap: AMap) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // 读取省级 GeoJSON 文件
            val provinceJson = context.assets.open("中华人民共和国(省).geojson").use {
                it.bufferedReader().readText()
            }
            
            // 解析并添加省份轮廓
            parseAndAddBoundaries(provinceJson, aMap)
            
            Log.d("LightAMap", "省份轮廓加载完成")
        } catch (e: Exception) {
            Log.e("LightAMap", "加载省份轮廓失败", e)
        }
    }
}

private fun parseAndAddBoundaries(geoJson: String, aMap: AMap) {
    try {
        val jsonObject = JSONObject(geoJson)
        val features = jsonObject.getJSONArray("features")
        
        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val geometry = feature.getJSONObject("geometry")
            val type = geometry.getString("type")
            
            if (type == "Polygon") {
                val coordinates = geometry.getJSONArray("coordinates")
                addPolygonFromCoordinates(coordinates, aMap)
            } else if (type == "MultiPolygon") {
                val coordinates = geometry.getJSONArray("coordinates")
                for (j in 0 until coordinates.length()) {
                    val polygonCoordinates = coordinates.getJSONArray(j)
                    addPolygonFromCoordinates(polygonCoordinates, aMap)
                }
            }
        }
    } catch (e: Exception) {
        Log.e("LightAMap", "解析 GeoJSON 失败", e)
    }
}

private fun addPolygonFromCoordinates(coordinates: JSONArray, aMap: AMap) {
    try {
        val polygonOptions = PolygonOptions()
        
        // 获取第一个环（外边界）
        val outerRing = coordinates.getJSONArray(0)
        for (i in 0 until outerRing.length()) {
            val point = outerRing.getJSONArray(i)
            val lng = point.getDouble(0)
            val lat = point.getDouble(1)
            polygonOptions.add(LatLng(lat, lng))
        }
        
        // 设置多边形样式
        polygonOptions
            .strokeWidth(1f)
            .strokeColor(0xFF666666.toInt())
            .fillColor(0x204CAF50.toInt()) // 半透明绿色
        
        // 添加到地图
        aMap.addPolygon(polygonOptions)
    } catch (e: Exception) {
        Log.e("LightAMap", "添加多边形失败", e)
    }
}