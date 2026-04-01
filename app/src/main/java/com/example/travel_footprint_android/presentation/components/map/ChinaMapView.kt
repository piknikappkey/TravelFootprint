// app/src/main/java/com/example/travel_footprint_android/presentation/components/map/ChinaMapView.kt
package com.example.travel_footprint_android.presentation.components.map

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.domain.service.HandDrawStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 中国地图手绘组件
 *
 * 负责加载 GeoJSON 数据、绘制手绘风格地图，并处理用户交互
 * 支持手势：单指拖动平移、双指缩放
 * 支持自动切换：省级地图和城市地图根据缩放级别自动切换
 *
 * @param modifier 修饰符
 * @param mapStyle 地图手绘风格，默认为水彩风格
 * @param onProvinceClick 省份点击回调，参数为省份 adcode
 * @param onMapLoaded 地图加载完成回调
 */
@Composable
fun ChinaMapView(
    modifier: Modifier = Modifier,
    mapStyle: HandDrawStyle = HandDrawStyle.WATERCOLOR,
    onProvinceClick: (String) -> Unit = {},
    onMapLoaded: () -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // 地图数据状态
    var provinces by remember { mutableStateOf<List<MapPath>>(emptyList()) }
    var cities by remember { mutableStateOf<List<MapPath>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // 当前地图级别：true=城市级, false=省级
    var isCityLevel by remember { mutableStateOf(false) }

    // 选中的省份/城市
    var selectedItem by remember { mutableStateOf<MapPath?>(null) }

    // 手势状态
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // 缩放限制
    val minScale = 0.3f
    val maxScale = 15f
    // 切换阈值
    val switchThreshold = 3.5f

    // 异步加载 GeoJSON 数据（省级和城市级）
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                // 获取屏幕尺寸（使用一个较大的值以确保地图质量）
                val canvasWidth = with(density) { 1000.dp.toPx() }
                val canvasHeight = with(density) { 800.dp.toPx() }

                // 加载省级地图
                provinces = GeoJSONParser.parseProvinces(
                    context = context,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight
                ).map { it as MapPath }

                // 加载城市级地图
                cities = GeoJSONParser.parseCities(
                    context = context,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight
                ).map { it as MapPath }

                withContext(Dispatchers.Main) {
                    isLoading = false
                    onMapLoaded()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadError = e.message
                    isLoading = false
                }
            }
        }
    }

    // 监听缩放变化，自动切换地图级别
    LaunchedEffect(scale) {
        when {
            // 放大超过阈值，从省级切换到城市级
            !isCityLevel && scale >= switchThreshold -> {
                isCityLevel = true
            }
            // 缩小低于阈值，从城市级切换回省级
            isCityLevel && scale <= switchThreshold -> {
                isCityLevel = false
            }
        }
    }

    // 当前显示的地图数据
    val currentMapData = if (isCityLevel) cities else provinces

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(getBackgroundColor(mapStyle)),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
            loadError != null -> {
                androidx.compose.material3.Text(
                    text = "地图加载失败: $loadError",
                    color = MaterialTheme.colorScheme.error
                )
            }
            currentMapData.isNotEmpty() -> {
                MapCanvas(
                    mapData = currentMapData,
                    provinces = provinces,
                    mapStyle = mapStyle,
                    selectedItem = selectedItem,
                    scale = scale,
                    offset = offset,
                    onScaleChange = { scale = it },
                    onOffsetChange = { offset = it },
                    minScale = minScale,
                    maxScale = maxScale,
                    onItemClick = { item ->
                        selectedItem = item
                        onProvinceClick(item.adcode)
                    }
                )
            }
        }
    }
}

/**
 * 地图画布组件
 * 支持手势：拖动平移、双指缩放、点击
 * 使用 ZoomableImage 的双指缩放逻辑
 */
@Composable
private fun MapCanvas(
    mapData: List<MapPath>,
    provinces: List<MapPath>,
    mapStyle: HandDrawStyle,
    selectedItem: MapPath?,
    scale: Float,
    offset: Offset,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit,
    minScale: Float,
    maxScale: Float,
    onItemClick: (MapPath) -> Unit
) {
    // 使用 remember 来保存手势状态
    var currentScale by remember { mutableFloatStateOf(scale) }
    var currentOffset by remember { mutableStateOf(offset) }

    // 同步外部状态
    LaunchedEffect(scale) { currentScale = scale }
    LaunchedEffect(offset) { currentOffset = offset }

    // 使用 rememberUpdatedState 确保手势回调中始终使用最新的地图数据
    val currentMapData by rememberUpdatedState(mapData)
    val currentProvinces by rememberUpdatedState(provinces)
    val currentSelectedItem by rememberUpdatedState(selectedItem)

    Box(
        modifier = Modifier
            .fillMaxSize()
            // 点击检测 - 使用常量 key 避免重新创建
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val clickOffset = (tapOffset - currentOffset) / currentScale
                    val clickedItem = MapClickDetector.findProvinceAt(clickOffset, currentMapData)
                    clickedItem?.let { onItemClick(it) }
                }
            }
            // 变换手势（平移+缩放）- 使用常量 key 避免重新创建
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    // 计算新的缩放比例
                    val newScale = (currentScale * zoom).coerceIn(minScale, maxScale)

                    // 以双指中心点为锚点进行缩放
                    // 公式：newOffset = centroid - (centroid - oldOffset) * (newScale / oldScale)
                    val scaleRatio = newScale / currentScale
                    val newOffsetX = centroid.x - (centroid.x - currentOffset.x) * scaleRatio
                    val newOffsetY = centroid.y - (centroid.y - currentOffset.y) * scaleRatio

                    // 添加拖动偏移
                    val finalOffsetX = newOffsetX + pan.x
                    val finalOffsetY = newOffsetY + pan.y

                    currentScale = newScale
                    currentOffset = Offset(finalOffsetX, finalOffsetY)
                    onScaleChange(newScale)
                    onOffsetChange(Offset(finalOffsetX, finalOffsetY))
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = currentScale
                    scaleY = currentScale
                    translationX = currentOffset.x
                    translationY = currentOffset.y
                    // 设置变换中心点为左上角，配合offset实现以双指中心缩放
                    transformOrigin = TransformOrigin(0.0f, 0.0f)
                }
        ) {
            // 判断是否为城市级别：检查第一个元素是否为 CityPath
            val isCityLevel = currentMapData.firstOrNull() is CityPath

            if (isCityLevel) {
                // 城市级别：先绘制所有城市（虚线 - 市界）
                currentMapData.forEach { item ->
                    val isSelected = item.adcode == currentSelectedItem?.adcode
                    drawCityItem(item, mapStyle, isSelected, isDashed = true)
                }
                // 再叠加绘制省级边界（实线 - 省界）
                currentProvinces.forEach { province ->
                    drawProvinceBorder(province, mapStyle)
                }
            } else {
                // 省份级别：正常绘制
                currentMapData.forEach { item ->
                    val isSelected = item.adcode == currentSelectedItem?.adcode
                    drawCityItem(item, mapStyle, isSelected, isDashed = false)
                }
            }
        }
    }
}



/**
 * 绘制城市项（用于城市级别）
 * @param isDashed 是否使用虚线（市界用虚线，省界用实线）
 */
private fun DrawScope.drawCityItem(
    item: MapPath,
    mapStyle: HandDrawStyle,
    isSelected: Boolean,
    isDashed: Boolean
) {
    val (fillColor, strokeColor, strokeWidth) = when (mapStyle) {
        HandDrawStyle.WATERCOLOR -> Triple(
            if (isSelected) Color(0xFFE3F2FD) else Color(0xFFFFF8E1),
            Color(0xFF8D6E63),
            if (isDashed) 0.8f else 1.5f
        )
        HandDrawStyle.PENCIL_SKETCH -> Triple(
            if (isSelected) Color(0xFFE0E0E0) else Color(0xFFF5F5F5),
            Color(0xFF424242),
            if (isDashed) 0.5f else 1f
        )
        HandDrawStyle.VINTAGE_PAPER -> Triple(
            if (isSelected) Color(0xFFD7CCC8) else Color(0xFFF4E6C2),
            Color(0xFF5D4037),
            if (isDashed) 1f else 2f
        )
        HandDrawStyle.INK_WASH -> Triple(
            if (isSelected) Color(0xFFBDBDBD) else Color(0xFFECEFF1),
            Color(0xFF37474F),
            if (isDashed) 0.8f else 1.5f
        )
        HandDrawStyle.CRAYON -> Triple(
            if (isSelected) Color(0xFFFFCCBC) else Color(0xFFFFF3E0),
            Color(0xFFE65100),
            if (isDashed) 1.2f else 2.5f
        )
    }

    // 绘制填充
    drawPath(
        path = item.path,
        color = fillColor,
        style = Stroke(width = 0f)
    )

    // 绘制边界线
    val strokeStyle = if (isDashed) {
        // 虚线样式（市界）
        Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                intervals = floatArrayOf(5f, 3f),
                phase = 0f
            )
        )
    } else {
        // 实线样式（省界或省份级别）
        Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    }

    drawPath(
        path = item.path,
        color = strokeColor,
        style = strokeStyle
    )

    // 选中状态高亮
    if (isSelected) {
        drawPath(
            path = item.path,
            color = Color(0xFF2196F3).copy(alpha = 0.3f),
            style = Stroke(width = 0f)
        )
    }
}

/**
 * 绘制省份边界（用于城市级别时叠加显示省界）
 * 只绘制边界线，不绘制填充
 */
private fun DrawScope.drawProvinceBorder(
    province: MapPath,
    mapStyle: HandDrawStyle
) {
    val strokeColor = when (mapStyle) {
        HandDrawStyle.WATERCOLOR -> Color(0xFF8D6E63)
        HandDrawStyle.PENCIL_SKETCH -> Color(0xFF424242)
        HandDrawStyle.VINTAGE_PAPER -> Color(0xFF5D4037)
        HandDrawStyle.INK_WASH -> Color(0xFF37474F)
        HandDrawStyle.CRAYON -> Color(0xFFE65100)
    }

    val strokeWidth = when (mapStyle) {
        HandDrawStyle.WATERCOLOR -> 1.5f
        HandDrawStyle.PENCIL_SKETCH -> 1f
        HandDrawStyle.VINTAGE_PAPER -> 2f
        HandDrawStyle.INK_WASH -> 1.5f
        HandDrawStyle.CRAYON -> 2.5f
    }

    // 只绘制边界线（实线 - 省界）
    drawPath(
        path = province.path,
        color = strokeColor,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * 根据地图风格获取背景色
 */
private fun getBackgroundColor(mapStyle: HandDrawStyle): Color {
    return when (mapStyle) {
        HandDrawStyle.WATERCOLOR -> Color(0xFFFAFAFA)
        HandDrawStyle.PENCIL_SKETCH -> Color(0xFFFFFFFF)
        HandDrawStyle.VINTAGE_PAPER -> Color(0xFFF4E6C2)
        HandDrawStyle.INK_WASH -> Color(0xFFF5F5F5)
        HandDrawStyle.CRAYON -> Color(0xFFFFFDE7)
    }
}

/**
 * 中国地图视图状态
 * 用于外部控制地图状态
 */
data class ChinaMapViewState(
    val provinces: List<ProvincePath> = emptyList(),
    val selectedProvince: ProvincePath? = null,
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * 加载地图数据的辅助函数
 * 可在 ViewModel 中使用
 */
suspend fun loadMapData(
    context: Context,
    canvasWidth: Float,
    canvasHeight: Float
): Result<List<ProvincePath>> {
    return try {
        val provinces = GeoJSONParser.parseProvinces(
            context = context,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight
        )
        Result.success(provinces)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
