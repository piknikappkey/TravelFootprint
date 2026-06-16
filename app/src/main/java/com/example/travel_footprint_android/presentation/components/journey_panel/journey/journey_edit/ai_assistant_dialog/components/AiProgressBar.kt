package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.ui.theme.MainColor1
import com.example.travel_footprint_android.ui.theme.MainColor3
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * AI 进度条组件
 *
 * 不规则随机增长到 99%（1分钟内），若 AI 仍未完成则停在 99%，
 * AI 完成时跳到 100% 并短暂停留后消失。
 *
 * @param isActive AI 是否正在运行
 * @param modifier 外部 Modifier
 */
@Composable
fun AiProgressBar(
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "ai_progress"
    )

    LaunchedEffect(isActive) {
        if (isActive) {
            // 开始：从 0 向 99% 不规则增长
            progress = 0f
            val startTime = System.currentTimeMillis()

            while (progress < 0.99f) {
                val elapsed = System.currentTimeMillis() - startTime
                // 基于时间的上限：1分钟内线性增长到 99%
                val timeBasedMax = (elapsed / 60_000f * 0.99f).coerceAtMost(0.99f)

                // 随机增量：有时快有时慢，模拟"不规则"感
                val randomFactor = 0.5f + Random.nextFloat() * 1.0f
                val increment = (timeBasedMax - progress) * 0.15f * randomFactor
                progress = (progress + increment.coerceAtLeast(0.005f))
                    .coerceAtMost(timeBasedMax.coerceAtMost(0.99f))

                delay(Random.nextLong(300, 1500))
            }
        } else {
            // AI 完成：跳到 100%，短暂停留后重置
            if (progress > 0f) {
                progress = 1.0f
                delay(1500)
                progress = 0f
            }
        }
    }

    AnimatedVisibility(
        visible = progress > 0f,
        enter = fadeIn() + expandVertically(),
    ) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MainColor3,
            trackColor = MainColor1,
        )
    }
}
