package com.example.travel_footprint_android

import android.app.Application
import com.amap.api.location.AMapLocationClient
import com.example.travel_footprint_android.data.database.AppDatabase
import com.example.travel_footprint_android.utils.GeoDataInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// MyApplication.kt
@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. 高德地图隐私合规
        initAMapPrivacy()

        // 2. 初始化地区数据
        initGeoData()
    }

    private fun initAMapPrivacy() {
        try {
            AMapLocationClient.updatePrivacyShow(this, true, true)
            AMapLocationClient.updatePrivacyAgree(this, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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