package com.example.travel_footprint_android.presentation.components.map

/**
 * 城市信息数据类
 *
 * @param name 城市名称
 * @param adcode 行政区划代码
 * @param centerLng 中心经度
 * @param centerLat 中心纬度
 * @param parentAdcode 父级行政区划代码
 */
data class CityInfo(
    val name: String,
    val adcode: String,
    val centerLng: String,
    val centerLat: String,
    val parentAdcode: String
)
