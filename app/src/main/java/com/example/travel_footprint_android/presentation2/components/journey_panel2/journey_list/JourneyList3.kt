/*
 * 文件名：JourneyList3.kt
 * 包路径：presentation2.components.journey_panel2.journey_list
 *
 * 【用途】
 * 本文件是"旅程面板"模块中的"旅程列表"可组合组件，旅行足迹地图应用的主要用户界面之一。
 * 该组件加载在可拖拽上下缩放的旅程面板（JourneyPanel）内部，展示用户已创建的所有旅程记录。
 *
 * 【功能】
 * 1. 顶部标题栏
 *    - 条件性显示"返回"按钮（选择旅程后可回到列表总览）
 *    - 固定标题文本"我的旅程"，同时作为拖拽手柄，通过 `detectVerticalDragGestures` 将拖拽事件
 *      向上传递给父级旅程面板，实现面板高度的拖拽缩放
 *    - 条件性显示"编辑"按钮（选择旅程后可直接编辑该旅程）
 *    - 旅程面板高度切换按钮 IcJourneyHeightButton（点击可在收起/展开两种面板高度间切换）
 * 2. 内容区域
 *    - 空状态：旅程列表为空时显示半透明白色背景的引导提示文本
 *    - 列表状态：委托给 JourneyListView4 组件渲染旅程卡片列表（LazyColumn + JourneyItem5）
 * 3. 右下角浮动操作区
 *    - 未选择旅程时：显示 IconAdd 添加按钮，带动画（淡入+缩放），点击后导航至旅程编辑页
 *    - 已选择旅程时：显示"前往足迹->"按钮，带动画，内部使用 BGImgBox 随机渲染背景图，
 *      点击后导航至足迹列表页
 * 4. 性能日志：组件重组结束时通过 Log.d("ComposeTime", ...) 输出本次渲染耗时
 *
 * 【关联组件】
 * - JourneyListView4     : 旅程卡片列表（LazyColumn），位于同包 journey_list_view 子包
 * - JourneyItem5         : 单个旅程卡片项，由 JourneyListView4 内部引用
 * - JourneyNavController : 单例导航控制器，管理 JourneyPanel2State 状态机和选中的旅程/足迹数据
 * - JourneyPanel2State   : 枚举，定义 JOURNEY_LIST / JOURNEY_EDIT / FOOTPRINT_LIST / FOOTPRINT_EDIT 四个页面状态
 * - IcJourneyHeightButton: 面板高度切换按钮，带 180 度旋转动画
 * - IconAdd / IconEdit   : 通用图标按钮组件
 * - Headline / TextMedium: 通用文本组件（标题/正文样式，使用自定义字体）
 * - BGBox / BGImgBox     : 通用背景容器组件（BGImgBox 支持从 drawable 列表中随机选取背景图）
 * - Journey              : Room 数据库实体（旅程表），定义 id/title/description/日期/封面/经纬度等字段
 * - SecondColor3         : 主题调色板中的副色（金色 #FFBF47），用于图标着色
 *
 * 【简单实现逻辑】
 * 1. 使用 Jetpack Compose 声明式 UI 编写，整个组件是一个 @Composable 函数
 * 2. 进入时记录 starTime 用于性能度量
 * 3. 顶部 Row 布局：左侧 animateContentSize 包装的返回按钮 Column（仅 journeySelected != null 显示）+ 中间 Headline 标题（weight=1f 撑满 + 拖拽手势）+ 右侧编辑按钮 Column（条件显示）+ IcJourneyHeightButton 切换按钮
 * 4. 标题上的 pointerInput 使用 detectVerticalDragGestures 捕获垂直拖拽，将 delta 通过 onDragDelta 回调传出，
 *    用于父组件计算面板高度；通过 setIsDragging 通知父组件拖拽开始/结束
 * 5. 内容区域使用 Box(fillMaxSize) 覆盖父容器剩余空间：
 *    - journeyList.isEmpty() 时显示空状态提示
 *    - 否则嵌入 JourneyListView4 渲染列表
 * 6. 右下角两个 Box（通过 BottomEnd 对齐），分别由 AnimatedVisibility 根据 journeySelected 是否为 null 控制显示/隐藏：
 *    - journeySelected == null → 显示 IconAdd
 *    - journeySelected != null → 显示"前往足迹->"按钮（内部嵌套 BGBox → BGImgBox → TextMedium）
 * 7. 导航使用 JourneyNavController.navigate() 改变状态，子组件通过订阅 journeyNavController 实现响应式导航
 */

package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.bg_box.BGBox
import com.example.travel_footprint_android.presentation2.components.bg_box.BGImgBox
import com.example.travel_footprint_android.presentation2.components.button.button_main.ButtonMain
import com.example.travel_footprint_android.presentation2.components.icon.icon_add.IconAdd
import com.example.travel_footprint_android.presentation2.components.icon.icon_edit.IconEdit
import com.example.travel_footprint_android.presentation2.components.journey_panel2.ic_journey_height_button.IcJourneyHeightButton
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_list_view.JourneyListView4
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State
import com.example.travel_footprint_android.presentation2.components.text.headline.Headline
import com.example.travel_footprint_android.presentation2.components.text.text_medium.TextMedium
import com.example.travel_footprint_android.ui.theme.SecondColor3

/**
 * 旅程列表面板组件
 *
 * @param journeyList              全部旅程数据列表，由父组件传入
 * @param updateJourney            更新单条旅程的回调，传递给子组件 JourneyListView4 → JourneyItem5
 * @param journeySelected          当前选中的旅程（null 表示处于列表总览模式，非 null 表示已选中某条旅程进入详情/操作模式）
 * @param journeyPanelHeightState  面板高度状态：true=展开，false=收起，传递给 IcJourneyHeightButton 控制旋转动画
 * @param setJourneyPanelHeightState 切换面板高度状态的 lambda，传递给 IcJourneyHeightButton 的 onClick
 * @param setIsDragging            通知父组件"正在拖拽"状态（用于锁定地图防止冲突），由 detectVerticalDragGestures 触发
 * @param onDragDelta              垂直拖拽位移量回调，父组件据此累加计算面板新高度
 */
@Composable
fun JourneyList3(
    journeyList: List<Journey>,
    updateJourney: (Journey) -> Unit,
    journeySelected: Journey?,
    journeyPanelHeightState: Boolean,
    setJourneyPanelHeightState: (Boolean) -> Unit,
    setIsDragging: (Boolean) -> Unit,
    onDragDelta: (Float) -> Unit,
    ) {
    // 记录进入组件时的时间戳，用于在末尾输出重组耗时日志
    val starTime = System.currentTimeMillis()

    // 整体采用 Column 纵向布局：顶部标题栏 + 下方内容区域
    Column{
        // ============================================================
        // 顶部标题栏：返回按钮 | 标题（可拖拽） | 编辑按钮 | 高度切换按钮
        // ============================================================
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 返回按钮区域：通过 animateContentSize 实现显示/隐藏时平滑尺寸过渡
            Column(
                modifier = Modifier.animateContentSize()
            ) {
                // 仅在选中某条旅程时显示返回按钮，点击回到旅程列表总览
                if(journeySelected != null) {
                    Image(
                        modifier = Modifier
                            .size(26.dp)
                            .padding(start = 5.dp)
                            .clickable(onClick = {
                                // 导航回 JOURNEY_LIST 状态，清空选中数据
                                JourneyNavController.navigate(JourneyPanel2State.JOURNEY_LIST, null)
                            }),
                        painter = painterResource(id = R.drawable.ic_left_long),
                        contentDescription = "返回图标",
                        colorFilter = ColorFilter.tint(SecondColor3),
                    )
                }
            }
            // 固定标题："我的旅程"，weight=1f 撑满剩余空间
            // 同时也是拖拽手柄：detectVerticalDragGestures 捕获垂直拖拽，将位移量通过 onDragDelta 上传给父组件
            Headline(
                text = "我的旅程",
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 5.dp, horizontal = 10.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { setIsDragging(true) },
                            onVerticalDrag = { _, dragAmount -> onDragDelta(dragAmount) },
                            onDragEnd = { setIsDragging(false) }
                        )
                    },
            )

            // 编辑按钮区域：通过 animateContentSize 实现显示/隐藏时平滑尺寸过渡
            Column(
                modifier = Modifier.animateContentSize()
            ) {
                // 仅在选中某条旅程时显示编辑图标，点击后导航至旅程编辑页并携带当前旅程数据
                if(journeySelected != null) {
                    IconEdit() {
                        JourneyNavController.navigate(JourneyPanel2State.JOURNEY_EDIT, journeySelected)
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            // 面板高度切换按钮：点击在展开/收起两种状态之间切换，带 180° 旋转动画
            IcJourneyHeightButton(journeyPanelHeightState, { setJourneyPanelHeightState(!journeyPanelHeightState) })

            Spacer(Modifier.width(10.dp))
        }

        // ============================================================
        // 内容区域：空状态提示 或 旅程卡片列表 + 右下角浮动操作按钮
        // ============================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // 根据旅程列表是否为空，显示不同内容
            if (journeyList.isEmpty()) {
                // 空状态：半透明白色背景 + 居中的引导提示文本
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp, 5.dp, 20.dp, 20.dp)
                        .background(Color.White.copy(.5f), RoundedCornerShape(5.dp))
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                    ) {
                        TextMedium(
                            modifier = Modifier
                                .align(Alignment.Center),
                            text = "目前还没有旅程内容哦~\n点击右下角添加按钮，新增你的足迹~",
                            fontSize = 15.sp,
                        )
                    }
                }
            } else {
                // 旅程列表：委托 JourneyListView4 用 LazyColumn 渲染 JourneyItem5 卡片列表
                JourneyListView4(journeyList = journeyList, journeySelected =  journeySelected, updateJourney =  updateJourney)
            }

            // 右下角浮动按钮 1：添加旅程按钮（仅未选择旅程时可见）
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Column {
                    // 通过 AnimatedVisibility 控制显示/隐藏，使用 fadeIn/Out + scaleIn/Out 组合动画
                    AnimatedVisibility(
                        visible = journeySelected == null,
                        enter = fadeIn() + scaleIn(initialScale = 0.8f),
                        exit = fadeOut() + scaleOut(targetScale = 0.8f)
                    ) {
                        IconAdd(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp),
                            clickable = {
                                // 导航至旅程新增/编辑页，journeyData 传 null 表示新建
                                JourneyNavController.navigate(JourneyPanel2State.JOURNEY_EDIT, null)
                            },
                        )
                    }
                }
            }

            // 右下角浮动按钮 2："前往足迹"按钮（仅在选中某条旅程时可见）
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Column {
                    // 与 IconAdd 使用相同的动画效果，通过 journeySelected != null 控制可见性
                    AnimatedVisibility(
                        visible = journeySelected != null,
                        enter = fadeIn() + scaleIn(initialScale = 0.8f),
                        exit = fadeOut() + scaleOut(targetScale = 0.8f)
                    ) {
                        // 使用 ButtonMain 作为可点击容器
                        ButtonMain(
                            onClick = {
                                // 导航至足迹列表页，携带当前选中的旅程数据
                                JourneyNavController.navigate(JourneyPanel2State.FOOTPRINT_LIST, journeyData = journeySelected)
                            },
                            paddingValues = PaddingValues(0.dp)
                        ) {
                            // BGBox 提供带阴影的圆角背景
                            BGBox {
                                // BGImgBox 从 bg_simple_hor_small_small 列表中随机选取背景图并叠加半透明白色蒙层
                                BGImgBox(
                                    listOf(R.drawable.bg_simple_hor_small_small),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(vertical = 5.dp, horizontal = 12.dp)
                                    ) {
                                        TextMedium("前往足迹->")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    // 输出本次重组耗时日志，用于性能调优
    Log.d("ComposeTime", "JourneyList3: ${System.currentTimeMillis() - starTime}")
}
