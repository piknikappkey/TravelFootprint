package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation.components.button.button_main.ButtonMain
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation.components.text.text_small.TextSmall
import com.example.travel_footprint_android.ui.theme.FontDark5
import com.example.travel_footprint_android.ui.theme.FontDark6
import com.example.travel_footprint_android.ui.theme.MainColor1
import com.example.travel_footprint_android.ui.theme.MainColor3

/**
 * AI 操作按钮的通用基础组件
 *
 * 封装了 AI 按钮的通用结构：标签 + 提示图标 + 操作按钮 + 提示说明 + 进度条 + 脚注。
 * 差异部分通过参数传入，避免多个 AI 按钮之间的代码重复。
 *
 * @param label 左侧标签文字，如 "AI 封面涂鸦："
 * @param buttonIcon 按钮图标
 * @param buttonText 按钮文字，如 "开始涂鸦"
 * @param loadingText 加载中文字，如 "涂鸦中..."
 * @param tipText 提示说明文字
 * @param footerText 底部脚注文字，如 "注：由豆包(doubao-seedream)生成~"
 * @param isLoading 是否正在加载
 * @param autoShowTipOnClick 点击按钮后是否自动展开提示区
 * @param onButtonClick 按钮点击回调
 * @param extraContent 额外内容插槽，插入在按钮行和提示区之间
 */
@Composable
fun AiActionButtonBase(
    label: String,
    buttonIcon: ImageVector,
    buttonText: String,
    loadingText: String,
    tipText: String,
    footerText: String,
    isLoading: Boolean,
    autoShowTipOnClick: Boolean = false,
    onButtonClick: () -> Unit,
    extraContent: @Composable () -> Unit = {},
) {
    var showTip by remember { mutableStateOf(false) }

    Column {
        // 按钮所在的行布局
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧标签文字（点击可切换提示显示）
            TextMedium(
                text = label,
                color = MainColor3,
                fontSize = 16.sp,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { showTip = !showTip }
            )
            Image(
                modifier = Modifier
                    .size(10.dp)
                    .offset(x = (-8).dp, y = (-8).dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { showTip = !showTip },
                painter = painterResource(id = R.drawable.ic_tip),
                contentDescription = "提示图标",
                colorFilter = ColorFilter.tint(FontDark6),
            )
        }

        // 提示说明和进度条
        AiTipSection(
            showTip = showTip,
            tipText = tipText,
            isLoading = isLoading
        )

        // 额外内容（如风格选择区）
        extraContent()

        Row {
            Spacer(Modifier.weight(1f))
            // AI 操作按钮
            ButtonMain(
                onClick = {
                    if (!isLoading) {
                        onButtonClick()
                        if (autoShowTipOnClick) {
                            showTip = true
                        }
                    }
                },
                bgColor = if (isLoading) MainColor1 else MainColor3,
                paddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                AiButtonContent(
                    icon = buttonIcon,
                    buttonText = buttonText,
                    loadingText = loadingText,
                    isLoading = isLoading
                )
            }
            Spacer(Modifier.width(10.dp))
        }
        Spacer(Modifier.height(5.dp))

        // 脚注
        AiFooterRow(
            footerText = footerText,
            showTip = showTip
        )
    }
}

/**
 * 按钮内部内容组件
 *
 * 根据加载状态切换显示：加载中显示进度动画，空闲时显示图标和文字。
 *
 * @param icon 按钮图标
 * @param buttonText 按钮文字
 * @param loadingText 加载中文字
 * @param isLoading 是否正在加载
 */
@Composable
private fun AiButtonContent(
    icon: ImageVector,
    buttonText: String,
    loadingText: String,
    isLoading: Boolean,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(6.dp))
            TextMedium(
                text = loadingText,
                color = Color.White,
                fontSize = 14.sp,
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            TextMedium(
                text = buttonText,
                color = Color.White,
                fontSize = 14.sp,
            )
        }
    }
}

/**
 * 提示说明区域组件
 *
 * 点击提示图标后淡入显示说明文字和进度条。
 *
 * @param showTip 是否显示提示
 * @param tipText 提示说明文字
 * @param isLoading 是否正在加载（控制进度条状态）
 */
@Composable
private fun AiTipSection(
    showTip: Boolean,
    tipText: String,
    isLoading: Boolean,
) {
    AnimatedVisibility(
        visible = showTip,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column {
            TextSmall(
                text = tipText,
                fontSize = 13.sp,
                color = FontDark5,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 4.dp),
                firstLine = 2
            )
            AiProgressBar(
                isActive = isLoading,
                modifier = Modifier
                    .padding(horizontal = 15.dp, vertical = 4.dp),
            )
        }
    }
}

/**
 * 底部脚注行组件
 *
 * 显示 "注：由 xxx 生成~" 的脚注信息。
 *
 * @param footerText 脚注文字
 * @param showTip 是否已展开提示（控制脚注颜色）
 */
@Composable
private fun AiFooterRow(
    footerText: String,
    showTip: Boolean,
) {
    Row {
        Spacer(Modifier.weight(1f))
        TextSmall(
            text = footerText,
            fontSize = 11.sp,
            color = if (showTip) FontDark5 else FontDark6
        )
        Spacer(Modifier.width(5.dp))
    }
}
