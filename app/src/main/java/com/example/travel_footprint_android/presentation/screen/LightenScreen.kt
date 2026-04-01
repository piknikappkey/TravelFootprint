package com.example.travel_footprint_android.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.travel_footprint_android.presentation.components.map.ChinaMapViewSVG
import com.example.travel_footprint_android.presentation.components.map.CityInfo
import com.example.travel_footprint_android.presentation.components.panel.BottomPanel
import com.example.travel_footprint_android.presentation.components.panel.PanelState
import java.time.LocalDate

/**
 * 点亮页面
 *
 * 核心功能：
 * 1. 显示 SVG 格式的中国地图（支持手势缩放和拖拽）
 * 2. 支持点击城市获取城市信息
 * 3. 底部面板管理已点亮城市列表
 *    - COLLAPSED: 收起状态（120dp）
 *    - EXPANDED: 展开状态（350dp）
 *    - EDIT_MODE: 编辑模式（350dp）
 */
@Composable
fun LightenScreen() {
    // 面板状态管理
    var panelState by remember { mutableStateOf(PanelState.COLLAPSED) }

    // 模拟已点亮的城市数据（20个城市）
    val lightedCities = remember { getMockLightedCities() }

    // 编辑模式下选中的城市
    var selectedCities by remember {
        mutableStateOf(lightedCities.map { it.adcode }.toSet())
    }

    // 点亮时间
    var lightedTime by remember { mutableStateOf(LocalDate.now()) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // SVG 地图组件 - 占满整个背景
        ChinaMapViewSVG(
            modifier = Modifier.fillMaxSize(),
            onCityClick = { cityInfo ->
                // 测试阶段：打印点击的城市信息
                println("点击了城市: ${cityInfo.name}, adcode: ${cityInfo.adcode}")
            },
            showInfoCard = true
        )

        // 底部面板 - 固定在底部
        BottomPanel(
            modifier = Modifier.align(Alignment.BottomCenter),
            panelState = panelState,
            lightedCities = lightedCities,
            selectedCities = selectedCities,
            lightedTime = lightedTime,
            onPanelStateChange = { newState ->
                panelState = newState
                println("面板状态切换: $newState")
            },
            onCityLocationClick = { city ->
                // 点击城市定位图标
                println("定位到城市: ${city.name}")
            },
            onSaveClick = { cities, time ->
                // 保存编辑结果
                selectedCities = cities
                lightedTime = time
                println("保存: 选中城市数=${cities.size}, 时间=$time")
            },
            onBackClick = {
                // 取消编辑
                println("取消编辑")
            }
        )
    }
}

/**
 * 获取模拟的已点亮城市数据
 *
 * 包含中国主要城市，共20个
 */
private fun getMockLightedCities(): List<CityInfo> {
    return listOf(
        // 直辖市
        CityInfo(
            name = "北京市",
            adcode = "110100",
            centerLng = "116.407526",
            centerLat = "39.90403",
            parentAdcode = "110000"
        ),
        CityInfo(
            name = "上海市",
            adcode = "310100",
            centerLng = "121.473701",
            centerLat = "31.230416",
            parentAdcode = "310000"
        ),
        CityInfo(
            name = "天津市",
            adcode = "120100",
            centerLng = "117.200983",
            centerLat = "39.084158",
            parentAdcode = "120000"
        ),
        CityInfo(
            name = "重庆市",
            adcode = "500100",
            centerLng = "106.504962",
            centerLat = "29.533155",
            parentAdcode = "500000"
        ),

        // 广西
        CityInfo(
            name = "南宁市",
            adcode = "450100",
            centerLng = "108.366543",
            centerLat = "22.817002",
            parentAdcode = "450000"
        ),
        CityInfo(
            name = "柳州市",
            adcode = "450200",
            centerLng = "109.428608",
            centerLat = "24.326291",
            parentAdcode = "450000"
        ),
        CityInfo(
            name = "桂林市",
            adcode = "450300",
            centerLng = "110.179953",
            centerLat = "25.234479",
            parentAdcode = "450000"
        ),

        // 广东省
        CityInfo(
            name = "广州市",
            adcode = "440100",
            centerLng = "113.264434",
            centerLat = "23.129162",
            parentAdcode = "440000"
        ),
        CityInfo(
            name = "深圳市",
            adcode = "440300",
            centerLng = "114.057868",
            centerLat = "22.543099",
            parentAdcode = "440000"
        ),
        CityInfo(
            name = "佛山市",
            adcode = "440600",
            centerLng = "113.121416",
            centerLat = "23.021548",
            parentAdcode = "440000"
        ),

        // 浙江省
        CityInfo(
            name = "杭州市",
            adcode = "330100",
            centerLng = "120.15507",
            centerLat = "30.274084",
            parentAdcode = "330000"
        ),
        CityInfo(
            name = "宁波市",
            adcode = "330200",
            centerLng = "121.550357",
            centerLat = "29.874556",
            parentAdcode = "330000"
        ),

        // 江苏省
        CityInfo(
            name = "南京市",
            adcode = "320100",
            centerLng = "118.796877",
            centerLat = "32.060255",
            parentAdcode = "320000"
        ),
        CityInfo(
            name = "苏州市",
            adcode = "320500",
            centerLng = "120.585315",
            centerLat = "31.298886",
            parentAdcode = "320000"
        ),

        // 四川省
        CityInfo(
            name = "成都市",
            adcode = "510100",
            centerLng = "104.066541",
            centerLat = "30.572269",
            parentAdcode = "510000"
        ),

        // 陕西省
        CityInfo(
            name = "西安市",
            adcode = "610100",
            centerLng = "108.93977",
            centerLat = "34.341574",
            parentAdcode = "610000"
        ),

        // 湖北省
        CityInfo(
            name = "武汉市",
            adcode = "420100",
            centerLng = "114.305392",
            centerLat = "30.593099",
            parentAdcode = "420000"
        ),

        // 湖南省
        CityInfo(
            name = "长沙市",
            adcode = "430100",
            centerLng = "112.938814",
            centerLat = "28.228209",
            parentAdcode = "430000"
        ),

        // 云南省
        CityInfo(
            name = "昆明市",
            adcode = "530100",
            centerLng = "102.832891",
            centerLat = "24.880095",
            parentAdcode = "530000"
        ),

        // 海南省
        CityInfo(
            name = "海口市",
            adcode = "460100",
            centerLng = "110.349228",
            centerLat = "20.017377",
            parentAdcode = "460000"
        )
    )
}
