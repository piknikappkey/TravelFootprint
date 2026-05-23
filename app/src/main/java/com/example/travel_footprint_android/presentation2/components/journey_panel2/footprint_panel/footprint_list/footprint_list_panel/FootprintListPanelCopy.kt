//package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.footprint_list.footprint_list_panel
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import com.example.travel_footprint_android.data.entity.Footprint
//import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.footprint_details.LocationRecorder
//import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.footprint_list.footprint_list_panel.FootprintListPanelState.PAUSE
//import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.footprint_list.footprint_list_panel.FootprintListPanelState.START
//import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.footprint_panel.footprint_list.footprint_list_panel.FootprintListPanelState.STOP
//
//@Composable
//fun FootprintListPanel(
//    footprint: Footprint,
//) {
//    var panelState by remember { mutableStateOf(FootprintListPanelState.STOP) }
//
//    // 开始时间
//    var startTime by remember { mutableStateOf<Long>(0) }
//
//    // 持续时间
//    var durationTime by remember { mutableStateOf<Long>(0) }
//
//    // 移动距离
//    var displacementDistance by remember { mutableStateOf(0) }
//
//    // 移动速度
//    var speed by remember { mutableStateOf(0) }
//
//    // 消耗卡路里
//    var calories by remember { mutableStateOf(0) }
//
//    // 是否记录用户定位
//    var isRecord by remember { mutableStateOf(true) }
//
//    LaunchedEffect(panelState) {
//        when(panelState) {
//            START -> {
//
//            }
//            PAUSE -> {
//
//            }
//            STOP -> {
//
//            }
//        }
//    }
//
//    // 位置记录组件 - 当 isRecord 为 true 时持续获取定位
//    LocationRecorder(isRecord = isRecord) { latitude, longitude ->
//
//    }
//
//    Column {
//        // 基础面板数据
//        // 开始时间
//        Text(text = "${startTime}")
//
//        // 持续时间
//        Text(text = "${durationTime}")
//
//        // 移动距离
//        Text(text = "${displacementDistance}")
//
//        // 移动速度
//        Text(text = "${speed}")
//
//        // 消耗卡路里
//        Text(text = "${calories}")
//
//        // 开始/暂停按钮
//        Button(
//            onClick = {
//                if(panelState == FootprintListPanelState.STOP || panelState == FootprintListPanelState.PAUSE) {
//                    panelState = FootprintListPanelState.START
//                } else {
//                    panelState = FootprintListPanelState.PAUSE
//                }
//            }
//        ) {
//            if(panelState == FootprintListPanelState.STOP || panelState == FootprintListPanelState.PAUSE) {
//                Text(text = "开始")
//            }
//
//            if(panelState == FootprintListPanelState.START) {
//                Text(text = "暂停")
//            }
//        }
//        // 结束按钮
//        if(panelState != FootprintListPanelState.STOP) {
//            Button(
//                onClick = {
//                    panelState = FootprintListPanelState.STOP
//                }
//            ) {
//                Text(text = "结束")
//            }
//        }
//    }
//}
//
//enum class FootprintListPanelState {
//    START, // 开始
//    PAUSE, // 暂停
//    STOP, // 结束
//}