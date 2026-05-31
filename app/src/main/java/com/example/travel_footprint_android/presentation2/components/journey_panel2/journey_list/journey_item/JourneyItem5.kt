package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_item

/**
 * JourneyItem5 - 旅程列表卡片/详情展示组件（第五版）
 *
 * ====== 用途 ======
 * 本文件是旅程列表页的核心卡片组件，负责渲染单个旅程条目。
 * 通过 showDetail 参数在同一個组件中实现两种展示模式：
 *   - 列表模式（showDetail=false）：紧凑卡片，适用于旅程列表（JourneyList）
 *   - 详情模式（showDetail=true）：完整详情，适用于旅程详情页（JourneyDetails）
 *
 * ====== 主要功能 ======
 * 1. 双模式切换 —— 通过 showDetail 控制紧密列表布局与完整详情布局的切换。
 * 2. 列表模式 —— 显示封面缩略图（110dp）、标题、截断描述（14字）、地址、日期。
 * 3. 详情模式 —— 显示完整封面大图、完整描述、地址/日期详细信息、旅程回忆图片网格。
 * 4. 图片管理 —— 详情模式下封面图片可更换/删除（通过 ImageSquare2），
 *    回忆图片通过 Reminiscence 组件支持添加和删除。
 * 5. 性能优化 —— 使用 derivedStateOf 缓存截断描述和日期格式化结果，
 *    使用 remember 缓存日期格式化器和地址分割结果，避免重组时重复计算。
 * 6. 随机背景 —— 外层使用 BGImgBox 随机装饰背景，提供视觉区分。
 * 7. 点击响应 —— 列表模式下整张卡片可点击，通过 journeyClick 回调跳转详情。
 *
 * ====== 关联组件 ======
 * - BGImgBox（/bg_box/BGImgBox.kt）：随机背景图片容器，提供装饰性背景。
 * - ImageSquare2（/image_square/ImageSquare2.kt）：方形图片组件，
 *   列表模式用于封面缩略图，详情模式用于完整封面（可更换/删除）。
 * - Reminiscence（/journey_details/reminiscence/Reminiscence.kt）：
 *   旅程回忆图片网格，详情模式下展示所有回忆照片。
 * - TextMedium / TextSmall（/text/）：自定义字体文本组件，用于标题、描述、地址等。
 * - LineBetween（/line_between/LineBetween.kt）：虚线分隔线。
 * - Journey（/data/entity/Journey.kt）：Room 实体类，携带所有旅程数据。
 *
 * ====== 简单实现逻辑 ======
 * 1. 主组件 JourneyItem5 负责外层容器（阴影、边距）、BGImgBox 背景、
 *    左右两栏布局（左侧封面缩略图 + 右侧 RightContent），
 *    以及列表模式下的点击事件。
 * 2. 私有组件 RightContent 负责右侧所有内容区的渲染，通过 showDetail
 *    分支控制不同模式下显示的内容：
 *    - 标题行（居中/居左）
 *    - 详情模式：封面大图 + 分隔线 + "旅程描述："标签 + 完整描述 + 地址日期 + 回忆网格
 *    - 列表模式：截断描述 + 位置/日期行
 * 3. 使用 derivedStateOf 将截断描述、日期字符串、截断位置的计算
 *    变为惰性求值，仅在依赖的输入（description / startDate / location）变化时重新计算。
 * 4. 使用 remember 缓存 SimpleDateFormat 实例和地址分割结果，防止每次重组创建新对象。
 */

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.image_square.ImageSquare2
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_details.reminiscence.Reminiscence
import com.example.travel_footprint_android.presentation2.components.journey_panel2.line_between.LineBetween
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation2.components.text.text_small.TextSmall
import com.example.travel_footprint_android.ui.theme.FontDark3
import com.example.travel_footprint_android.ui.theme.FontDark4
import com.example.travel_footprint_android.ui.theme.FontDark5
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 主组件：渲染旅程列表卡片，支持列表/详情双模式
// @param journey 旅程数据实体
// @param journeyClick 列表模式下点击卡片的跳转回调
// @param showDetail true=详情模式，false=列表模式
// @param updateJourney 更新旅程数据的回调
@Composable
fun JourneyItem5(
    journey: Journey,
    journeyClick: () -> Unit = {},
    showDetail: Boolean,
    updateJourney: (Journey) -> Unit,
) {
    // 记录渲染开始时间，用于性能日志分析
    val starTime = System.currentTimeMillis()

    // 外层卡片容器：全宽、阴影、圆角、垂直外边距
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // 列表模式水平边距更大（25dp），详情模式较紧凑（10dp）
            .padding(vertical = 5.dp, horizontal = if (showDetail) 10.dp else 25.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(3.dp),
                clip = true
            )
    ) {
        // 随机背景图片列表：两种矩形背景装饰图
        val bgList = remember { listOf(R.drawable.bg_rectangular_1__3__1, R.drawable.bg_rectangular_1__3__2) }
        // 装饰性随机背景容器
        BGImgBox(
            bgList,
        ) {
            // 列表模式整行可点击；详情模式禁用点击
            val modifier = if (showDetail) Modifier else Modifier.clickable(onClick = journeyClick)
            // 横向布局：左侧封面缩略图 + 右侧内容区
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 封面缩略图区域：仅列表模式显示（animateContentSize 做展开/收起动画）
                Row(
                    modifier = Modifier
                        .animateContentSize(
//                            animationSpec = tween(durationMillis = 300)
                        )
                ) {
                    // 列表模式：显示 110dp 宽的封面缩略图
                    if (!showDetail) {
                        Spacer(Modifier.width(3.dp))
                        ImageSquare2(
                            imgPath = journey.coverImagePath,
                            modifier = Modifier.width(110.dp),
                            aspectRatio = 1.2f,
                            addIconSize = .3f,
                            shape = RoundedCornerShape(5.dp),
                        )
                    }
                }
                // 右侧内容区（标题、描述、日期、地址等）
                RightContent(journey, showDetail, updateJourney)
            }
        }
    }
    // 详情模式下在卡片底部增加额外间距
    if(showDetail) {
        Spacer(Modifier.padding(5.dp))
    }

    // 性能日志：输出组件渲染耗时
    Log.d("ComposeTime", "JourneyItem5: ${System.currentTimeMillis() - starTime}")
}

// 右侧内容区私有组件：集中处理旅程卡片的文字信息展示
// 包括标题、封面大图、描述、地址、日期、回忆图片网格等
// 通过 showDetail 分支控制列表模式与详情模式的显示差异
@Composable
private fun RightContent(
    journey: Journey,
    showDetail: Boolean,
    updateJourney: (Journey) -> Unit,
) {
    // 1. 缓存日期格式化器，避免每次重组都创建新实例
    val fullDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val yearFormat = remember { SimpleDateFormat("yyyy", Locale.getDefault()) }
    val shortDateFormat = remember { SimpleDateFormat("MM-dd", Locale.getDefault()) }
    val currentYear = remember { yearFormat.format(Date()) }

    // 2. 缓存地址分割结果，避免重复 split 操作
    // journey.address 格式为 "区域\n地点"，通过换行符分割得到区域和具体地点
    val addressParts = remember(journey.address) { journey.address.split("\n") }
    val region = addressParts.firstOrNull() ?: ""
    val location = addressParts.lastOrNull() ?: ""

    // 右侧内容区：垂直排列标题、描述、地址日期、回忆图片
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 5.dp)
            .animateContentSize()
    ) {
        // --- 标题行 ---
        // 详情模式居中显示（大号字体，深色），列表模式居左（中号字体，中深色）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if(showDetail) Arrangement.Center else Arrangement.Start
        ) {
            TextMedium(
                text = journey.title,
                fontSize = if(showDetail) 20.sp else 15.sp,
                color = if(showDetail) FontDark3 else FontDark4,
            )
        }

        Spacer(Modifier.padding(1.dp))

        // --- 详情模式：封面大图区（可更换/删除） + 分隔线 ---
        if(showDetail) {
            Column {
                // 封面大图：宽高比 1.2:1，支持点击更换和删除
                ImageSquare2(
                    imgPath = journey.coverImagePath,
                    updateImgPath = { file ->
                        journey.coverImagePath = file.absolutePath
                        updateJourney(journey)
                        file
                    },
                    deleteImgPath = { imgPath ->
                        journey.coverImagePath = ""
                        updateJourney(journey)
                    },
                    modifier = Modifier.padding(horizontal = 30.dp),
                    aspectRatio = 1.2f,
                    addIconSize = .3f
                )
                // 封面图下方的虚线分隔线
                LineBetween(paddingUp = 12.dp)
            }
        }

        // --- 描述区 ---
        // 使用 derivedStateOf 缓存截断描述，仅当 description 实际变化时才重新计算，
        // 避免 showDetail 切换时无效执行字符串操作
        val truncatedDesc by remember {
            derivedStateOf {
                val desc = journey.description
                if (desc.length > 14) desc.substring(0, 14) + "... ..." else desc
            }
        }

        // 详情模式显示"旅程描述："标签
        if(showDetail) {
            Column {
                TextMedium(
                    text = "旅程描述：",
                    firstLine = 0,
                    modifier = Modifier.padding(horizontal = 15.dp),
                    fontSize = 17.sp
                )
                Spacer(Modifier.padding(2.dp))
            }
        }

        // 描述正文：详情模式显示完整内容，列表模式显示截断内容（最多14字 + "..."）
        // 详情模式左右边距较大，首行缩进1个字符
        TextSmall(
            modifier = Modifier.padding(start = if(showDetail) 15.dp else 2.dp, end = if(showDetail) 15.dp else 0.dp),
            text = if(showDetail) journey.description else truncatedDesc,
            color = if(showDetail) FontDark4 else FontDark5,
            firstLine = 1,
            fontSize = if(showDetail) 16.sp else 14.sp,
            minLines = if(showDetail) 1 else 2,
            maxLines = if(showDetail) Int.MAX_VALUE else 2,
        )

        Spacer(Modifier.padding(2.dp))

        // --- 地址与日期区 ---
        // 详情模式：显示完整日期 + 地点/区域信息（左右两栏布局），底部有分隔线
        if(showDetail) {
            Column {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    val dateStr = fullDateFormat.format(journey.startDate)
                    Spacer(Modifier.width(10.dp))
                    // 左侧：旅程开始日期
                    TextSmall(
                        text = dateStr,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .padding(0.dp)
                            .offset(y = 5.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    // 右侧：具体地点 + 所属区域（两行）
                    Column {
                        TextSmall(
                            text = location,
                            firstLine = 0,
                            modifier = Modifier.padding(horizontal = 15.dp)
                        )
                        TextSmall(
                            text = region,
                            firstLine = 2,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 15.dp),
                        )
                    }
                }
                // 分隔线
                LineBetween()
            }
        }
        // 列表模式：简化的日期 + 地点（一行两端对齐），截断长地点
        if(!showDetail) {
            // 使用 derivedStateOf 缓存日期字符串计算，仅当 startDate 或 currentYear 变化时才重新计算
            val dateStr by remember {
                derivedStateOf {
                    val startYear = yearFormat.format(journey.startDate)
                    // 今年内显示 "MM-dd"，跨年显示 "yyyy-MM-dd"
                    if (currentYear == startYear) {
                        shortDateFormat.format(journey.startDate)
                    } else {
                        fullDateFormat.format(journey.startDate)
                    }
                }
            }
            // 位置信息（字数不能太多，超过10字截断加"..."）
            val loc by remember {
                derivedStateOf {
                    val l = location
                    if (l.length > 10) l.substring(0, 10) + "..." else l
                }
            }

            // 一行布局：左地点 / 右日期
            Row {
                Spacer(Modifier.width(10.dp))
                TextSmall(
                    text = loc
                )
                Spacer(Modifier.weight(1f))
                TextSmall(
                    text = dateStr,
                )
                Spacer(Modifier.width(5.dp))
            }
        }

        // --- 详情模式：旅程回忆图片网格 ---
        if(showDetail) {
            Column {
                // "旅程回忆"标题
                TextMedium(
                    text = "旅程回忆",
                    firstLine = 0,
                    modifier = Modifier.padding(horizontal = 15.dp),
                    fontSize = 17.sp
                )
                Spacer(Modifier.padding(5.dp))
                // 回忆图片网格（FlowRow 流式布局，支持添加/删除）
                Reminiscence(
                    journey = journey,
                    updateJourney = updateJourney
                )
            }
        }
    }
}