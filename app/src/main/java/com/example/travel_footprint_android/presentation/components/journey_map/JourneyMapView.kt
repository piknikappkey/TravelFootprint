package com.example.travel_footprint_android.presentation.components.journey_map

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable

/**
 * 高德地图组件
 * 地图组件需要读取Room数据库中关于旅程和足迹的数据库表，并在地图上显示旅程标点
 * 当用户点击旅程标点时，要显示这个旅程下对应的足迹标点以及对应的足迹路线图
 */
@Composable
fun JourneyMapView() {
    Box() {
        /**
         * 显示高德地图（大小覆盖整个页面）
         */

        /**
         * 新建旅程按钮
         * 位于页面左上角
         * 未点击时为“+”图标
         * 点击后变为“X”图标
         * 如果此时用户点击地图，则
         *  在地图上创建旅程标点，并将默认信息（标题：未定义标题、描述：未定义描述。。。）填入数据库
         *  然后将旅程面板上移，并进入编辑旅程页面
         *  用户可对刚创建的旅程进行编辑与保存
         * 如果此时用户没有点击地图，则视为此次新建旅程无效，重置新建旅程按钮为“+”
         */
    }
}
