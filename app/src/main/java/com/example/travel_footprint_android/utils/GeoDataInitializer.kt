// app/src/main/java/com/example/travel_footprint_android/utils/GeoDataInitializer.kt
package com.example.travel_footprint_android.utils

import android.content.Context
import android.util.Log
import com.example.travel_footprint_android.data.dao.CityDao
import com.example.travel_footprint_android.data.dao.ProvinceDao
import com.example.travel_footprint_android.data.entity.City
import com.example.travel_footprint_android.data.entity.Province
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object GeoDataInitializer {

    private const val TAG = "GeoDataInit"
    private const val JSON_FILE = "china_all_data.json"

    /**
     * 初始化省份和城市数据到数据库
     * 如果已有数据则跳过
     */
    suspend fun initializeData(
        context: Context,
        provinceDao: ProvinceDao,
        cityDao: CityDao
    ) = withContext(Dispatchers.IO) {
        // 检查是否已有数据
        val provinceCount = provinceDao.getProvinceCount()
        if (provinceCount > 0) {
            Log.d(TAG, "数据已存在，跳过初始化 (省份数: $provinceCount)")
            return@withContext
        }

        try {
            Log.d(TAG, "开始解析 JSON 文件...")
            val jsonString = context.assets.open(JSON_FILE)
                .bufferedReader()
                .use { it.readText() }

            val jsonObject = JSONObject(jsonString)

            // 1. 解析省份
            val provinces = parseProvinces(jsonObject)
            provinceDao.insertProvinces(provinces)
            Log.d(TAG, "✅ 成功插入 ${provinces.size} 个省份")

            // 2. 解析城市
            val cities = parseCities(jsonObject)
            cityDao.insertCities(cities)
            Log.d(TAG, "✅ 成功插入 ${cities.size} 个城市")

        } catch (e: Exception) {
            Log.e(TAG, "初始化失败: ${e.message}", e)
        }
    }

    /**
     * 省份中心经纬度映射
     */
    private val provinceCenterMap: Map<String, Pair<Double, Double>> = mapOf(
        "110000" to Pair(39.9042, 116.4074),  // 北京市
        "120000" to Pair(39.1252, 117.1908),  // 天津市
        "130000" to Pair(38.0455, 114.5020),  // 河北省
        "140000" to Pair(37.8570, 112.5624),  // 山西省
        "150000" to Pair(40.8174, 111.7653),  // 内蒙古自治区
        "210000" to Pair(41.8057, 123.4315),  // 辽宁省
        "220000" to Pair(43.8961, 125.3269),  // 吉林省
        "230000" to Pair(45.8038, 126.5350),  // 黑龙江省
        "310000" to Pair(31.2304, 121.4737),  // 上海市
        "320000" to Pair(32.0617, 118.7969),  // 江苏省
        "330000" to Pair(30.2741, 120.1551),  // 浙江省
        "340000" to Pair(31.8206, 117.2272),  // 安徽省
        "350000" to Pair(26.0745, 119.2965),  // 福建省
        "360000" to Pair(28.6742, 115.9092),  // 江西省
        "370000" to Pair(36.6683, 116.9972),  // 山东省
        "410000" to Pair(34.7657, 113.7532),  // 河南省
        "420000" to Pair(30.5464, 114.3420),  // 湖北省
        "430000" to Pair(28.2278, 112.9389),  // 湖南省
        "440000" to Pair(23.1317, 113.2663),  // 广东省
        "450000" to Pair(22.8166, 108.3669),  // 广西壮族自治区
        "460000" to Pair(20.0174, 110.3502),  // 海南省
        "500000" to Pair(29.4316, 106.5679),  // 重庆市
        "510000" to Pair(30.5728, 104.0668),  // 四川省
        "520000" to Pair(26.6470, 106.6302),  // 贵州省
        "530000" to Pair(25.0453, 102.7100),  // 云南省
        "540000" to Pair(29.6471, 91.1170),   // 西藏自治区
        "610000" to Pair(34.2655, 108.9543),  // 陕西省
        "620000" to Pair(36.0603, 103.8230),  // 甘肃省
        "630000" to Pair(36.6209, 101.7800),  // 青海省
        "640000" to Pair(38.4712, 106.2588),  // 宁夏回族自治区
        "650000" to Pair(43.7930, 87.6270),   // 新疆维吾尔自治区
        "710000" to Pair(25.0330, 121.5654),  // 台湾省
        "810000" to Pair(22.3193, 114.1694),  // 香港特别行政区
        "820000" to Pair(22.1987, 113.5491),  // 澳门特别行政区
    )

    /**
     * 解析省份数据
     */
    private fun parseProvinces(jsonObject: JSONObject): List<Province> {
        val provincesArray = jsonObject.getJSONArray("provinces")
        val provinces = mutableListOf<Province>()
        val seenAdcodes = mutableSetOf<String>()

        for (i in 0 until provincesArray.length()) {
            val item = provincesArray.getJSONObject(i)
            val adcode = item.getString("adcode")
            val name = item.getString("name")

            if (adcode.endsWith("0000") && seenAdcodes.add(adcode)) {
                val center = provinceCenterMap[adcode] ?: Pair(0.0, 0.0)
                provinces.add(
                    Province(
                        adcode = adcode,
                        name = name,
                        centerLat = center.first,
                        centerLng = center.second,
                        sortOrder = i
                    )
                )
            }
        }
        return provinces
    }

    /**
     * 解析城市数据
     */
    private fun parseCities(jsonObject: JSONObject): List<City> {
        val citiesArray = jsonObject.getJSONArray("cities")
        val cities = mutableListOf<City>()
        var cityCount = 0

        for (i in 0 until citiesArray.length()) {
            val item = citiesArray.getJSONObject(i)
            val adcode = item.getString("adcode")
            val name = item.getString("name")
            val parentAdcode = item.getString("parent")

            // 只添加市级行政区（adcode 以 00 结尾，且不是 0000 结尾）
            if (adcode.endsWith("00") ||adcode.endsWith("0000")) {
                cities.add(
                    City(
                        adcode = adcode,
                        name = name,
                        provinceAdcode = parentAdcode,
                        centerLat = 0.0,
                        centerLng = 0.0,
                        sortOrder = cityCount++
                    )
                )
            }
        }
        return cities
    }
}