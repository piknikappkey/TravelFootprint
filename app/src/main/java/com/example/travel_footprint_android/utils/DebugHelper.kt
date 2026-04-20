// app/src/main/java/com/example/travel_footprint_android/utils/DebugHelper.kt
package com.example.travel_footprint_android.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.example.travel_footprint_android.domain.usecase.AppService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugHelper @Inject constructor(
    private val appService: AppService,
    @ApplicationContext private val context: Context
) {

    private val tag = "AppServiceTest"

    // ==================== 图片测试 ====================

    /**
     * 创建测试图片（模拟）
     */
    fun createTestBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.RED)

        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("测试图片", 100f, 100f, paint)

        return bitmap
    }

    /**
     * 保存测试图片到本地
     */
    suspend fun saveTestImage(): String {
        Log.d(tag, "========== 保存测试图片 ==========")

        val bitmap = createTestBitmap()
        val fileName = "test_image_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)

        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            Log.d(tag, "✅ 测试图片保存成功: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(tag, "❌ 保存图片失败: ${e.message}")
            ""
        }
    }

    /**
     * 为足迹添加图片（自动获取第一个足迹）
     */
    suspend fun testAddPhotoToFootprint() {
        Log.d(tag, "========== 测试为足迹添加图片 ==========")

        // 1. 先获取所有旅程
        val journeys = appService.getAllJourneys().first()
        if (journeys.isEmpty()) {
            Log.d(tag, "❌ 没有旅程，请先创建旅程")
            return
        }

        // 2. 获取第一个旅程的足迹
        val firstJourney = journeys.first()
        val footprints = appService.getFootprintsForMap(firstJourney.id).first()

        if (footprints.isEmpty()) {
            Log.d(tag, "❌ 旅程 ${firstJourney.title} 没有足迹，请先添加足迹")
            return
        }

        // 3. 使用第一个足迹
        val footprint = footprints.first()
        val footprintId = footprint.id
        Log.d(tag, "使用足迹ID: $footprintId (${footprint.title})")

        // 4. 创建测试图片
        val imagePath = saveTestImage()
        if (imagePath.isEmpty()) {
            Log.d(tag, "❌ 创建测试图片失败")
            return
        }

        // 5. 生成缩略图
        val thumbnailPath = appService.generateThumbnail(imagePath)
        if (thumbnailPath.isEmpty()) {
            Log.d(tag, "⚠️ 生成缩略图失败，但原图已保存")
        } else {
            Log.d(tag, "缩略图已生成: $thumbnailPath")
        }

        // 6. 将图片关联到足迹
        val mediaId = appService.addPhotoToFootprint(
            footprintId = footprintId,
            imagePath = imagePath,
            thumbnailPath = thumbnailPath,
            caption = "测试图片 ${System.currentTimeMillis()}"
        )

        if (mediaId > 0) {
            Log.d(tag, "✅ 图片已关联到足迹，mediaId: $mediaId")
        } else {
            Log.d(tag, "❌ 图片关联失败")
        }
    }

    /**
     * 获取足迹图片（自动获取第一个足迹）
     */
    suspend fun testGetFootprintPhotos() {
        Log.d(tag, "========== 测试获取足迹图片 ==========")

        // 1. 先获取所有旅程
        val journeys = appService.getAllJourneys().first()
        if (journeys.isEmpty()) {
            Log.d(tag, "❌ 没有旅程")
            return
        }

        // 2. 获取第一个旅程的足迹
        val firstJourney = journeys.first()
        val footprints = appService.getFootprintsForMap(firstJourney.id).first()

        if (footprints.isEmpty()) {
            Log.d(tag, "❌ 没有足迹")
            return
        }

        // 3. 使用第一个足迹
        val footprint = footprints.first()
        Log.d(tag, "查询足迹ID: ${footprint.id} (${footprint.title})")

        val photos = appService.getPhotosByFootprint(footprint.id)
        Log.d(tag, "✅ 共有 ${photos.size} 张图片")
        photos.forEach { photo ->
            Log.d(tag, "   [${photo.id}] ${photo.caption}")
            Log.d(tag, "       路径: ${photo.localPath}")
            Log.d(tag, "       缩略图: ${photo.thumbnailPath}")
        }
    }
    suspend fun testJourneyWithPhoto() {
        Log.d(tag, "🎯 ========== 测试旅程+足迹+图片完整流程 ==========")

        // 1. 创建旅程
        val journeyId = appService.createJourney("图片测试旅程", "watercolor", "测试图片功能")
        Log.d(tag, "✅ 创建旅程成功，ID: $journeyId")

        // 2. 添加足迹
        val footprintId = appService.addFootprint(journeyId, 39.9042, 116.4074, "天安门广场")
        Log.d(tag, "✅ 添加足迹成功，ID: $footprintId")

        // 3. 为足迹添加图片（直接调用带参数的内部方法）
        // 创建测试图片
        val imagePath = saveTestImage()
        if (imagePath.isNotEmpty()) {
            val thumbnailPath = appService.generateThumbnail(imagePath)
            val mediaId = appService.addPhotoToFootprint(
                footprintId = footprintId,
                imagePath = imagePath,
                thumbnailPath = thumbnailPath,
                caption = "测试图片 ${System.currentTimeMillis()}"
            )
            if (mediaId > 0) {
                Log.d(tag, "✅ 图片已关联到足迹，mediaId: $mediaId")
            } else {
                Log.d(tag, "❌ 图片关联失败")
            }
        }

        // 4. 获取足迹图片
        val photos = appService.getPhotosByFootprint(footprintId)
        Log.d(tag, "✅ 共有 ${photos.size} 张图片")
        photos.forEach { photo ->
            Log.d(tag, "   [${photo.id}] ${photo.caption}")
            Log.d(tag, "       路径: ${photo.localPath}")
            Log.d(tag, "       缩略图: ${photo.thumbnailPath}")
        }

        Log.d(tag, "🎯 ========== 测试完成 ==========")
    }

    /**
     * 从指定路径加载图片并关联到足迹
     * @param imagePath 本地图片绝对路径
     * @param footprintId 足迹ID（可选，不传则自动获取第一个）
     */
    suspend fun testAddLocalImageToFootprint(imagePath: String, footprintId: Long? = null) {
        Log.d(tag, "========== 添加本地图片到足迹 ==========")

        // 检查图片是否存在
        val imageFile = File(imagePath)
        if (!imageFile.exists()) {
            Log.d(tag, "❌ 图片不存在: $imagePath")
            return
        }

        // 获取足迹ID
        var targetFootprintId = footprintId
        if (targetFootprintId == null) {
            val journeys = appService.getAllJourneys().first()
            if (journeys.isEmpty()) {
                Log.d(tag, "❌ 没有旅程")
                return
            }
            val footprints = appService.getFootprintsForMap(journeys.first().id).first()
            if (footprints.isEmpty()) {
                Log.d(tag, "❌ 没有足迹")
                return
            }
            targetFootprintId = footprints.first().id
        }

        Log.d(tag, "使用足迹ID: $targetFootprintId")

        // 复制图片到应用目录
        val savedPath = appService.saveImageToApp(imagePath)
        if (savedPath.isEmpty()) {
            Log.d(tag, "❌ 保存图片失败")
            return
        }
        Log.d(tag, "图片已保存: $savedPath")

        // 生成缩略图并关联
        val thumbnailPath = appService.generateThumbnail(savedPath)
        val mediaId = appService.addPhotoToFootprint(
            footprintId = targetFootprintId,
            imagePath = savedPath,
            thumbnailPath = thumbnailPath,
            caption = "本地图片 ${System.currentTimeMillis()}"
        )

        if (mediaId > 0) {
            Log.d(tag, "✅ 图片已关联到足迹，mediaId: $mediaId")
        } else {
            Log.d(tag, "❌ 图片关联失败")
        }
    }

    /**
     * 测试：添加设备上的图片（需要提供完整路径）
     * 示例：/storage/emulated/0/DCIM/Camera/photo.jpg
     */
    suspend fun testAddDeviceImage() {
        // 请修改为设备上的实际图片路径
        val imagePath = "/storage/emulated/0/Pictures/test.jpg"
        testAddLocalImageToFootprint(imagePath)
    }



    // ==================== 点亮城市测试 ====================

    /**
     * 测试点亮城市
     */
    suspend fun testLightCity(
        cityAdcode: String = "110100",
        cityName: String = "北京市",
        provinceAdcode: String = "110000",
        provinceName: String = "北京市",
        latitude: Double = 39.9042,
        longitude: Double = 116.4074
    ) {
        Log.d(tag, "========== 测试点亮城市 ==========")
        Log.d(tag, "城市: $cityName ($cityAdcode)")
        Log.d(tag, "省份: $provinceName ($provinceAdcode)")

        val id = appService.lightCity(
            cityAdcode = cityAdcode,
            cityName = cityName,
            provinceAdcode = provinceAdcode,
            provinceName = provinceName,
            latitude = latitude,
            longitude = longitude
        )

        if (id > 0) {
            Log.d(tag, "✅ 点亮成功，ID: $id")
        } else if (id == -1L) {
            Log.d(tag, "⚠️ 城市已点亮，无需重复点亮")
        } else {
            Log.d(tag, "❌ 点亮失败")
        }
    }

    /**
     * 测试取消点亮城市
     */
    suspend fun testUnlightCity(cityAdcode: String = "110100") {
        Log.d(tag, "========== 测试取消点亮城市 ==========")
        Log.d(tag, "城市代码: $cityAdcode")

        appService.unlightCity(cityAdcode)
        Log.d(tag, "✅ 取消点亮成功")
    }

    /**
     * 测试获取所有点亮城市
     */
    suspend fun testGetAllLightedCities() {
        Log.d(tag, "========== 测试获取所有点亮城市 ==========")

        val cities = appService.getAllLightedCities().first()
        Log.d(tag, "✅ 共有 ${cities.size} 个点亮城市:")
        cities.forEach { city ->
            Log.d(tag, "   [${city.cityAdcode}] ${city.cityName}")
            Log.d(tag, "       省份: ${city.provinceName}")
            Log.d(tag, "       点亮时间: ${city.getFormattedTime()}")
        }
    }

    /**
     * 测试获取点亮城市数量
     */
    suspend fun testGetLightedCityCount() {
        Log.d(tag, "========== 测试获取点亮城市数量 ==========")

        val count = appService.getLightedCityCount()
        Log.d(tag, "✅ 当前点亮城市数量: $count")
    }

    /**
     * 测试检查城市是否已点亮
     */
    suspend fun testIsCityLighted(cityAdcode: String = "110100") {
        Log.d(tag, "========== 测试检查城市是否已点亮 ==========")
        Log.d(tag, "城市代码: $cityAdcode")

        val isLighted = appService.isCityLighted(cityAdcode)
        Log.d(tag, "✅ 点亮状态: ${if (isLighted) "已点亮" else "未点亮"}")
    }

    /**
     * 测试批量点亮多个城市
     */
    suspend fun testBatchLightCities() {
        Log.d(tag, "========== 测试批量点亮城市 ==========")

        val cities = listOf(
            Triple("110100", "北京市", "110000") to Pair(39.9042, 116.4074),
            Triple("310100", "上海市", "310000") to Pair(31.2304, 121.4737),
            Triple("440100", "广州市", "440000") to Pair(23.1291, 113.2644),
            Triple("440300", "深圳市", "440000") to Pair(22.5431, 114.0579),
            Triple("510100", "成都市", "510000") to Pair(30.5728, 104.0668)
        )

        cities.forEach { (info, latLng) ->
            val (adcode, name, parentAdcode) = info
            val (lat, lng) = latLng
            testLightCity(adcode, name, parentAdcode, name, lat, lng)
        }

        Log.d(tag, "========== 批量点亮完成 ==========")
    }

    /**
     * 完整测试点亮城市流程
     */
    suspend fun testLightCityFull() {
        Log.d(tag, "🎯 ========== 开始点亮城市完整测试 ==========")

        // 1. 查看当前点亮城市数量
        testGetLightedCityCount()

        // 2. 点亮北京市
        testLightCity("110100", "北京市", "110000", "北京市", 39.9042, 116.4074)

        // 3. 检查北京市是否已点亮
        testIsCityLighted("110100")

        // 4. 再次尝试点亮北京市（应该提示已点亮）
        testLightCity("110100", "北京市", "110000", "北京市", 39.9042, 116.4074)

        // 5. 批量点亮更多城市
        testBatchLightCities()

        // 6. 获取所有点亮城市
        testGetAllLightedCities()

        // 7. 再次获取点亮城市数量
        testGetLightedCityCount()

        // 8. 取消点亮北京市
        testUnlightCity("110100")

        // 9. 再次获取所有点亮城市（验证北京已移除）
        testGetAllLightedCities()

        // 10. 最终统计
        testGetLightedCityCount()

        Log.d(tag, "🎯 ========== 点亮城市测试完成 ==========")
    }



    /**
     * 测试独立点亮省份（不依赖城市）
     */
    suspend fun testLightProvinceOnly(
        provinceAdcode: String = "440000",
        provinceName: String = "广东省"
    ) {
        Log.d(tag, "========== 测试独立点亮省份 ==========")
        Log.d(tag, "省份: $provinceName ($provinceAdcode)")

        val id = appService.lightProvince(
            provinceAdcode = provinceAdcode,
            provinceName = provinceName
        )

        if (id > 0) {
            Log.d(tag, "✅ 点亮省份成功，ID: $id")
        } else if (id == -1L) {
            Log.d(tag, "⚠️ 省份已点亮，无需重复点亮")
        } else {
            Log.d(tag, "❌ 点亮省份失败")
        }
    }

    /**
     * 测试取消点亮省份
     */
    suspend fun testUnlightProvinceOnly(provinceAdcode: String = "440000") {
        Log.d(tag, "========== 测试取消点亮省份 ==========")
        Log.d(tag, "省份代码: $provinceAdcode")

        appService.unlightProvince(provinceAdcode)
        Log.d(tag, "✅ 取消点亮省份成功")
    }

    /**
     * 测试批量独立点亮省份
     */
    suspend fun testBatchLightProvincesOnly() {
        Log.d(tag, "========== 测试批量独立点亮省份 ==========")

        val provinces = listOf(
            Pair("110000", "北京市"),
            Pair("310000", "上海市"),
            Pair("440000", "广东省"),
            Pair("510000", "四川省"),
            Pair("320000", "江苏省")
        )

        provinces.forEach { (adcode, name) ->
            testLightProvinceOnly(adcode, name)
        }

        Log.d(tag, "========== 批量独立点亮省份完成 ==========")
    }


    /**
     * 测试获取所有省份
     */
    suspend fun testGetAllProvinces() {
        Log.d(tag, "========== 测试获取所有省份 ==========")

        val provinces = appService.getAllProvinces().first()
        Log.d(tag, "✅ 共有 ${provinces.size} 个省份:")
        provinces.forEach { province ->
            Log.d(tag, "   [${province.adcode}] ${province.name}")
        }
    }

    /**
     * 测试获取省份下的所有城市
     */
    suspend fun testGetCitiesByProvince(provinceAdcode: String = "110000") {
        Log.d(tag, "========== 测试获取省份下的城市 ==========")
        Log.d(tag, "省份代码: $provinceAdcode")

        val cities = appService.getCitiesByProvince(provinceAdcode).first()
        Log.d(tag, "✅ 共有 ${cities.size} 个城市:")
        cities.forEach { city ->
            Log.d(tag, "   [${city.adcode}] ${city.name}")
        }
    }

    /**
     * 测试搜索城市
     */
    suspend fun testSearchCities(keyword: String = "北京") {
        Log.d(tag, "========== 测试搜索城市 ==========")
        Log.d(tag, "关键词: $keyword")

        val cities = appService.searchCities(keyword)
        Log.d(tag, "✅ 找到 ${cities.size} 个匹配城市:")
        cities.forEach { city ->
            Log.d(tag, "   [${city.adcode}] ${city.name}")
        }
    }

}