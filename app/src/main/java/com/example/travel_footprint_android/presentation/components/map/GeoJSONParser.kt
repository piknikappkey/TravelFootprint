// app/src/main/java/com/example/travel_footprint_android/presentation/components/map/GeoJSONParser.kt
package com.example.travel_footprint_android.presentation.components.map

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.double

/**
 * GeoJSON 解析器
 * 负责将 GeoJSON 数据解析为可绘制的 Path 对象
 */
object GeoJSONParser {

    // 中国地图经纬度范围
    private const val MIN_LNG = 73.0
    private const val MAX_LNG = 135.0
    private const val MIN_LAT = 3.0
    private const val MAX_LAT = 54.0

    /**
     * 从 assets 加载并解析省份 GeoJSON
     *
     * @param context 上下文
     * @param fileName GeoJSON 文件名
     * @param canvasWidth 画布宽度
     * @param canvasHeight 画布高度
     * @return 省份路径列表
     */
    fun parseProvinces(
        context: Context,
        fileName: String = "中华人民共和国(省).geojson",
        canvasWidth: Float,
        canvasHeight: Float
    ): List<ProvincePath> {
        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            parseProvinceGeoJSON(jsonString, canvasWidth, canvasHeight)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 从 assets 加载并解析城市 GeoJSON
     *
     * @param context 上下文
     * @param fileName GeoJSON 文件名
     * @param canvasWidth 画布宽度
     * @param canvasHeight 画布高度
     * @return 城市路径列表
     */
    fun parseCities(
        context: Context,
        fileName: String = "中华人民共和国(市).geojson",
        canvasWidth: Float,
        canvasHeight: Float
    ): List<CityPath> {
        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            parseCityGeoJSON(jsonString, canvasWidth, canvasHeight)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 解析省份 GeoJSON 字符串
     */
    private fun parseProvinceGeoJSON(
        jsonString: String,
        canvasWidth: Float,
        canvasHeight: Float
    ): List<ProvincePath> {
        val provinces = mutableListOf<ProvincePath>()
        val json = Json { ignoreUnknownKeys = true }
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val features = jsonObject["features"]?.jsonArray ?: return emptyList()

        features.forEach { featureElement ->
            val feature = featureElement.jsonObject
            val properties = feature["properties"]?.jsonObject ?: return@forEach
            val geometry = feature["geometry"]?.jsonObject ?: return@forEach

            val adcode = properties["adcode"]?.jsonPrimitive?.content ?: return@forEach
            val name = properties["name"]?.jsonPrimitive?.content ?: return@forEach
            val childrenNum = properties["childrenNum"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0

            val centerArray = properties["center"]?.jsonArray
            val center = if (centerArray != null && centerArray.size >= 2) {
                val lng = centerArray[0].jsonPrimitive.double
                val lat = centerArray[1].jsonPrimitive.double
                coordinateToOffset(lng, lat, canvasWidth, canvasHeight)
            } else {
                Offset.Zero
            }

            val path = parseGeometry(geometry, canvasWidth, canvasHeight)

            provinces.add(
                ProvincePath(
                    adcode = adcode,
                    name = name,
                    path = path,
                    center = center,
                    childrenNum = childrenNum
                )
            )
        }

        return provinces
    }

    /**
     * 解析城市 GeoJSON 字符串
     */
    private fun parseCityGeoJSON(
        jsonString: String,
        canvasWidth: Float,
        canvasHeight: Float
    ): List<CityPath> {
        val cities = mutableListOf<CityPath>()
        val json = Json { ignoreUnknownKeys = true }
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val features = jsonObject["features"]?.jsonArray ?: return emptyList()

        features.forEach { featureElement ->
            val feature = featureElement.jsonObject
            val properties = feature["properties"]?.jsonObject ?: return@forEach
            val geometry = feature["geometry"]?.jsonObject ?: return@forEach

            val adcode = properties["adcode"]?.jsonPrimitive?.content ?: return@forEach
            val name = properties["name"]?.jsonPrimitive?.content ?: return@forEach

            // 获取所属省份代码
            val parent = properties["parent"]?.jsonObject
            val provinceAdcode = parent?.get("adcode")?.jsonPrimitive?.content ?: ""

            val centerArray = properties["center"]?.jsonArray
            val center = if (centerArray != null && centerArray.size >= 2) {
                val lng = centerArray[0].jsonPrimitive.double
                val lat = centerArray[1].jsonPrimitive.double
                coordinateToOffset(lng, lat, canvasWidth, canvasHeight)
            } else {
                Offset.Zero
            }

            val path = parseGeometry(geometry, canvasWidth, canvasHeight)

            cities.add(
                CityPath(
                    adcode = adcode,
                    name = name,
                    path = path,
                    center = center,
                    provinceAdcode = provinceAdcode
                )
            )
        }

        return cities
    }

    /**
     * 解析几何数据
     */
    private fun parseGeometry(
        geometry: JsonObject,
        canvasWidth: Float,
        canvasHeight: Float
    ): Path {
        val path = Path()
        val type = geometry["type"]?.jsonPrimitive?.content ?: return path
        val coordinates = geometry["coordinates"]?.jsonArray ?: return path

        when (type) {
            "Polygon" -> {
                parsePolygon(coordinates, path, canvasWidth, canvasHeight)
            }
            "MultiPolygon" -> {
                coordinates.forEach { polygon ->
                    parsePolygon(polygon.jsonArray, path, canvasWidth, canvasHeight)
                }
            }
        }

        return path
    }

    /**
     * 解析多边形数据
     */
    private fun parsePolygon(
        coordinates: kotlinx.serialization.json.JsonArray,
        path: Path,
        canvasWidth: Float,
        canvasHeight: Float
    ) {
        coordinates.forEach { ringElement ->
            val ring = ringElement.jsonArray
            if (ring.isNotEmpty()) {
                val firstPoint = ring[0].jsonArray
                if (firstPoint.size >= 2) {
                    val firstLng = firstPoint[0].jsonPrimitive.double
                    val firstLat = firstPoint[1].jsonPrimitive.double
                    val firstOffset = coordinateToOffset(firstLng, firstLat, canvasWidth, canvasHeight)
                    path.moveTo(firstOffset.x, firstOffset.y)

                    for (i in 1 until ring.size) {
                        val point = ring[i].jsonArray
                        if (point.size >= 2) {
                            val lng = point[0].jsonPrimitive.double
                            val lat = point[1].jsonPrimitive.double
                            val offset = coordinateToOffset(lng, lat, canvasWidth, canvasHeight)
                            path.lineTo(offset.x, offset.y)
                        }
                    }
                    path.close()
                }
            }
        }
    }

    /**
     * 经纬度坐标转换为屏幕坐标
     *
     * 注意：屏幕 Y 轴向下递增，而纬度向北递增，所以需要翻转 Y 轴
     *
     * @param lng 经度
     * @param lat 纬度
     * @param canvasWidth 画布宽度
     * @param canvasHeight 画布高度
     * @return 屏幕坐标 Offset
     */
    fun coordinateToOffset(
        lng: Double,
        lat: Double,
        canvasWidth: Float,
        canvasHeight: Float
    ): Offset {
        val x = ((lng - MIN_LNG) / (MAX_LNG - MIN_LNG) * canvasWidth).toFloat()
        val y = ((MAX_LAT - lat) / (MAX_LAT - MIN_LAT) * canvasHeight).toFloat()
        return Offset(x, y)
    }

    /**
     * 屏幕坐标转换为经纬度坐标
     *
     * @param offset 屏幕坐标
     * @param canvasWidth 画布宽度
     * @param canvasHeight 画布高度
     * @return 经纬度坐标 Pair(lng, lat)
     */
    fun offsetToCoordinate(
        offset: Offset,
        canvasWidth: Float,
        canvasHeight: Float
    ): Pair<Double, Double> {
        val lng = MIN_LNG + (offset.x / canvasWidth) * (MAX_LNG - MIN_LNG)
        val lat = MAX_LAT - (offset.y / canvasHeight) * (MAX_LAT - MIN_LAT)
        return lng to lat
    }
}
