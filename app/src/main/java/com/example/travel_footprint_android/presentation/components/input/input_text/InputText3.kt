package com.example.travel_footprint_android.presentation.components.input.input_text

/*
 * ============================================================================
 * InputText3.kt — 可复用文本输入组件（第3版）
 * ============================================================================
 *
 * 【用途】
 *   提供带背景图、图标前缀、聚焦态视觉反馈的通用文本输入框组件。
 *   被 JourneyEdit、FootprintEdit 等编辑表单页面引用，用于标题和描述的输入。
 *
 * 【功能】
 *   1. 聚焦视觉反馈：输入框聚焦时边框变粗（1dp→2dp）且颜色从半透明变为主色完整色
 *   2. 背景渲染：通过 BGImgBox 为输入区域添加背景图片纹理
 *   3. 图标前缀：左侧显示可自定义的 Icon（默认为 Edit 图标），颜色半透明主色
 *   4. 占位提示：输入为空时显示 tipText 占位文字（颜色半透明），输入内容后隐藏
 *   5. 字数限制：通过 maxLength 参数限制最大输入字符数（默认 Int.MAX_VALUE）
 *   6. 多行支持：BasicTextField 设为单行模式关闭，支持多行文本输入
 *   7. 高度可定制：padding/contentPadding/textStyle/颜色/图标均可通过参数配置
 *
 * 【关联组件】
 *   - BGImgBox：背景图片容器，提供 Canvas 绘制的背景纹理 + 半透明白色遮罩
 *   - BasicTextField（Compose Foundation）：底层文本输入框，自定义光标和装饰
 *   - FFRuanMengChuLianTi：软萌初恋体 FontFamily（资源字体 R.font.ruan_meng_chu_lian_ti）
 *   - MainColor2（#9F79FA）：主色调紫色，用于聚焦态光标颜色
 *   - SecondColor2（#FDD583）：副色调金色，用于聚焦态边框主色（primaryColor 默认值）
 *   - SecondColor4（#FFAB00）：副色调琥珀色，用于默认文字颜色（textStyle 默认 color）
 *
 * 【简单实现逻辑】
 *   1. isFocused 状态跟踪输入框是否获得焦点
 *   2. 根据 isFocused 动态计算 borderColor（聚焦时用完整主色，否则半透明）和 borderWidth
 *   3. 外层 Box 应用圆角裁剪 + 动态边框，内嵌 BGImgBox 作为背景层
 *   4. InputContent 内部子组件负责实际的输入交互：
 *      - Row 水平布局：Icon + Spacer + BasicTextField
 *      - BasicTextField 绑定 value/onValueChange，maxLength 超长截断
 *      - onFocusChanged 回调更新 isFocused 实现边框视觉切换
 *      - decorationBox 在 value 为空时渲染 tipText 占位文字
 * ============================================================================
 */

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.presentation.components.bg_box.BGImgBox
import com.example.travel_footprint_android.ui.theme.FFRuanMengChuLianTi
import com.example.travel_footprint_android.ui.theme.MainColor2
import com.example.travel_footprint_android.ui.theme.SecondColor2
import com.example.travel_footprint_android.ui.theme.SecondColor4


@Composable
fun InputText3(
    // 当前输入文本值
    value: String,
    // 文本变化回调，超长时自动截断至 maxLength
    onValueChange: (String) -> Unit,
    // 输入为空时显示的占位提示文字
    tipText: String,
    // 最大输入字符数限制，默认无限制
    maxLength: Int = Int.MAX_VALUE,
    // 外层容器的内边距
    padding: PaddingValues = PaddingValues(horizontal = 24.dp),
    // 内容区域（BGImgBox 内部）的内边距
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    // 文本样式，默认使用软萌初恋体 17sp 琥珀色
    textStyle: TextStyle = TextStyle(
        color = SecondColor4,
        fontSize = 17.sp,
        fontFamily = FFRuanMengChuLianTi
    ),
    // 聚焦时的边框和光标主色（默认金色）
    primaryColor: Color = SecondColor2,
    // 未聚焦时的边框颜色（默认紫色）
    onSurfaceColor: Color = MainColor2,
    // 左侧前缀图标，默认为 Material Edit 图标
    imageVector: ImageVector = Icons.Default.Edit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    // 跟踪输入框是否获得焦点，用于切换边框样式
    var isFocused by remember { mutableStateOf(false) }

    // 动态边框颜色：聚焦时完整主色，否则半透明
    val borderColor = if (isFocused) primaryColor else onSurfaceColor.copy(.6f)
    // 动态边框宽度：聚焦时 2dp 加粗，否则 1dp 细线
    val borderWidth = if (isFocused) 2.dp else 1.dp

    // 外层容器：圆角裁剪 + 动态边框
    Box(
        modifier = modifier
            .padding(padding)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        // 背景图片容器，为输入框提供纹理背景
        BGImgBox(
            R.drawable.bg_simple_hor_small_small,
        ) {
            Box(
                modifier = Modifier
                    .padding(contentPadding)
            ) {
                InputContent(
                    value,
                    onValueChange,
                    tipText,
                    maxLength,
                    textStyle,
                    primaryColor,
                    onSurfaceColor,
                    imageVector,
                    // 将焦点状态变化传递给外层 isFocused
                    { bool ->
                        isFocused = bool
                    }
                )
            }
        }
    }
}

// 输入内容的实际交互层：Icon + BasicTextField 的水平排列，处理焦点、字数限制和占位提示
@Composable
fun InputContent(
    // 当前输入文本值
    value: String,
    // 文本变化回调（已含字数截断逻辑）
    onValueChange: (String) -> Unit,
    // 占位提示文字
    tipText: String,
    // 最大字符数
    maxLength: Int,
    // 文本样式
    textStyle: TextStyle,
    // 主色（光标和图标颜色）
    primaryColor: Color,
    // 表面色（占位文字颜色）
    onSurfaceColor: Color,
    // 左侧图标
    imageVector: ImageVector,
    // 焦点变化回调，用于联动外层的边框视觉切换
    setIsFocused: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,   // 居中对齐
    ) {
        // 左侧可自定义图标，半透明主色
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = primaryColor.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // 底层文本输入框
        BasicTextField(
            value = value,
            // 输入变化时进行字数截断：超出 maxLength 的部分直接抛弃
            onValueChange = { newText ->
                if (newText.length > maxLength) onValueChange(newText.take(maxLength))
                else onValueChange(newText)
            },
            singleLine = false,              // ← 允许多行
            textStyle = textStyle,
            cursorBrush = SolidColor(primaryColor),
            modifier = Modifier
                .weight(1f)
                // 焦点变化时通知外层，驱动边框样式切换
                .onFocusChanged { focusState ->
                    setIsFocused(focusState.isFocused)
                },
            // 自定义装饰：输入为空时显示占位文字（半透明），内容存在时占位文字被 innerTextField 覆盖
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = tipText,
                        style = textStyle,
                        color = onSurfaceColor.copy(alpha = 0.7f)
                    )
                }
                innerTextField()
            }
        )
    }
}