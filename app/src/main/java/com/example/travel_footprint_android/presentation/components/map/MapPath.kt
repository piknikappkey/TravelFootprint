// app/src/main/java/com/example/travel_footprint_android/presentation/components/map/MapPath.kt
package com.example.travel_footprint_android.presentation.components.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

/**
 * 地图路径数据基类
 * 用于存储解析后的 GeoJSON 数据
 */
sealed class MapPath {
    abstract val adcode: String
    abstract val name: String
    abstract val path: Path
    abstract val center: Offset
}

/**
 * 省份路径数据
 *
 * @param adcode 省份行政区划代码
 * @param name 省份名称
 * @param path 绘制路径
 * @param center 中心点坐标
 * @param childrenNum 下属城市数量
 */
data class ProvincePath(
    override val adcode: String,
    override val name: String,
    override val path: Path,
    override val center: Offset,
    val childrenNum: Int = 0
) : MapPath()

/**
 * 城市路径数据
 *
 * @param adcode 城市行政区划代码
 * @param name 城市名称
 * @param path 绘制路径
 * @param center 中心点坐标
 * @param provinceAdcode 所属省份代码
 */
data class CityPath(
    override val adcode: String,
    override val name: String,
    override val path: Path,
    override val center: Offset,
    val provinceAdcode: String
) : MapPath()

/**
 * 地图层级枚举
 */
enum class MapLevel {
    PROVINCE,  // 省份层级
    CITY       // 城市层级
}

/**
 * GeoJSON 特征属性数据类
 */
data class GeoFeatureProperties(
    val adcode: Int,
    val name: String,
    val center: List<Double>,
    val centroid: List<Double>? = null,
    val childrenNum: Int = 0,
    val level: String = "province",
    val parent: Map<String, Int>? = null
)

/**
 * GeoJSON 几何数据类
 */
data class GeoGeometry(
    val type: String,
    val coordinates: List<Any>
)

/**
 * GeoJSON 特征数据类
 */
data class GeoFeature(
    val type: String = "Feature",
    val properties: GeoFeatureProperties,
    val geometry: GeoGeometry
)

/**
 * GeoJSON 集合数据类
 */
data class GeoFeatureCollection(
    val type: String = "FeatureCollection",
    val features: List<GeoFeature>
)
