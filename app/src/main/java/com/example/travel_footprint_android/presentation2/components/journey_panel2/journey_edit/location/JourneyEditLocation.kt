/**
 * 旅程地址编辑组件
 * 
 * 用途：
 * - 在旅程编辑页面中提供地点搜索和选择功能
 * - 允许用户搜索地点、查看选中位置详情、确认或取消选择、删除已选位置
 * 
 * 功能：
 * 1. 地点搜索：集成 LocationSearch 组件，支持关键词搜索高德地点建议
 * 2. 位置选择回调：选中地点后更新本地状态（名称/地址/经纬度），显示确认按钮
 * 3. 位置详情面板：展示已选位置的名称、地址、经纬度，提供删除按钮
 * 4. 确认/取消操作：
 *    - 确认：将地址信息（address\nname格式）和经纬度保存到 journey 实体
 *    - 取消：恢复到上次保存的状态（或清空如果之前无地址）
 *    - 删除：清空所有位置信息并更新 journey
 * 5. 地图标记同步：通过 LocationSearchViewModel 回调将选中位置同步到 JourneyMap3ViewModel 进行地图标记
 * 
 * 关联组件：
 * - Journey: 旅程实体，包含 address（存储格式："详细地址\n地点名称"）、latitude、longitude
 * - LocationSearchViewModel: 地点搜索 ViewModel，管理搜索文本、建议列表、选中位置状态
 *   - 提供 setOnLocationSelectedCallback 注册地图标记回调
 *   - 选中地点后通过 onLocationSelected 回调传出 LocationInfo
 * - JourneyMap3ViewModel: 地图 ViewModel，提供 setSelectedLocation() 在地图上标记选中位置
 * - LocationSearch: 地点搜索 Composable，包含搜索框和建议列表
 * 
 * 实现逻辑：
 * - 使用 remember 管理本地状态：isSelectedLocation（是否已选位置）、name/address/latitude/longitude（位置详情）、showButton（是否显示确认按钮）
 * - 初始化时解析 journey.address（按"\n"分割为 name 和 address），判断是否有已存位置
 * - LaunchedEffect 注册 LocationSearchViewModel 回调，将选中位置同步到地图
 * - LocationSearch.onLocationSelected 回调中更新本地状态并显示确认按钮
 * - LocationPanel 展示位置详情，submit/cancel/delete 三个回调分别处理确认/取消/删除逻辑
 * - 地址存储格式：address + "\n" + name（与解析逻辑对应）
 * 
 * @param journey 当前旅程实体
 * @param setJourney 更新旅程数据的回调
 * @param locationSearchViewModel 地点搜索 ViewModel（Hilt 注入）
 * @param journeyMap3ViewModel 地图 ViewModel（Hilt 注入）
 */
package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_edit.location

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.button.button_main.ButtonMain
import com.example.travel_footprint_android.presentation2.components.journey_map3.location_search.LocationSearch
import com.example.travel_footprint_android.presentation2.components.journey_map3.location_search.LocationSearchViewModel
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation2.components.text.text_small.TextSmall
import com.example.travel_footprint_android.presentation2.viewmodel.journey_map2_viewmodel.JourneyMap3ViewModel
import com.example.travel_footprint_android.ui.theme.FontDark6
import com.example.travel_footprint_android.ui.theme.FontDark8
import com.example.travel_footprint_android.ui.theme.MainColor3

// 旅程地址编辑主组件：地点搜索 + 位置详情面板 + 确认/取消/删除操作
@Composable
fun JourneyEditLocation(
    journey: Journey,
    setJourney: (Journey) -> Unit,
    locationSearchViewModel: LocationSearchViewModel = hiltViewModel(),
    journeyMap3ViewModel: JourneyMap3ViewModel = hiltViewModel(),
) {
    // 本地状态：是否已选择位置（根据 journey.address 是否为空判断）
    var isSelectedLocation by remember { mutableStateOf(journey.address != "") }

    // 本地状态：位置名称（journey.address 按"\n"分割的第一部分）
    var name by remember { mutableStateOf(if(journey.address != "") journey.address.split("\n")[0] else "") }

    // 本地状态：详细地址（journey.address 按"\n"分割的最后一部分）
    var address by remember { mutableStateOf(if(journey.address != "") journey.address.split("\n").last() else "") }

    // 本地状态：纬度
    var latitude by remember { mutableStateOf(if(journey.address != "") journey.latitude else 0.0) }

    // 本地状态：经度
    var longitude by remember { mutableStateOf(if(journey.address != "") journey.longitude else 0.0) }

    // 本地状态：是否显示确认/取消按钮（选中位置后显示）
    var showButton by remember { mutableStateOf(false) }

    // 初始化：注册 LocationSearchViewModel 回调，将选中位置同步到地图进行标记
    LaunchedEffect(Unit) {
        locationSearchViewModel.setOnLocationSelectedCallback { latLng ->
            journeyMap3ViewModel.setSelectedLocation(latLng)
        }
    }

    // 标签文字
    TextMedium(
        text = "旅程地址：",
        firstLine = 0,
        modifier = Modifier.padding(horizontal = 15.dp)
    )

    Spacer(Modifier.padding(2.dp))

    // 地点搜索组件：关键词搜索高德地点建议
    LocationSearch(
        locationSearchViewModel = locationSearchViewModel,
        onLocationSelected = { location ->
            Log.d("JourneyEditLocation", "location = $location")
            // 更新本地状态为选中的位置信息，并显示确认按钮
            isSelectedLocation = true
            showButton = true
            name = location.name
            address = location.address
            latitude = location.latitude
            longitude = location.longitude
        }
    )

    Spacer(Modifier.padding(2.dp))

    // 已选择位置时显示位置详情面板
    if(isSelectedLocation) {
        LocationPanel(
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            showButton = showButton,
            submit = {
                // 确认：将位置信息保存到 journey（address 格式：详细地址\n地点名称）
                Log.d("JourneyEditLocation", "submit")
                setJourney(
                    journey.copy(
                        address = address + "\n" + name,
                        longitude = longitude,
                        latitude = latitude
                    )
                )
                showButton = false
            },
            cancel = {
                // 取消：恢复到上次保存的状态
                Log.d("JourneyEditLocation", "cancel")
                if (journey.address != "") {
                    // 恢复为已保存的位置信息
                    name = journey.address.split("\n")[0]
                    address = journey.address.split("\n").last()
                    latitude = journey.latitude
                    longitude = journey.longitude
                    isSelectedLocation = true
                } else {
                    // 之前无地址，清空状态
                    name = ""
                    address = ""
                    latitude = 0.0
                    longitude = 0.0
                    isSelectedLocation = false
                }
                showButton = false
            },
            delete = {
                // 删除：清空所有位置信息并更新 journey
                showButton = false
                isSelectedLocation = false
                name = ""
                address = ""
                latitude = 0.0
                longitude = 0.0
                setJourney(
                    journey.copy(
                        address = "",
                        longitude = 0.0,
                        latitude = 0.0
                    )
                )
            }
        )
    }
}

/**
 * 位置详情展示面板
 * 
 * 用途：展示已选位置的详细信息，提供删除、取消、确认操作
 * 
 * 功能：
 * - 展示位置名称（带位置图标）、详细地址、经纬度
 * - 右上角删除按钮：点击触发 delete 回调清空位置
 * - 底部操作按钮（showButton=true 时显示）：取消/确认
 * 
 * @param name 地点名称
 * @param address 详细地址
 * @param latitude 纬度
 * @param longitude 经度
 * @param showButton 是否显示取消/确认按钮
 * @param submit 确认回调
 * @param cancel 取消回调
 * @param delete 删除回调
 */
@Composable
fun LocationPanel(
    name: String,
    address: String,
    latitude: Double,
    longitude: Double,
    showButton: Boolean,
    submit: () -> Unit,
    cancel: () -> Unit,
    delete: () -> Unit,
) {
    // 外层背景容器：带阴影和圆角
    BGBox (
        modifier = Modifier
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        // 内层背景图片容器：随机选择背景图
        BGImgBox(listOf(R.drawable.bg_rectangular_2__1__0, R.drawable.bg_rectangular_2__1__1, R.drawable.bg_rectangular_2__1__2, R.drawable.bg_rectangular_2__1__3),) {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, 8.dp)
            ) {
                // 标题行："已选择位置" + 删除按钮
                Row {
                    TextMedium(
                        text = "已选择位置",
                        color = FontDark6,
                        modifier = Modifier.padding(bottom = 8.dp),
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.weight(1f))
                    // 删除按钮：红色垃圾桶图标
                    Image(
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(onClick = delete),
                        painter = painterResource(R.drawable.ic_delete_trash),
                        contentDescription = "删除位置",
                        colorFilter = ColorFilter.tint(Color(0xFFFF0000)),
                    )
                    Spacer(Modifier.width(10.dp))
                }

                // 位置名称行：位置图标 + 名称
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "位置图标",
                        tint = MainColor3,
                        modifier = Modifier.size(20.dp)
                    )
                    Headline(
                        text = name,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(start = 28.dp)
                    )
                }

                // 详细地址（非空时显示）
                if (address.isNotEmpty()) {
                    Row {
                        Spacer(Modifier.weight(1f))
                        TextMedium(
                            text = address,
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                }

                // 经纬度信息（有效时显示，保留4位小数）
                if (latitude != 0.0 && longitude != 0.0) {
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Spacer(Modifier.weight(1f))
                        TextSmall(
                            text = "${String.format("%.4f", latitude)} - ${String.format("%.4f", longitude)}",
                            color = FontDark8,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 操作按钮行：取消 + 确认（showButton=true 时显示）
                if (showButton) {
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        ButtonMain(onClick = cancel) {
                            TextMedium("取消")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        ButtonMain(onClick = submit) {
                            TextMedium("确定")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}