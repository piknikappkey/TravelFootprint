package com.example.travel_footprint_android.presentation.components.milestone.card

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation.components.milestone.Milestone
import com.example.travel_footprint_android.presentation.components.text.headline.Headline
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import kotlinx.coroutines.delay

/**
 * MilestoneCard - 里程碑成就卡片
 *
 * 展示单条里程碑成就，包含图标、名称、描述和进度条
 * - 已解锁：蓝色背景、奖杯图标、完整进度条
 * - 未解锁：灰色背景、锁图标、部分进度条
 */

val MilestoneCardLoadTime = 100

object MilestoneCardControl {
    // 当前正在加载或等待加载的图片数量
    var loadMilestoneCard = 0

    // 开始加载一张图片，计数器 +1，返回递增的延迟毫秒数（每张间隔 100ms）
    fun loadMilestoneCardStart(): Long {
        loadMilestoneCard++
        return ((loadMilestoneCard - 1) * MilestoneCardLoadTime).toLong()
    }

    // 图片加载完成，计数器 -1
    fun loadMilestoneCardOver() {
        loadMilestoneCard--
    }
}

/**
 * 单条里程碑成就卡片
 *
 * @param milestone 里程碑成就数据
 * @param isAchieved 是否已解锁
 * @param progress 当前进度（0.0 - 1.0）
 */
@Composable
internal fun MilestoneCard(
    milestone: Milestone,
    isAchieved: Boolean,
    progress: Float
) {
    // 已解锁用浅蓝色背景，未解锁用浅灰色背景
    val bgColor = if (isAchieved) Color(0xFFEFF6FF) else Color(0xFFF3F4F6)

    var show by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            delay(MilestoneCardControl.loadMilestoneCardStart())
            show = true
            delay(MilestoneCardLoadTime.toLong())
        } finally {
            MilestoneCardControl.loadMilestoneCardOver()
        }
    }
    Box {
        Column(
            modifier = Modifier
                .width(90.dp)
        ) {  }
            Row(
                modifier = Modifier.animateContentSize(
                    animationSpec = tween(
                        durationMillis = 200,
                    ),
                )
            ) {

            if(!show) return@Row
            Column(
                modifier = Modifier
                    .width(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 圆形图标容器：已解锁 → 蓝色底 + 白色奖杯；未解锁 → 灰色底 + 灰色锁
                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .clip(CircleShape)
                        .background(if (isAchieved) Color(0xFF3B82F6) else Color(0xFFD1D5DB)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAchieved) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "成就",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "未解锁",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(Modifier.height(5.dp))

                // 成就名称：已解锁深色文字，未解锁灰色文字
                Headline(
                    text = milestone.name,
                    fontSize = 13.sp,
                    color = if (isAchieved) Color(0xFF1F2937) else Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(3.dp))

                // 成就描述：已解锁可见灰色，未解锁极浅灰（近乎隐藏）
                TextMedium(
                    text = milestone.description,
                    fontSize = 10.sp,
                    color = if (isAchieved) Color(0xFF6B7280) else Color(0xFFE5E7EB),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(5.dp))

                // 进度条：灰色背景轨道
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFE5E7EB))
                ) {
                    // 进度条填充：已解锁 100% 蓝色；未解锁按 progress 比例浅蓝色
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (isAchieved) 1f else progress)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (isAchieved) Color(0xFF3B82F6)
                                else Color(0xFF93C5FD)
                            )
                    )
                }
            }
        }
    }
}
