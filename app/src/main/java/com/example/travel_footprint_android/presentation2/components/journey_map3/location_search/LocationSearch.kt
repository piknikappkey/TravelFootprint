package com.example.travel_footprint_android.presentation2.components.journey_map3.location_search

/*
 * ============================================================================
 * LocationSearch.kt — 地点搜索组件（高德地图集成）
 * ============================================================================
 *
 * 【用途】
 *   提供地点搜索与选择功能，是行程地图编辑流程中的核心交互组件。
 *   用户可通过关键词搜索地点，从建议列表中选择，并将所选位置信息（名称、
 *   地址、经纬度）回传给父级组件。
 *
 * 【功能】
 *   1. 关键词搜索输入：使用自定义 InputText3 搜索框，输入关键词实时触发
 *      高德地图 Inputtips API 获取搜索建议
 *   2. 搜索建议列表：以 LazyColumn 展示建议结果，每项显示地点名称和区域
 *      信息，点击后触发地址解析
 *   3. 加载状态展示：搜索过程中显示"搜索中..."文字提示
 *   4. 位置选择回调：选择地点后自动调用 onLocationSelected 回调，将
 *      LocationInfo（名称/地址/经纬度）传出
 *   5. 已选位置详情卡片（独立组件）：展示已选择位置的名称、详细地址、
 *      经纬度信息，提供"取消"和"确定"操作按钮
 *
 * 【关联组件】
 *   - LocationSearchViewModel（同包）：Hilt ViewModel，管理搜索文本、
 *     建议列表、选中位置等状态，封装 Inputtips API 和 GeocodeSearch 调用
 *   - InputText3（input.input_text）：自定义可复用输入框组件，带图标前缀
 *     和聚焦边框反馈
 *   - ButtonMain（button.button_main）：通用按钮容器，带阴影和圆角背景
 *   - TextMedium（text.text_medium）：自定义中等文字组件，使用软萌初恋体
 *   - BGLight0/BGLight2（ui.theme）：白色和浅紫白背景色
 *   - FontDark2/FontDark6（ui.theme）：深色和灰色字体颜色
 *   - MainColor3（ui.theme）：紫色主色调，用于位置图标颜色
 *   - Tip（com.amap.api.services.help）：高德地图搜索建议数据类型
 *   - LocationInfo（LocationSearchViewModel）：内部数据类，封装位置信息
 *
 * 【简单实现逻辑】
 *   1. LocationSearch 主组件接收 ViewModel（默认 Hilt 注入）和选中回调
 *   2. 通过 collectAsState() 收集 ViewModel 的 StateFlow 状态：
 *      searchText（搜索文本）、suggestions（建议列表）、showSuggestions
 *      （是否显示建议）、selectedLocation（选中位置）、isSearching（搜索中）
 *   3. 组件进入时（LaunchedEffect）先清除上一次的选择状态
 *   4. 输入框输入内容 → ViewModel.updateSearchText() → 调用 Inputtips
 *      API → 更新 suggestions
 *   5. 建议列表以 LazyColumn 渲染 SuggestionItem，点击后调用 ViewModel
 *      selectLocation() 进行地理编码解析
 *   6. 选中位置变化时，通过 selectedLocaltionOld 去重，触发一次
 *      onLocationSelected 回调
 *   7. SelectedLocationInfo 独立卡片组件展示位置详情，"确定"按钮最终
 *      确认选择，"取消"按钮清除选择
 * ============================================================================
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amap.api.services.help.Tip
import com.example.travel_footprint_android.presentation2.components.button.button_main.ButtonMain
import com.example.travel_footprint_android.presentation2.components.input.input_text.InputText3
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.BGLight0
import com.example.travel_footprint_android.ui.theme.BGLight2
import com.example.travel_footprint_android.ui.theme.FontDark2
import com.example.travel_footprint_android.ui.theme.FontDark6
import com.example.travel_footprint_android.ui.theme.MainColor3

// —— 主组件：地点搜索器 ——
// 整合了搜索输入框、建议列表、加载态和位置选中回调
@Composable
fun LocationSearch(
    // 外部传入的 Modifier，用于父组件控制布局位置和尺寸
    modifier: Modifier = Modifier,
    // ViewModel 实例，默认通过 Hilt 自动注入，管理搜索状态和 API 调用
    locationSearchViewModel: LocationSearchViewModel = hiltViewModel(),
    // 位置选中时的回调，向外传递 LocationInfo（包含名称、地址、经纬度）
    onLocationSelected: (LocationSearchViewModel.LocationInfo) -> Unit = {}
) {
    // 从 ViewModel 收集各状态，collectAsState() 使 Compose 在 StateFlow 变化时重组
    val searchText by locationSearchViewModel.searchText.collectAsState()
    val suggestions by locationSearchViewModel.searchSuggestions.collectAsState()
    val showSuggestions by locationSearchViewModel.showSuggestions.collectAsState()
    val selectedLocation by locationSearchViewModel.selectedLocation.collectAsState()
    // 记录上一次选中的位置，用于判断位置是否发生了变化，防止重复回调
    var selectedLocaltionOld by remember { mutableStateOf(selectedLocation) }
    val isSearching by locationSearchViewModel.isSearching.collectAsState()

    // 组件首次进入时，清除上次残留的选择状态
    LaunchedEffect(Unit) {
        locationSearchViewModel.clearSelection()
    }

    // 垂直排列：搜索框 → 建议列表/加载态 → 选中位置信息
    Column(modifier = modifier) {
        // —— 搜索输入框 ——
        // 使用自定义 InputText3 组件，图标设置为搜索图标
        InputText3(
            value = searchText,
            onValueChange = { locationSearchViewModel.updateSearchText(it) },
            tipText = "搜索地点",
//            padding = PaddingValues(horizontal = 16.dp),
//            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
//            textStyle = TextStyle(fontSize = 16.sp, color = FontDark2),
//            primaryColor = MainColor3,
//            onSurfaceColor = FontDark2,
//            containerColor = BGLight2,
            imageVector = Icons.Default.Search
        )

        // —— 搜索建议列表 ——
        // 有建议且标志位为 true 时，显示带圆角白色背景的滚动列表
        if (showSuggestions && suggestions.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(BGLight0, RoundedCornerShape(12.dp))
                    .heightIn(max = 300.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(suggestions) { tip ->
                        SuggestionItem(
                            tip = tip,
                            onClick = {
                                locationSearchViewModel.selectLocation(tip)
                            }
                        )
                    }
                }
            }
        }

        // —— 搜索加载态 ——
        // 正在搜索时显示"搜索中..."文字
        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(BGLight0, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "搜索中...",
                    color = FontDark6,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // —— 已选位置处理 ——
        // selectedLocation 非空时，自动触发回调（已注释的 SelectedLocationInfo 卡片方案被替换为直接回调）
        selectedLocation?.let { location ->
//            SelectedLocationInfo(
//                location = location,
//                onClear = { locationSearchViewModel.clearSelection() },
//                onConfirm = { onLocationSelected(location) }
//            )
            // 仅当选中的位置与上一次不同时才触发回调，避免重复调用
            if(selectedLocaltionOld != selectedLocation) {
                onLocationSelected(location)
                selectedLocaltionOld = selectedLocation
            }
        }
    }
}

// —— 搜索建议单项组件 ——
// 展示地点名称和区域/地址信息，点击后触发选择回调
@Composable
fun SuggestionItem(
    // 高德地图搜索建议数据结构，包含名称、区域、地址和坐标
    tip: Tip,
    // 点击该建议项时的回调
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 地点名称（主要文字，较大、深色）
        Text(
            text = tip.name,
            color = FontDark2,
            style = TextStyle(fontSize = 16.sp)
        )
        // 区域或地址信息（次要文字，较小、灰色），优先显示 district（区域）
        if (!tip.district.isNullOrEmpty() || !tip.address.isNullOrEmpty()) {
            Text(
                text = tip.district ?: tip.address ?: "",
                color = FontDark6,
                style = TextStyle(fontSize = 14.sp),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        // 选项之间的分隔线（极浅灰色）
        Divider(
            color = FontDark6.copy(alpha = 0.1f),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// —— 已选位置详情卡片组件 ——
// 展示选中位置的名称、地址、经纬度，提供"取消"和"确定"按钮
// （当前在 LocationSearch 主组件中被注释，未启用）
@Composable
fun SelectedLocationInfo(
    // 已选中的位置信息数据
    location: LocationSearchViewModel.LocationInfo,
    // 取消选择回调（清除当前选择）
    onClear: () -> Unit,
    // 确认选择回调（最终确认并返回该位置）
    onConfirm: () -> Unit
) {
    // 控制按钮组的显示状态，"确定"点击后隐藏按钮防止重复操作
    var showButton by remember { mutableStateOf(true) }

    // 当选中的位置变化时，重新显示按钮
    LaunchedEffect(location) {
        showButton = true
    }

    // 卡片容器：浅紫白圆角背景
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .background(BGLight2, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // "已选择位置"标题
        Text(
            text = "已选择位置",
            color = FontDark6,
            style = TextStyle(fontSize = 14.sp),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // —— 位置名称行 ——
        // 左侧紫色位置图标 + 右侧地点名称
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
            Text(
                text = location.name,
                color = FontDark2,
                style = TextStyle(fontSize = 16.sp),
                modifier = Modifier.padding(start = 28.dp)
            )
        }

        // —— 详细地址 ——
        // 非空时显示
        if (location.address.isNotEmpty()) {
            Text(
                text = location.address,
                color = FontDark6,
                style = TextStyle(fontSize = 14.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // —— 经纬度信息 ——
        // 格式化保留 6 位小数展示
        Text(
            text = "纬度: ${String.format("%.6f", location.latitude)}",
            color = FontDark6,
            style = TextStyle(fontSize = 14.sp),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "经度: ${String.format("%.6f", location.longitude)}",
            color = FontDark6,
            style = TextStyle(fontSize = 14.sp),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        // —— 操作按钮行 ——
        // 右侧对齐："取消"清除选择，"确定"触发确认回调并隐藏按钮
        if(showButton) {
            Row {
                Spacer(
                    modifier = Modifier
                        .weight(1f),
                )
                ButtonMain(
                    onClick = {onClear()},
                ) {
                    TextMedium(
                        text = "取消"
                    )
                }
                Spacer(
                    modifier = Modifier
                        .width(10.dp),
                )
                ButtonMain(
                    onClick = {
                        showButton = false
                        onConfirm()
                    },
                ) {
                    TextMedium(
                        text = "确定"
                    )
                }
            }
        }
    }
}