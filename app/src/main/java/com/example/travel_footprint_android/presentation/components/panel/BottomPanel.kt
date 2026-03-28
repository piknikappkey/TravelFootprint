package com.example.travel_footprint_android.presentation.components.panel

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation.components.map.CityInfo
import java.time.LocalDate

/**
 * 底部面板组件
 *
 * 可折叠的底部面板容器，支持三种状态切换：
 * - COLLAPSED: 收起状态（120dp），显示标题和少量城市列表
 * - EXPANDED: 展开状态（350dp），显示完整城市列表
 * - EDIT_MODE: 编辑模式（350dp），显示省份-城市选择器和日期选择器
 *
 * 特性：
 * - 流畅的高度动画（300ms，FastOutSlowInEasing）
 * - 内容切换淡入淡出动画
 * - 支持手势拖动收起/展开
 * - 动画冲突保护
 *
 * @param modifier 修饰符
 * @param panelState 当前面板状态（由 ViewModel 提供）
 * @param lightedCities 已点亮的城市列表
 * @param selectedCities 编辑模式下已选择的城市代码集合
 * @param lightedTime 点亮时间
 * @param onPanelStateChange 面板状态变化回调
 * @param onCityLocationClick 点击城市定位图标回调
 * @param onSaveClick 保存编辑回调（城市代码集合，点亮时间）
 * @param onBackClick 返回/取消编辑回调
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomPanel(
    modifier: Modifier = Modifier,
    panelState: PanelState = PanelState.COLLAPSED,
    lightedCities: List<CityInfo> = emptyList(),
    selectedCities: Set<String> = emptySet(),
    lightedTime: LocalDate = LocalDate.now(),
    onPanelStateChange: (PanelState) -> Unit = {},
    onCityLocationClick: (CityInfo) -> Unit = {},
    onSaveClick: (Set<String>, LocalDate) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    // ========== 内部状态 ==========
    // 动画进行中标志，防止动画冲突
    var isAnimating by remember { mutableStateOf(false) }

    // ========== 高度动画 ==========
    val animatedHeight by animateDpAsState(
        targetValue = when (panelState) {
            PanelState.COLLAPSED -> COLLAPSED_HEIGHT
            PanelState.EXPANDED -> EXPANDED_HEIGHT
            PanelState.EDIT_MODE -> EDIT_MODE_HEIGHT
        },
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        ),
        finishedListener = {
            isAnimating = false
        },
        label = "panelHeight"
    )

    // ========== 渲染逻辑 ==========
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp
                )
            )
            .clip(
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp
                )
            )
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // 内容区域（带动画切换）
        AnimatedContent(
            targetState = panelState,
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedHeight),
            transitionSpec = {
                // 内容切换动画
                when {
                    // 从收起/展开切换到编辑模式：从底部滑入
                    targetState == PanelState.EDIT_MODE -> {
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(ANIMATION_DURATION_MS)
                        ) + fadeIn() with
                        slideOutVertically(
                            targetOffsetY = { -it / 2 },
                            animationSpec = tween(ANIMATION_DURATION_MS)
                        ) + fadeOut()
                    }
                    // 从编辑模式切换回收起/展开：向底部滑出
                    initialState == PanelState.EDIT_MODE -> {
                        slideInVertically(
                            initialOffsetY = { -it / 2 },
                            animationSpec = tween(ANIMATION_DURATION_MS)
                        ) + fadeIn() with
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(ANIMATION_DURATION_MS)
                        ) + fadeOut()
                    }
                    // 收起和展开之间的切换：淡入淡出
                    else -> {
                        fadeIn(
                            animationSpec = tween(ANIMATION_DURATION_MS / 2)
                        ) with
                        fadeOut(
                            animationSpec = tween(ANIMATION_DURATION_MS / 2)
                        )
                    }
                }
            },
            label = "panelContent"
        ) { currentState ->
            when (currentState) {
                PanelState.COLLAPSED,
                PanelState.EXPANDED -> {
                    ReadOnlyPanel(
                        lightedCities = lightedCities,
                        isExpanded = currentState == PanelState.EXPANDED,
                        onExpandClick = {
                            if (!isAnimating) {
                                isAnimating = true
                                val newState = if (currentState == PanelState.EXPANDED) {
                                    PanelState.COLLAPSED
                                } else {
                                    PanelState.EXPANDED
                                }
                                onPanelStateChange(newState)
                            }
                        },
                        onLightenClick = {
                            if (!isAnimating) {
                                isAnimating = true
                                onPanelStateChange(PanelState.EDIT_MODE)
                            }
                        },
                        onCityLocationClick = { city ->
                            onCityLocationClick(city)
                        }
                    )
                }

                PanelState.EDIT_MODE -> {
                    EditPanel(
                        selectedCities = selectedCities,
                        lightedTime = lightedTime,
                        onSaveClick = { cities, time ->
                            onSaveClick(cities, time)
                            isAnimating = true
                            onPanelStateChange(PanelState.COLLAPSED)
                        },
                        onBackClick = {
                            onBackClick()
                            isAnimating = true
                            onPanelStateChange(PanelState.COLLAPSED)
                        }
                    )
                }
            }
        }
    }
}

// ========== 常量定义 ==========

/**
 * 收起状态高度
 */
private val COLLAPSED_HEIGHT = 250.dp

/**
 * 展开状态高度
 */
private val EXPANDED_HEIGHT = 450.dp

/**
 * 编辑模式高度
 */
private val EDIT_MODE_HEIGHT = 450.dp

/**
 * 动画时长（毫秒）
 */
private const val ANIMATION_DURATION_MS = 300
