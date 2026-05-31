package com.example.travel_footprint_android

/*
 * ============================================================================
 * Application.kt — 应用入口（Hilt + 高德地图初始化）
 * ============================================================================
 *
 * 【用途】
 *   应用程序的 Application 入口类，标记 @HiltAndroidApp 以启用 Dagger Hilt
 *   依赖注入框架，并在应用启动时完成两项关键初始化工作。
 *
 * 【功能】
 *   1. Hilt 依赖注入入口：@HiltAndroidApp 注解自动生成 Hilt 组件，
 *      使全局依赖注入（如 ViewModel、Dao）可用
 *   2. 高德地图隐私合规声明：根据中国相关法律法规和平台政策，
 *      在使用高德地图 SDK 前必须调用 updatePrivacyShow/Agree 接口
 *   3. 中国地区数据初始化：异步读取 assets/china_all_data.json 文件，
 *      解析省份和城市数据并写入 Room 数据库，供城市选择等功能使用
 *
 * 【关联组件】
 *   - AMapLocationClient（com.amap.api.location）：高德地图定位 SDK
 *     客户端，用于后续地图显示和位置搜索功能
 *   - AppDatabase（data.database）：Room 数据库实例，
 *     包含 Journey、Footprint、Province、City 等 10 个实体表
 *   - GeoDataInitializer（utils）：地区数据初始化工具类，
 *     读取 china_all_data.json 解析 Province 和 City 并插入数据库
 *   - ProvinceDao / CityDao（data.dao）：省份和城市的 Room DAO，
 *     提供数据库写入和查询操作
 *
 * 【简单实现逻辑】
 *   1. @HiltAndroidApp 注解 MyApplication 类，Hilt 在编译期
 *      生成 Application 级别的依赖注入组件
 *   2. onCreate() 中依次调用：
 *      a. initAMapPrivacy()：调用 AMapLocationClient.updatePrivacyShow()
 *         和 updatePrivacyAgree() 声明用户已同意隐私政策
 *      b. initGeoData()：在 IO 协程中读取 assets JSON 文件，
 *         通过 GeoDataInitializer 解析并写入 Province 表和 City 表
 *   3. GeoDataInitializer 内部先检查数据库是否已有数据，
 *      存在则跳过，避免重复初始化
 * ============================================================================
 */

import android.app.Application
import com.amap.api.location.AMapLocationClient
import com.example.travel_footprint_android.data.database.AppDatabase
import com.example.travel_footprint_android.utils.GeoDataInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// —— 应用入口类 ——
// 标记 Hilt 依赖注入，启动时初始化高德地图隐私声明和地区数据
@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. 高德地图 SDK 隐私合规声明（法律要求，必须在 SDK 初始化前调用）
        initAMapPrivacy()

        // 2. 异步初始化中国省份和城市地区数据到数据库
        initGeoData()
    }

    // —— 高德地图隐私合规初始化 ——
    // 向高德 SDK 声明用户已查看并同意隐私政策
    // 若未调用此接口，高德 SDK 将无法正常工作
    private fun initAMapPrivacy() {
        try {
            AMapLocationClient.updatePrivacyShow(this, true, true)
            AMapLocationClient.updatePrivacyAgree(this, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // —— 地区数据初始化 ——
    // 在 IO 线程中读取 assets/china_all_data.json，
    // 解析出省份和城市列表并写入 Room 数据库
    private fun initGeoData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getInstance(this@MyApplication)
                GeoDataInitializer.initializeData(
                    context = this@MyApplication,
                    provinceDao = database.provinceDao(),
                    cityDao = database.cityDao()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}