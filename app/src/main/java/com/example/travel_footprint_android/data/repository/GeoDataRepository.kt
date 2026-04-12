package com.example.travel_footprint_android.data.repository

import android.content.Context
import android.util.Log
import com.example.travel_footprint_android.presentation.components.panel.CityItemInfo
import com.example.travel_footprint_android.presentation.components.panel.ProvinceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * GeoJSON 数据仓库
 *
 * 负责从 assets 加载省份和城市数据
 */
class GeoDataRepository(private val context: Context) {

    /**
     * 加载所有省份信息
     *
     * @return 省份列表，按名称排序
     */
    suspend fun loadProvinces(): List<ProvinceInfo> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始加载省份数据...")
            val jsonString = context.assets.open(PROVINCE_FILE)
                .bufferedReader()
                .use { it.readText() }

            Log.d(TAG, "省份文件大小: ${jsonString.length} 字符")

            val jsonObject = JSONObject(jsonString)
            val features = jsonObject.getJSONArray("features")

            Log.d(TAG, "省份数量: ${features.length()}")

            val provinces = mutableListOf<ProvinceInfo>()

            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val properties = feature.getJSONObject("properties")

                // adcode 可能是 int 或 string，统一用 optString 获取
                val adcodeValue = properties.opt("adcode")
                val adcode = when (adcodeValue) {
                    is Int -> adcodeValue.toString()
                    is String -> adcodeValue
                    else -> continue // 跳过无效数据
                }

                // 过滤掉特殊区域（如京东地区）
                if (adcode.contains("_") || adcode.length != 6) {
                    Log.d(TAG, "跳过特殊区域: $adcode")
                    continue
                }

                val name = properties.optString("name", "")
                val level = properties.optString("level", "")

                // 只添加省级行政区
                if (level == "province" && name.isNotEmpty()) {
                    provinces.add(ProvinceInfo(adcode = adcode, name = name))
                }
            }

            Log.d(TAG, "加载完成，共 ${provinces.size} 个省份")

            // 按名称排序
            provinces.sortedBy { it.name }
        } catch (e: Exception) {
            Log.e(TAG, "加载省份数据失败", e)
            emptyList()
        }
    }

    /**
     * 加载所有城市并按省份分组
     *
     * 注意：市文件里包含的是"区"级别数据，需要过滤出真正的市
     *
     * @return Map<省份代码, 城市列表>
     */
    suspend fun loadCitiesByProvince(): Map<String, List<CityItemInfo>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始加载城市数据...")
            val jsonString = context.assets.open(CITY_FILE)
                .bufferedReader()
                .use { it.readText() }

            Log.d(TAG, "城市文件大小: ${jsonString.length} 字符")

            val jsonObject = JSONObject(jsonString)
            val features = jsonObject.getJSONArray("features")

            Log.d(TAG, "城市/区数量: ${features.length()}")

            val citiesByProvince = mutableMapOf<String, MutableList<CityItemInfo>>()
            var cityCount = 0

            for (i in 0 until features.length()) {
                val feature = features.getJSONObject(i)
                val properties = feature.getJSONObject("properties")

                // adcode 可能是 int 或 string，统一用 opt 获取
                val adcodeValue = properties.opt("adcode")
                val adcode = when (adcodeValue) {
                    is Int -> adcodeValue.toString()
                    is String -> adcodeValue
                    else -> continue // 跳过无效数据
                }

                // 过滤掉特殊区域
                if (adcode.contains("_") || adcode.length != 6) {
                    continue
                }

                val name = properties.optString("name", "")
                if (name.isEmpty()) continue

                val level = properties.optString("level", "")

                // 获取父级省份代码
                val parentObj = properties.optJSONObject("parent")
                val parentAdcodeValue = parentObj?.opt("adcode")
                val parentAdcode = when (parentAdcodeValue) {
                    is Int -> parentAdcodeValue.toString()
                    is String -> parentAdcodeValue
                    else -> continue // 跳过无效数据
                }

                // 过滤掉特殊区域
                if (parentAdcode.contains("_")) {
                    continue
                }

                // 过滤出真正的市级别数据
                // 市的 level 应该是 "city" 或者是直辖市下的区（district）
                val isCity = when {
                    level == "city" -> true
                    // 直辖市下的区也算作市级别
                    parentAdcode in PROVINCE_CODES && level == "district" -> true
                    else -> false
                }

                if (isCity) {
                    val cityInfo = CityItemInfo(
                        adcode = adcode,
                        name = name,
                        parentAdcode = parentAdcode
                    )

                    // 按父级省份分组
                    if (!citiesByProvince.containsKey(parentAdcode)) {
                        citiesByProvince[parentAdcode] = mutableListOf()
                    }
                    citiesByProvince[parentAdcode]?.add(cityInfo)
                    cityCount++
                }
            }

            Log.d(TAG, "加载完成，共 $cityCount 个城市，分布在 ${citiesByProvince.size} 个省份")

            // 对每个省份的城市按名称排序
            citiesByProvince.mapValues { (_, cities) ->
                cities.sortedBy { it.name }
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载城市数据失败", e)
            emptyMap()
        }
    }

    /**
     * 加载所有城市数据（省份+城市）
     *
     * @return Pair<省份列表, 按省份分组的城市列表>
     */
    suspend fun loadAllGeoData(): Pair<List<ProvinceInfo>, Map<String, List<CityItemInfo>>> {
        Log.d(TAG, "开始加载所有地理数据...")
        val provinces = loadProvinces()
        val citiesByProvince = loadCitiesByProvince()
        Log.d(TAG, "地理数据加载完成: ${provinces.size} 省份, ${citiesByProvince.size} 省份有城市数据")
        return Pair(provinces, citiesByProvince)
    }

    companion object {
        private const val TAG = "GeoDataRepository"
        private const val PROVINCE_FILE = "中华人民共和国(省).geojson"
        private const val CITY_FILE = "中华人民共和国(市).geojson"

        // 直辖市代码列表
        private val PROVINCE_CODES = setOf(
            "110000", // 北京市
            "120000", // 天津市
            "310000", // 上海市
            "500000"  // 重庆市
        )
    }
}
