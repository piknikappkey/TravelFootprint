package com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_list_view

/**
 * 旅程列表视图组件（第4版）
 *
 * 用途：
 * - 在旅程面板中以列表形式展示所有旅程，支持列表模式与详情模式的切换
 * - 属于旅程面板(journey_panel2) → 旅程列表(journey_list) → 列表视图(journey_list_view) 层级中的核心展示组件
 *
 * 功能：
 * - 使用 LazyColumn 高效渲染旅程列表，支持大量数据的懒加载
 * - 当未选中旅程(journeySelected == null)时，以紧凑列表模式展示所有旅程
 * - 当选中某个旅程时，仅展示该旅程的详情展开视图
 * - 每个旅程项点击后通过 JourneyNavController 导航到该旅程的详情/编辑页面
 * - 列表底部添加 70.dp 的底部间距 Spacer，避免内容被底部导航栏遮挡
 *
 * 关联组件：
 * - JourneyItem5: 旅程列表项组件，包含封面图片、标题、描述、日期、地点展示，支持展开/收起详情
 * - JourneyNavController(单例): 全局导航控制器，管理面板状态切换和选中的旅程/足迹数据
 * - JourneyPanel2State(枚举): 面板状态枚举，包含 JOURNEY_LIST / JOURNEY_EDIT / FOOTPRINT_LIST / FOOTPRINT_EDIT
 *
 * 实现逻辑：
 * - 接收 journeyList 作为数据源，通过 LazyColumn.items 遍历渲染
 * - 使用 key = { it.id } 为每个列表项指定唯一标识，优化列表重排性能
 * - items 内部通过条件判断(journeySelected == null || journeySelected.id == journey.id)决定是否渲染
 *   - 未选中时渲染全部旅程(列表模式)，选中时仅渲染匹配项(详情模式)
 * - 每个 JourneyItem5 传入 showDetail 参数，控制是否显示完整详情
 * - 点击旅程触发 JourneyNavController.navigate 跳转到对应旅程的详情面板
 * - 列表末尾的 Spacer(70.dp) 为底部操作栏提供避让空间
 */

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.journey_panel2.journey_list.journey_item.JourneyItem5
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyNavController
import com.example.travel_footprint_android.presentation2.components.journey_panel2.viewmodel.JourneyPanel2State

// 旅程列表视图：LazyColumn 渲染旅程列表，支持列表/详情双模式切换
@Composable
fun JourneyListView4(
    modifier: Modifier = Modifier, // 外部传入的 Modifier，用于控制列表布局位置
    journeyList: List<Journey>, // 所有旅程数据列表，作为 LazyColumn 的数据源
    journeySelected: Journey?, // 当前选中的旅程，null 表示列表模式(展示所有)，非 null 表示详情模式(仅展示选中项)
    updateJourney: (Journey) -> Unit, // 旅程更新回调，当子组件修改旅程数据时调用，同步到 ViewModel
) {
    val starTime = System.currentTimeMillis()

    // 懒加载列：高效渲染大量旅程项，key 使用旅程 id 保证列表项的唯一性
    LazyColumn(
        modifier = modifier
    ) {
        // 遍历旅程列表，未选中时展示全部，选中时仅展示匹配项
        items(journeyList, key = { it.id }) { journey ->
            // 条件决定渲染：未选中(列表模式)渲染全部；选中(详情模式)仅渲染匹配的那一项
            if(journeySelected == null || journeySelected.id == journey.id) {
                // 旅程列表项：点击跳转到详情面板，传递 showDetail 控制展开/收起
                JourneyItem5(
                    journey = journey,
                    journeyClick = { JourneyNavController.navigate(JourneyPanel2State.JOURNEY_LIST, journey) },
                    showDetail = (journeySelected?.id == journey.id),
                    updateJourney = updateJourney
                )
            }
        }
        // 底部间距 70.dp，避免列表最后一项被底部导航栏遮挡
        item {
            Spacer(Modifier.height(70.dp))
        }
    }
    Log.d("ComposeTime", "JourneyListView4: ${System.currentTimeMillis() - starTime}")
}