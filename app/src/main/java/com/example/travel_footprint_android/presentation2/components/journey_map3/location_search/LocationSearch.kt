package com.example.travel_footprint_android.presentation2.components.journey_map3.location_search

/**
 * LocationSearch - 地点搜索组件
 *
 * 功能：搜索并选择地点，支持搜索建议、位置反编码
 * 实现方法：
 *  - 使用 InputText3 组件作为搜索输入框
 *  - 调用高德地图 Inputtips API 获取搜索建议
 *  - 选择建议后通过 GeocodeSearch 进行地址解析
 *  - 使用 LazyColumn 展示搜索建议列表
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
import com.example.travel_footprint_android.ui.theme.BGLight0
import com.example.travel_footprint_android.ui.theme.BGLight2
import com.example.travel_footprint_android.ui.theme.FontDark2
import com.example.travel_footprint_android.ui.theme.FontDark6
import com.example.travel_footprint_android.ui.theme.MainColor3

@Composable
fun LocationSearch(
    modifier: Modifier = Modifier,
    locationSearchViewModel: LocationSearchViewModel = hiltViewModel(),
    onLocationSelected: (LocationSearchViewModel.LocationInfo) -> Unit = {}
) {
    val searchText by locationSearchViewModel.searchText.collectAsState()
    val suggestions by locationSearchViewModel.searchSuggestions.collectAsState()
    val showSuggestions by locationSearchViewModel.showSuggestions.collectAsState()
    val selectedLocation by locationSearchViewModel.selectedLocation.collectAsState()
    var selectedLocaltionOld by remember { mutableStateOf(selectedLocation) }
    val isSearching by locationSearchViewModel.isSearching.collectAsState()

    Column(modifier = modifier) {
        // 搜索输入框
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

        // 搜索建议列表
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

        // 加载状态
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

        // 已选择的位置信息
        selectedLocation?.let { location ->
//            SelectedLocationInfo(
//                location = location,
//                onClear = { locationSearchViewModel.clearSelection() },
//                onConfirm = { onLocationSelected(location) }
//            )
            if(selectedLocaltionOld != selectedLocation) {
                onLocationSelected(location)
                selectedLocaltionOld = selectedLocation
            }
        }
    }
}

/** 搜索建议单项 */
@Composable
fun SuggestionItem(
    tip: Tip,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = tip.name,
            color = FontDark2,
            style = TextStyle(fontSize = 16.sp)
        )
        if (!tip.district.isNullOrEmpty() || !tip.address.isNullOrEmpty()) {
            Text(
                text = tip.district ?: tip.address ?: "",
                color = FontDark6,
                style = TextStyle(fontSize = 14.sp),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Divider(
            color = FontDark6.copy(alpha = 0.1f),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/** 已选位置的展示卡片 */
@Composable
fun SelectedLocationInfo(
    location: LocationSearchViewModel.LocationInfo,
    onClear: () -> Unit,
    onConfirm: () -> Unit
) {
    var showButton by remember { mutableStateOf(true) }

    LaunchedEffect(location) {
        showButton = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .background(BGLight2, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "已选择位置",
            color = FontDark6,
            style = TextStyle(fontSize = 14.sp),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 位置名称
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

        // 详细地址
        if (location.address.isNotEmpty()) {
            Text(
                text = location.address,
                color = FontDark6,
                style = TextStyle(fontSize = 14.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 经纬度信息
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
        if(showButton) {
            Row {
                Spacer(
                    modifier = Modifier
                        .weight(1f),
                )
                ButtonMain(
                    title = "取消"
                ) {
                    onClear()
                }
                Spacer(
                    modifier = Modifier
                        .width(10.dp),
                )
                ButtonMain(
                    title = "确定"
                ) {
                    showButton = false
                    onConfirm()
                }
            }
        }
    }
}