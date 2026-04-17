package com.example.travel_footprint_android.presentation.components.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation.components.map.CityInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember

/**
 * 只读面板组件
 *
 * 显示已点亮城市的列表，支持收起和展开两种状态
 * 城市以标签形式横向排列，自动换行
 *
 * @param lightedCities 已点亮的城市列表
 * @param isExpanded 是否展开
 * @param onExpandClick 展开/收起按钮点击回调
 * @param onLightenClick "点亮城市"按钮点击回调
 * @param onCityLocationClick 城市定位图标点击回调
 */
@Composable
fun ReadOnlyPanel(
    lightedCities: List<CityInfo>,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onLightenClick: () -> Unit,
    onCityLocationClick: (CityInfo) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 0.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 标题栏
            LocationHeader(
                cityCount = lightedCities.size,
                onExpandClick = onExpandClick,
                showExpandButton = lightedCities.size > 6,
                isExpanded = isExpanded
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 城市标签流式布局
            CityTagsFlow(
                lightedCities = lightedCities,
                isExpanded = isExpanded,
                onCityLocationClick = onCityLocationClick
            )
        }

        // 右下角"点亮城市"按钮
        FloatingActionButton(
            onClick = onLightenClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 8.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "点亮城市",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/**
 * 位置标题栏组件
 *
 * @param cityCount 已点亮城市数量
 * @param onExpandClick 展开/收起按钮点击回调
 * @param showExpandButton 是否显示展开按钮
 * @param isExpanded 是否展开
 */
@Composable
private fun LocationHeader(
    cityCount: Int,
    onExpandClick: () -> Unit,
    showExpandButton: Boolean,
    isExpanded: Boolean
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 标题
        Text(
            text = "已点亮城市 ($cityCount)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        // 展开/收起按钮
        if (showExpandButton) {
            IconButton(
                onClick = onExpandClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Default.KeyboardArrowDown
                    } else {
                        Icons.Default.KeyboardArrowUp
                    },
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 城市标签流式布局组件
 *
 * 城市以标签形式横向排列，空间不足时自动换行
 *
 * @param lightedCities 已点亮的城市列表
 * @param isExpanded 是否展开
 * @param onCityLocationClick 城市定位图标点击回调
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CityTagsFlow(
    lightedCities: List<CityInfo>,
    isExpanded: Boolean,
    onCityLocationClick: (CityInfo) -> Unit
) {
    if (lightedCities.isEmpty()) {
        // 空状态
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "还没有点亮任何城市，快去探索吧！",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // 根据展开状态决定显示的城市数量
    val displayCities = if (isExpanded) {
        lightedCities
    } else {
        // 收起状态显示前8个（约2行）
        lightedCities.take(8)
    }

    // 使用 FlowRow 实现流式布局
    val scrollState = rememberScrollState()

    Box(
        modifier = if (isExpanded) {
            Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        } else {
            Modifier.fillMaxWidth()
        }
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = Int.MAX_VALUE
        ) {
            displayCities.forEach { city ->
                CityTag(
                    city = city,
                    onLocationClick = { onCityLocationClick(city) }
                )
            }

            // 收起状态下，如果还有更多城市，显示"+N"标签
            if (!isExpanded && lightedCities.size > 8) {
                MoreCitiesTag(count = lightedCities.size - 8)
            }
        }
    }
}

@Composable
private fun CityTag(
    city: CityInfo,
    onLocationClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),  // Material 3 的 ripple
                onClick = onLocationClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = city.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
/**
 * 更多城市标签组件
 *
 * 显示剩余城市数量
 *
 * @param count 剩余城市数量
 */
@Composable
private fun MoreCitiesTag(count: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "+$count",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
