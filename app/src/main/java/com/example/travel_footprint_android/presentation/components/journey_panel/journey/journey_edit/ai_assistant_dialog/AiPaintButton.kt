package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components.AiActionButtonBase
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components.CustomPromptInput
import com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit.ai_assistant_dialog.components.StyleChip
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.FontDark4
import com.example.travel_footprint_android.ui.theme.MainColor3

/**
 * 涂鸦风格选项
 *
 * 每种风格对应一个显示名称和一段独立的完整 AI 提示词。
 * 用户每次只能选择一种风格。
 *
 * @param label 显示在 UI 上的名称
 */
enum class PaintStyle(val label: String) {
    HAND_DRAWN("手绘"),
    CRAYON("蜡笔"),
    DREAMY("梦幻"),
    DYNAMIC("灵动"),
    DOODLE("涂鸦"),
    WATERCOLOR("水彩"),
    PIXEL("像素")
}

/**
 * 各风格的独有完整提示词
 *
 * 每种风格拥有独立完整的 AI 提示词，各自突出该风格的独特质感和画面特点。
 */
private val paintStyleFullPrompts: Map<PaintStyle, String> = mapOf(
    PaintStyle.HAND_DRAWN to (
        "把上传照片转化成温暖的彩铅手绘插画风格。整体呈现出彩铅在素描纸上自然涂画的质感，线条有轻微抖动和断续，像稚拙又灵动的手绘习作。上色方式模仿儿童彩铅涂色，保留不均匀的叠色痕迹和纸面留白，笔触方向随性自然。如果画面中存在人物，将该人物变成萌系二次元风格，画风可爱，表情天真治愈。背景点缀手绘小元素：爱心、星星、棉花糖云朵、太阳笑脸、小花、小动物爪印等。色彩以暖色为主——粉色系、橙黄色、浅紫色搭配薄荷绿点缀。整体氛围：温暖治愈，天真烂漫，手账风。不要精致商业插画感，不要写实渲染，不要干净工整的线稿，保留手绘的自然拙劣感和温度感，图片中禁止出现文字内容"
    ),
    PaintStyle.CRAYON to (
        "把上传照片转化成蜡笔画风格，模仿蜡笔在略微粗糙的纸面上用力涂画的效果。笔触粗犷厚重，有明显的蜡笔颗粒质感和叠色纹路，边缘不整齐像被磨碎的粉蜡痕迹。上色方式饱满有力，像儿童用蜡笔来回用力涂抹的感觉，色彩覆盖力强且带有蜡质光泽。如果画面中存在人物，将该人物简化成萌系卡通风格，表情夸张傻气，线条用粗蜡笔勾边。背景搭配简单的蜡笔涂鸦元素：波浪线、螺旋圈、小星星、棒棒糖、彩色气球。色彩以高饱和色为主——大红、明黄、翠绿、宝蓝、亮紫。整体氛围：童趣天真，热烈奔放，如幼儿园画作般自由大胆。不要精致细腻，不要柔和过渡，不要写实风格，图片中禁止出现文字内容"
    ),
    PaintStyle.DREAMY to (
        "把上传照片转化成梦幻浅水雾风格的插画。整体笼罩在细微的柔和朦胧的光影中，像透过薄磨砂玻璃或清晨薄纱看到的景象，轮廓轻微虚化柔和。线条若隐若现不强调清晰边缘，画面有自然的光晕弥散效果，仿佛梦境中的回忆画面。色彩以低饱和马卡龙色系为主——淡粉、薰衣草紫、天空蓝、奶油黄、薄荷绿，色彩之间有自然的晕染交融。背景飘浮着发光的小星星、晶莹水珠、羽毛、肥皂泡等梦幻飘浮元素。如果画面中存在人物，将所有人物都处理成柔光环绕发丝和衣摆的梦境精灵。整体氛围：浪漫唯美，梦境般空灵，少女心。不要锐利线条，不要高对比，不要现实感，图片中禁止出现文字内容"
    ),
    PaintStyle.DYNAMIC to (
        "把上传照片转化成充满动感和生命力的灵动风格插画。线条流畅富有节奏感，像一笔画成的速写，有明显的笔势走向和飞白留白，充满速度感。色彩明亮跳跃，运用对比色搭配制造视觉冲击——粉红配电光蓝、橙黄配紫罗兰。如果画面中有人物，将画面中所有人物处理成运动风动画小人，表情元气满满，但绝对不能改变人物位置、大小。如果画面中没有任何人物，则绝对禁止在画面中绘画人物。背景加入流动的曲线轨迹、音符形状、闪光星芒、速度线等动感装饰元素。色彩以高饱和亮色为主——亮粉红、荧光黄、天空蓝、活力橙、电光紫。整体氛围：活力四射，元气满满，青春跃动感。不要静态呆板，不要沉闷色调，不要拘谨工整，图片中禁止出现文字内容"
    ),
    PaintStyle.DOODLE to (
        "把上传照片转化成自由随性的涂鸦风格插画。模仿马克笔和彩铅笔在纸面上随意涂画的效果，线条粗细不一忽轻忽重，有重叠、交叉和反复勾画。上色不拘一格，颜色涂出边界、留白跳色是常态，故意保留「画错」「画歪」的手工痕迹。背景密集填充各种随性手绘涂鸦符号：箭头、对话框、波浪线、闪电、emoji表情、涂鸦字母、波点、斜条纹、小怪兽。如果画面中存在人物，将人物变成萌系可爱二次元小人。色彩鲜明并任意组合——荧光粉、柠檬黄、电光蓝、荧光绿。整体氛围：随性可爱，儿童玩闹感，拒绝规整。不要正经插画感，不要精致打磨，不要有束缚感，图片中禁止出现文字内容"
    ),
    PaintStyle.WATERCOLOR to (
        "把上传照片转化成清透的水彩画风格。色彩在水分的带动下自然晕染扩散，形成不规则的渐变和意想不到的色彩交融效果。画面有明显的湿画法痕迹——颜色在纸面上自然流淌、相互渗透，边缘有淡淡的水渍沉淀线。层次通过色彩的透明叠加实现，浅色打底深色点缀，高光和白色部分保留纸面留白而非涂白。如果画面中存在人物，将该人物处理成清新的水彩风插画，面容柔和，肤色清透。背景点缀淡淡的水彩花朵晕染、飘散的颜料点滴、轻盈的透明水珠。色彩以清透色系为主——浅粉、天蓝、嫩绿、淡紫、柠檬黄。整体氛围：清新文艺，恬静雅致，如手绘水彩明信片般治愈。不要厚重颜料覆盖，不要数码合成感，图片中禁止出现文字内容"
    ),
    PaintStyle.PIXEL to (
        "把上传照片转化成复古像素游戏风格的像素画。画面由分明的方形像素块组成，边缘呈现锯齿状阶梯效果，模仿经典8-bit/16-bit游戏画面。色彩使用有限色板，每个颜色有明确的明暗过渡层次，通过像素块密集排列来表现细节。如果画面中有人物，将占画面较大部分内容（大于30%）的人物处理成经典RPG游戏角色的Q版像素形象——大头小身体、方块手，人物大小与画面人物大小严格一致。背景加入像素游戏经典元素：像素云朵、草丛、砖块地面、金币、红蓝宝石、问号方块、食人花。色彩复古明亮——经典红蓝黄绿紫，参考早期任天堂游戏机的配色感觉。整体氛围：怀旧游戏感，复古科技风，充满童趣和 nostalgia。不要平滑线条，不要渐变过渡，不要高分辨率精致感，图片中禁止出现文字内容"
    )
)

/**
 * 构建单个风格的完整提示词
 *
 * @param selectedStyle 用户选中的风格（null 时使用手绘风格提示词作为默认）
 * @return 完整的 AI 提示词
 */
fun buildPaintPrompt(selectedStyle: PaintStyle?): String {
    return selectedStyle?.let { paintStyleFullPrompts[it] }
        ?: paintStyleFullPrompts.getValue(PaintStyle.HAND_DRAWN)
}

/**
 * AI 封面涂鸦按钮组件
 *
 * 包含：
 * 1. 自定义提示词输入区域（可输入自定义要求）
 * 2. 风格单选标签区域（每次只能选择一种风格）
 * 3. 涂鸦按钮（加载中时显示进度动画并禁用）
 * 4. 提示说明和进度条
 *
 * @param isLoading 是否正在加载
 * @param onPaintWithPrompt 点击回调，传入组合后的提示词
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiPaintButton(
    isLoading: Boolean,
    onPaintWithPrompt: (String) -> Unit,
) {
    // 默认选中「手绘」风格
    var selectedStyle by remember { mutableStateOf<PaintStyle?>(PaintStyle.HAND_DRAWN) }
    // 用户自定义提示词
    var customPrompt by remember { mutableStateOf("") }

    AiActionButtonBase(
        label = "AI 封面涂鸦：",
        buttonIcon = Icons.Default.Brush,
        buttonText = "开始涂鸦",
        loadingText = "涂鸦中...",
        tipText = "选择一种喜欢的风格，点击「开始涂鸦」生成效果。生成时间较长，请耐心等待 3-5 分钟。",
        footerText = "注：由豆包(doubao-seedream)生成~",
        isLoading = isLoading,
        estimatedLoadTimeMs  = 100_000L,
        onButtonClick = {
            val prompt = if (customPrompt.isNotBlank()) {
                "把上传照片按提示词要求转化，使用以下提示词：${customPrompt}"
            } else {
                buildPaintPrompt(selectedStyle)
            }
            onPaintWithPrompt(prompt)
        },
        extraContent = {
            Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 4.dp)) {
                TextMedium(
                    text = "预设提示词：",
                    color = if (customPrompt.isNotEmpty()) FontDark4 else MainColor3,
                    fontSize = 14.sp,
                )
                // 风格单选标签区域
                FieldSelectionFlow(
                    isLoading = isLoading,
                    selectedStyle = selectedStyle,
                    onSelectionChange = { sel -> selectedStyle = sel }
                )

                // 自定义提示词输入区域
                CustomPromptInput(
                    customPrompt = customPrompt,
                    onCustomPromptChange = { customPrompt = it }
                )
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FieldSelectionFlow(
    isLoading: Boolean,
    selectedStyle: PaintStyle?,
    onSelectionChange: (PaintStyle?) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        FlowRow(
            modifier = Modifier
                .widthIn(min = 100.dp)
                .padding(horizontal = 8.dp, vertical = 0.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            PaintStyle.entries.forEach { style ->
                val isSelected = style == selectedStyle
                StyleChip(
                    label = style.label,
                    isSelected = isSelected,
                    onClick = {
                        // Toggle Chip 逻辑：点击已选中的取消选中（回退到默认），点击未选中的设为选中
                        onSelectionChange(if (isSelected) null else style)
                    },
                    enabled = !isLoading
                )
            }
        }
    }
}
