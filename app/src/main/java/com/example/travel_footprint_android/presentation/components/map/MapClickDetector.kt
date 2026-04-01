// app/src/main/java/com/example/travel_footprint_android/presentation/components/map/MapClickDetector.kt
package com.example.travel_footprint_android.presentation.components.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import android.graphics.Region
import android.graphics.RectF

/**
 * 地图点击检测器
 * 负责检测用户点击了哪个省份或城市
 */
object MapClickDetector {

    /**
     * 在地图路径列表中查找点击的项
     *
     * @param offset 点击位置
     * @param mapData 地图数据列表（省份或城市）
     * @return 点击的项，未找到返回 null
     */
    fun findProvinceAt(
        offset: Offset,
        mapData: List<MapPath>
    ): MapPath? {
        // 从后向前遍历，确保点击上层区域时优先选中
        for (i in mapData.size - 1 downTo 0) {
            val item = mapData[i]
            if (isPointInPath(offset, item.path)) {
                return item
            }
        }
        return null
    }

    /**
     * 在城市列表中查找点击的城市
     *
     * @param offset 点击位置
     * @param cities 城市列表
     * @return 点击的城市，未找到返回 null
     */
    fun findCityAt(
        offset: Offset,
        cities: List<CityPath>
    ): CityPath? {
        // 从后向前遍历
        for (i in cities.size - 1 downTo 0) {
            val city = cities[i]
            if (isPointInPath(offset, city.path)) {
                return city
            }
        }
        return null
    }

    /**
     * 在指定省份的城市中查找点击的城市
     *
     * @param offset 点击位置
     * @param cities 城市列表
     * @param provinceAdcode 省份代码
     * @return 点击的城市，未找到返回 null
     */
    fun findCityInProvinceAt(
        offset: Offset,
        cities: List<CityPath>,
        provinceAdcode: String
    ): CityPath? {
        val provinceCities = cities.filter { it.provinceAdcode == provinceAdcode }
        for (i in provinceCities.size - 1 downTo 0) {
            val city = provinceCities[i]
            if (isPointInPath(offset, city.path)) {
                return city
            }
        }
        return null
    }

    /**
     * 判断点是否在路径内
     *
     * @param point 点坐标
     * @param path 路径
     * @return 是否在路径内
     */
    private fun isPointInPath(point: Offset, path: Path): Boolean {
        // 使用 Android 的 Region 进行精确判断
        val androidPath = path.asAndroidPath()
        val bounds = RectF()
        androidPath.computeBounds(bounds, true)

        val region = Region()
        val clip = Region(
            bounds.left.toInt(),
            bounds.top.toInt(),
            bounds.right.toInt(),
            bounds.bottom.toInt()
        )
        region.setPath(androidPath, clip)

        return region.contains(point.x.toInt(), point.y.toInt())
    }

    /**
     * 简化的点击检测（使用路径边界框）
     * 性能更好但精度较低
     *
     * @param point 点坐标
     * @param path 路径
     * @return 是否在边界框内
     */
    fun isPointInPathBounds(point: Offset, path: Path): Boolean {
        val androidPath = path.asAndroidPath()
        val bounds = RectF()
        androidPath.computeBounds(bounds, true)
        return bounds.contains(point.x, point.y)
    }
}
