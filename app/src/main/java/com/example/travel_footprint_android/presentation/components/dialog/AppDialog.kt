package com.example.travel_footprint_android.presentation.components.dialog

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 自定义弹窗组件，功能与用法与 [androidx.compose.ui.window.Dialog] 相同。
 *
 * 基于 [Popup] 实现，相比原生 Dialog 的优势：
 * - 完全自定义遮罩颜色与透明度
 * - 可自定义进出场动画（缩放 + 淡入淡出）
 * - 完全掌控渲染层，便于后续品牌化改造
 *
 * @param onDismissRequest 点击遮罩或按系统返回键时触发的回调（用于关闭弹窗）
 * @param maskAlpha 遮罩透明度，默认 0.4f
 * @param maskColor 遮罩颜色，默认黑色
 * @param animationDurationMs 进出场动画时长（毫秒），默认 250ms
 * @param interceptDismissOnClick 拦截点击关闭的回调，返回 true 表示拦截关闭（不执行关闭动画），返回 false 或 null 表示正常关闭
 * @param content 弹窗内容
 */
@Composable
fun AppDialog(
    onDismissRequest: () -> Unit,
    maskAlpha: Float = 0.4f,
    maskColor: Color = Color.Black,
    animationDurationMs: Int = 250,
    interceptDismissOnClick: (() -> Boolean)? = null,
    content: @Composable () -> Unit,
) {
    // 控制动画状态
    var visible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // 弹窗打开时触发动画，并请求根容器焦点以截断焦点传递链
    LaunchedEffect(Unit) {
        visible = true
        focusRequester.requestFocus()
    }

    // 遮罩透明度动画
    val currentMaskAlpha by animateFloatAsState(
        targetValue = if (visible) maskAlpha else 0f,
        animationSpec = tween(durationMillis = animationDurationMs, easing = FastOutSlowInEasing),
        label = "mask_alpha"
    )

    // 内容缩放动画（从 0.85 缩放到 1.0）
    val contentScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = tween(durationMillis = animationDurationMs, easing = FastOutSlowInEasing),
        label = "content_scale"
    )

    // 内容透明度动画
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = animationDurationMs, easing = FastOutSlowInEasing),
        label = "content_alpha"
    )

    // 关闭弹窗（带动画）
    val dismissWithAnimation: () -> Unit = {
        visible = false
        scope.launch {
            delay(animationDurationMs.toLong())
            focusManager.clearFocus(force = true)
            onDismissRequest()
        }
    }

    Popup(
        onDismissRequest = dismissWithAnimation,
        alignment = Alignment.Center,
        offset = IntOffset.Zero,
        properties = PopupProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            focusable = true,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .focusable()
        ) {
            // 遮罩层：全屏覆盖，拦截点击事件
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(maskColor.copy(alpha = currentMaskAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            // 如果提供了拦截回调，先调用它判断是否要拦截关闭
                            val intercepted = interceptDismissOnClick?.invoke() ?: false
                            if (!intercepted) {
                                dismissWithAnimation()
                            }
                        },
                    )
            )

            // 内容层：居中显示，带缩放 + 淡入动画
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(5.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = contentScale
                            scaleY = contentScale
                            alpha = contentAlpha
                        }
                ) {
                    content()
                }
            }
        }
    }
}
