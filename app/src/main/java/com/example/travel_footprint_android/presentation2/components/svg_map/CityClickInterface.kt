package com.example.travel_footprint_android.presentation2.components.svg_map

import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONObject

// JavaScript 接口类
class CityClickInterface(
    private val onCityClick: (cityName: String, adcode: String, parentAdcode: String) -> Unit,  // 修改：传递 3 个参数
    private val cityClickState: (Boolean) -> Unit,
) {
    @JavascriptInterface
    fun onCityClicked(cityInfoJson: String) {
        Log.d("CityClickInterface", "Received city click: $cityInfoJson")
        try {
            // 解析 JSON 数据
            val jsonObject = JSONObject(cityInfoJson)
            val cityName = jsonObject.getString("name")
            val adcode = jsonObject.optString("adcode", "")
            val parentAdcode = jsonObject.optString("parent_adcode", "")

            // 传递三个参数：城市名、adcode、父级adcode
            onCityClick(cityName, adcode, parentAdcode)
            cityClickState(true)
        } catch (e: Exception) {
            Log.e("CityClickInterface", "Error handling city click", e)
        }
    }
    @JavascriptInterface
    fun onCityUnClicked() {
        Log.d("CityClickInterface", "Received city un click")
        cityClickState(false)
    }
}