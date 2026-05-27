// 在合适的位置创建扩展函数，如 extensions/LightedCityExt.kt
package com.example.travel_footprint_android.data.extensions

import com.example.travel_footprint_android.data.entity.LightedCity
import com.example.travel_footprint_android.data.entity.LightedProvince

fun List<LightedCity>.toLightedProvinces(): List<LightedProvince> {
    return this
        .map { LightedProvince(it.provinceName, it.provinceAdcode) }
        .distinctBy { it.provinceAdcode }
}

fun List<LightedCity>.isCityLighted(cityAdcode: String): Boolean {
    return this.any { it.cityAdcode == cityAdcode }
}

fun List<LightedCity>.isProvinceLighted(provinceAdcode: String): Boolean {
    return this.any { it.provinceAdcode == provinceAdcode }
}