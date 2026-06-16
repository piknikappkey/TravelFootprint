/*
 * FootprintListItem - 足迹列表项组件
 *
 * 【用途】
 *  - 在旅程展开详情中，作为单个足迹的卡片列表项展示
 *  - 显示足迹的标题、描述、创建时间、地址等基本信息
 *  - 支持点击展开/折叠，展开后显示编辑按钮和足迹状态记录面板
 *
 * 【功能】
 *  1. 卡片展示：使用 BGImgBox 随机背景图片容器包装，带有阴影圆角卡片效果
 *  2. 点击交互：点击空白区域折叠展开状态（footprintClick(null)），点击返回按钮关闭（footprintClick(-1)）
 *  3. 展开态动画：返回按钮和编辑图标通过 AnimatedVisibility 平滑出现/消失（水平展开/收缩 + 淡入淡出）
 *  4. 描述截断：未展开时描述超过20字截断显示"... ..."，展开后显示完整描述
 *  5. 编辑导航：展开后显示编辑图标，点击通过 JourneyNavController 导航到足迹编辑页面
 *  6. 底部信息：足迹创建日期（固定显示）+ 地址（展开后显示区域，折叠时仅显示详细位置）
 *  7. 状态面板：展开后显示 FootprintListPanel，用于记录运动数据（持续时间、距离、速度、卡路里）
 *  8. 内容尺寸动画：Column 和 Row 使用 animateContentSize 使展开/折叠时高度变化平滑
 *
 * 【关联组件】
 *  - BGImgBox：提供随机背景图片的卡片容器组件
 *  - FootprintListPanel：足迹状态记录面板（开始/暂停/停止计时，运动数据记录）
 *  - IconEdit：编辑图标组件，点击导航到足迹编辑页面
 *  - JourneyNavController / JourneyPanel2State：页面导航控制器，管理旅程面板的各子页面状态
 *  - TextMedium / TextSmall：自定义中等/小号文本组件
 *  - Footprint / Journey：数据实体，分别代表足迹和旅程
 *
 * 【简单实现逻辑】
 *  1. FootprintListItem 作为最外层，使用 Box + BGImgBox 提供带随机背景的卡片容器
 *  2. Content 组合 Column 布局，按顺序排列：HeadRow → Description → (展开时) FootprintListPanel → BottomContent
 *  3. HeadRow：左端显示返回按钮（仅展开时可见），中间显示足迹标题，右端显示编辑图标（仅展开时可见）
 *  4. Description：根据 isClicked 决定展示完整描述或截断描述
 *  5. BottomContent：底部显示创建日期（左）和地址信息（右），地址中的区域部分仅展开时显示
 *  6. 所有的展开/收起动画通过 AnimatedVisibility + expandHorizontally/shrinkHorizontally 实现
 */
package com.example.travel_footprint_android.presentation.components.journey_panel.footprint.footprint_list

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation.components.icon.icon_edit.IconEdit
import com.example.travel_footprint_android.presentation.components.journey_panel.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation.components.line_between.LineBetween
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.presentation.components.text.text_small.TextSmall
import com.example.travel_footprint_android.ui.theme.FontDark4
import com.example.travel_footprint_android.ui.theme.MainColor2
import com.example.travel_footprint_android.ui.theme.SecondColor3
import java.text.SimpleDateFormat
import java.util.Locale

// 动画持续时间：展开/收起动画的基础时长（400毫秒）
private const val ANIMATION_DURATION = 400

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FootprintListItem(
    footprint: Footprint,
    footprintClick: (Int?) -> Unit,
    isClicked: Boolean,
    journeySelected: Journey,
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit,
    isRecording: Boolean = false,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 5.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(3.dp),
                clip = true
            )
            .clickable(onClick = { footprintClick(null) }) // 点击空白区域折叠详情
    ) {
        // 带随机背景图片的容器，从4张矩形背景图中随机选一张
        BGImgBox(
            R.drawable.bg_rectangular_2__1__0, R.drawable.bg_rectangular_2__1__1, R.drawable.bg_rectangular_2__1__2, R.drawable.bg_rectangular_2__1__3,
        ) {
            Content(footprint, footprintClick, isClicked, journeySelected, onPanelNavigate, isRecording)
        }
        // 右上角"记录中..."标签：仅在未展开且正在录制时显示
        if (isRecording && !isClicked) {
            TextSmall(
                text = "记录中...",
                fontSize = 12.sp,
                color = MainColor2,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 8.dp),
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Content(
    footprint: Footprint,
    footprintClick: (Int?) -> Unit,
    isClicked: Boolean,
    journeySelected: Journey,
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit,
    isRecording: Boolean = false,
) {

    // 按顺序垂直排列各子模块，animateContentSize 使展开/折叠时高度变化平滑过渡
    Column(
        modifier = Modifier.animateContentSize()
    ) {
        Spacer(Modifier.height(10.dp))
        HeadRow(footprint, footprintClick, isClicked, journeySelected, onPanelNavigate, isRecording)
        Spacer(Modifier.height(10.dp))
        // 足迹描述
        Description(footprint, isClicked)
        // 仅展开时显示足迹状态面板（运动数据记录）
        if(isClicked) {
            LineBetween()
            FootprintListPanel(footprint)
        } else {
            Spacer(Modifier.height(10.dp))
        }
        // 底部信息（足迹创建时间、足迹开始地址）
        BottomContent(footprint, isClicked)
        Spacer(Modifier.height(5.dp))
    }
}

@Composable
fun HeadRow(
    footprint: Footprint,
    footprintClick: (Int?) -> Unit,
    isClicked: Boolean,
    journeySelected: Journey,
    onPanelNavigate: (JourneyPanel2State, Journey?, Footprint?) -> Unit,
    isRecording: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 固定左侧间距，使按钮出现/消失时标题位置平滑过渡
        Spacer(Modifier.width(5.dp))
        // 返回按钮：仅展开时可见，带水平展开/收缩 + 淡入淡出动画
        AnimatedVisibility(
            visible = isClicked,
            enter = expandHorizontally(animationSpec = tween(ANIMATION_DURATION)) + fadeIn(animationSpec = tween(ANIMATION_DURATION)),
            exit = shrinkHorizontally(animationSpec = tween(ANIMATION_DURATION)) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
        ) {
            Image(
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = { footprintClick(-1) }), // -1 表示返回上一级
                painter = painterResource(id = R.drawable.ic_left2),
                contentDescription = "返回图标",
                colorFilter = ColorFilter.tint(SecondColor3), // 使用主题色着色
            )
        }
        // 足迹标题文本（始终显示）
        TextMedium(text = footprint.title)
        Spacer(Modifier.weight(1f))
        // 编辑图标：仅展开时可见，点击导航到足迹编辑页面
        AnimatedVisibility(
            visible = isClicked,
            enter = expandHorizontally(animationSpec = tween(ANIMATION_DURATION)) + fadeIn(animationSpec = tween(ANIMATION_DURATION)),
            exit = shrinkHorizontally(animationSpec = tween(ANIMATION_DURATION)) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
        ) {
            IconEdit(
                modifier = Modifier.size(16.dp)
            ) {
                onPanelNavigate(JourneyPanel2State.FOOTPRINT_EDIT, journeySelected, footprint)
            }
        }
        Spacer(Modifier.width(10.dp))
    }
}

// 足迹描述：展开时显示完整描述，折叠时超过20字截断加"... ..."
@Composable
fun Description(
    footprint: Footprint,   // 当前足迹数据，获取描述文本
    isClicked: Boolean,     // 是否展开，决定展示完整/截断描述
) {
    // 根据展开状态决定显示的描述文本
    val description = if (isClicked) {
        footprint.description                               // 展开时显示完整描述
    } else {
        if (footprint.description.length > 20) {
            footprint.description.take(20) + "... ..."      // 折叠时超过20字截断
        } else {
            footprint.description                           // 未超过20字直接显示
        }
    }
    // 使用中等文本组件展示，首行缩进2字符，深灰色字体
    TextMedium(
        text = description,
        firstLine = 2,
        modifier = Modifier.padding(horizontal = 15.dp),
        fontSize = 17.sp,
        color = FontDark4
    )
}

// 底部信息：左侧创建时间 + 右侧地址（地址区域部分仅展开时可见）
@Composable
fun BottomContent(
    footprint: Footprint,   // 当前足迹数据
    isClicked: Boolean,     // 是否展开
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.animateContentSize() // 使行高变化平滑，日期文本跟随移动
    ) {
        TimeView(footprint)                         // 左侧：足迹创建时间
        Spacer(Modifier.weight(1f))                 // 弹性间距
        Location(footprint, isClicked)              // 右侧：地址信息
    }
}

// 时间展示组件：将足迹的 createTime 格式化为 "yyyy-MM-dd" 并显示
@Composable
fun TimeView(
    footprint: Footprint,   // 当前足迹数据，从中获取创建时间
) {
    // 格式化日期为年-月-日
    val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dateStr = fullDateFormat.format(footprint.createTime)
    // 使用小号文本组件展示日期，浅色字体
    TextSmall(
        text = dateStr,
        fontSize = 11.sp,
        modifier = Modifier.padding(start = 10.dp)
    )
}

// 地址展示组件：左侧详细地址始终显示，右侧区域地址仅展开时显示
@Composable
fun Location(
    footprint: Footprint,   // 当前足迹数据，从中解析地址
    isClicked: Boolean,     // 是否展开
) {
    // 地址格式约定：第一行为区域（如"北京市"），最后一行为详细地址（如"天安门广场"）
    val region = footprint.address.split("\n")[0]
    val location = footprint.address.split("\n").last()

    Column(
        modifier = Modifier.animateContentSize() // 展开/折叠时高度变化平滑
    ) {
        // 详细地址：始终显示
        TextSmall(
            text = location,
            firstLine = 0,
            modifier = Modifier.padding(end = 10.dp)
        )
        // 区域地址：仅展开时显示，带水平展开/收缩 + 淡入淡出动画
        AnimatedVisibility(
            visible = isClicked,
            enter = expandHorizontally(animationSpec = tween(ANIMATION_DURATION)) + fadeIn(animationSpec = tween(ANIMATION_DURATION)),
            exit = shrinkHorizontally(animationSpec = tween(ANIMATION_DURATION)) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
        ) {
            TextSmall(
                text = region,
                firstLine = 2,
                fontSize = 11.sp,
                modifier = Modifier.padding(end = 10.dp),
            )
        }
    }
}