package com.example.travel_footprint_android.presentation.components.svg_map

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
            Log.e("CityClickInterface", "Error0")

            // 解析 JSON 数据
            val jsonObject = JSONObject(cityInfoJson)
            Log.e("CityClickInterface", "Error1")

            val cityName = jsonObject.getString("name")
            Log.e("CityClickInterface", "Error2")

            val adcode = jsonObject.optString("adcode", "")
            Log.e("CityClickInterface", "Error3")

            val parentAdcode = jsonObject.optString("parent_adcode", "")
            Log.e("CityClickInterface", "Error4")


            // 传递三个参数：城市名、adcode、父级adcode
            // onCityClick 内部会处理 cityState 更新，无需再单独调用 cityClickState
            onCityClick(cityName, adcode, parentAdcode)
            Log.e("CityClickInterface", "Error5")

        } catch (e: Exception) {
            Log.e("CityClickInterface", "Error handling city click", e)
        }
    }
    @JavascriptInterface
    fun onCityUnClicked() {
        Log.d("CityClickInterface", "被选中城市被点击")
        cityClickState(false)
    }
}