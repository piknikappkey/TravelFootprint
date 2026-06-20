package com.example.travel_footprint_android.presentation.components.ai_indicator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.domain.service.AiOperationInfo
import com.example.travel_footprint_android.presentation.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation.components.bg_box.DraggableBox
import com.example.travel_footprint_android.presentation.components.button.button_border.ButtonBorder
import com.example.travel_footprint_android.presentation.components.line_between.LineBetween
import com.example.travel_footprint_android.presentation.components.text.headline.Headline
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.MainColor1
import com.example.travel_footprint_android.ui.theme.MainColor2
import com.example.travel_footprint_android.ui.theme.MainColor3

/**
 * AI 运行状态浮窗组件
 *
 * 当用户在旅程编辑页关闭 AI 助手弹窗但 AI 操作仍在后台运行时，
 * 在 JourneyScreen 上显示此浮窗。支持多个 AI 任务同时运行，
 * 浮窗中列出每个任务及其进度条和取消按钮。
 *
 * @param operations 当前正在运行的所有 AI 操作列表
 * @param onCancel 点击取消按钮的回调，参数为操作 ID
 */
@Composable
fun AiRunningIndicator(
    operations: List<AiOperationInfo>,
    onCancel: (String) -> Unit,
) {
    var outerWidth by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val estimatedWidth = with(density) { 200.dp.toPx() }
    var contentWidth by remember { mutableFloatStateOf(estimatedWidth) }

    val rightPadding = with(density) { 12.dp.toPx() }
    val initialX = if (outerWidth > 0f) {
        (outerWidth - contentWidth - rightPadding).coerceAtLeast(0f)
    } else {
        0f
    }

    DraggableBox(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(12.dp),
        initialOffsetX = initialX,
        initialOffsetY = with(density) { 12.dp.toPx() },
        enableSnap = true,
        onOuterSizeChanged = { width, _ ->
            outerWidth = width
        },
    ) {
        BGImgBox(
            R.drawable.bg_rectangular_1_2__1__0,
            modifier = Modifier
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .onSizeChanged { size ->
                        contentWidth = size.width.toFloat()
                    }
                    .fillMaxWidth(0.45f)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                // 标题行
                Headline(
                    text = "AI 任务",
                    fontSize = 13.sp,
                    letterSpacing = TextUnit.Unspecified,
                )
                Spacer(Modifier.heightIn(5.dp))

                // 每个任务
                operations.forEachIndexed { index, operation ->
                    if (index > 0) {
                        LineBetween()
                    }

                    TaskItem(
                        operation = operation,
                        onCancel = { onCancel(operation.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    operation: AiOperationInfo,
    onCancel: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        // 描述文本
        TextMedium(
            text = operation.description,
            fontSize = 12.sp,
            color = MainColor2,
        )

        // 进度条 + 取消按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MainColor3,
                trackColor = MainColor1,
            )
            Spacer(Modifier.width(2.dp))
            ButtonBorder(
                onClick = onCancel,
                paddingValues = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                TextMedium(
                    text = "取消",
                    fontSize = 12.sp,
                )
            }
        }
    }
}
