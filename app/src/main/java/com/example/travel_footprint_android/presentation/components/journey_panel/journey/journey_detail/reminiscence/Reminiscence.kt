/**
 * 旅程回忆图片网格组件
 * 
 * 用途：
 * - 在旅程详情/编辑页面中展示旅程的回忆照片列表
 * - 支持展示已有照片、添加新照片、删除照片
 * 
 * 功能：
 * 1. 图片网格展示：使用 FlowRow 流式布局，自动换行排列照片
 * 2. 已有照片展示：遍历 journey.journeyImagePaths，每个路径对应一个 ImageSquare2 展示
 * 3. 添加新照片：网格末尾固定一个空位 ImageSquare2（imgPath=""），点击后从相册选取并添加到列表
 * 4. 删除照片：当 showDelIcon=true 时，每个图片右上角显示删除按钮，点击后从列表移除
 * 5. 延迟加载优化：所有 ImageSquare2 启用 lazy=true，避免大量图片同时加载造成卡顿
 * 
 * 关联组件：
 * - Journey: 旅程实体，包含 journeyImagePaths（回忆图片路径列表）
 * - ImageSquare2: 方形图片组件，支持展示/添加/删除图片，支持延迟加载（lazy 参数）
 *   - 展示模式：imgPath 有效时显示图片
 *   - 添加模式：imgPath 为空时显示"+"按钮，点击启动系统相册选择器
 *   - 删除功能：showDelIcon=true 时右上角显示删除图标
 *   - 延迟加载：lazy=true 时通过 ImageLoadControl 分批次加载避免卡顿
 * 
 * 实现逻辑：
 * - FlowRow 作为流式布局容器，horizontalArrangement=0.dp（无水平间距），verticalArrangement=4.dp（垂直行间距）
 * - 遍历 journeyImagePaths 创建展示型 ImageSquare2，删除时通过 filter 过滤列表并 updateJourney
 * - 末尾固定一个添加型 ImageSquare2（imgPath=""），选取图片后通过 += 追加到列表
 * - 所有 ImageSquare2 尺寸固定 80dp，统一样式参数（aspectRatio=1f, elevation=2.dp, shape=5.dp圆角）
 * 
 * @param journey 当前旅程实体，包含回忆图片路径列表
 * @param updateJourney 更新旅程数据的回调，用于添加/删除图片后同步状态
 * @param showDelIcon 是否显示删除图标，默认 false
 */
package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_detail.reminiscence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.image_square.ImageSquare2

// 旅程回忆图片网格：FlowRow 流式布局展示图片列表，支持添加和删除
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Reminiscence(
    journey: Journey,
    updateJourney: (Journey) -> Unit,
    showDelIcon: Boolean = false, // 是否显示删除图标
) {
    // 统一样式参数
    val imgSize = 80.dp           // 图片方块尺寸
    val aspectRatio = 1f          // 宽高比 1:1
    val iconSize = .4f            // 添加图标占容器比例
    val elevation = 2.dp          // 阴影高度
    val shape = RoundedCornerShape(5.dp)  // 圆角形状
    val delIconSize = 15.dp       // 删除图标尺寸

    // 外层 Box：全宽容器，内部 FlowRow 居中
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // FlowRow 流式布局：自动换行排列图片，最小宽度 295dp
        FlowRow(
            modifier = Modifier
                .widthIn(min = 295.dp)
                .align(Alignment.Center)
                .padding(horizontal = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),  // 水平无间距
            verticalArrangement = Arrangement.spacedBy(4.dp)     // 垂直行间距 4dp
        ) {
            // 遍历已有图片路径，为每个图片创建展示型 ImageSquare2
            journey.journeyImagePaths.forEachIndexed { i, imgPath ->
                Box(
                    modifier = Modifier
                        .height(imgSize)
                        .width(imgSize)
                ) {
                    ImageSquare2(
                        imgPath = imgPath,
                        updateImgPath = { file -> file }, // 展示模式无需更新路径
                        deleteImgPath = { path ->
                            // 从列表中移除该图片路径
                            val newList = journey.journeyImagePaths - path
                            updateJourney(journey.copy(journeyImagePaths = newList))
                        },
                        aspectRatio = aspectRatio,
                        addIconSize = iconSize,
                        elevation = elevation,
                        shape = shape,
                        delIconSize = delIconSize,
                        showDelIcon = showDelIcon,
                        lazy = true, // 启用延迟加载优化性能
                    )
                }
            }

            // 添加新图片的占位方块：imgPath="" 触发添加模式
            Box(
                modifier = Modifier
                    .height(imgSize)
                    .width(imgSize)
            ) {
                ImageSquare2(
                    imgPath = "",
                    updateImgPath = { file ->
                        // 将新图片路径追加到列表
                        journey.journeyImagePaths += file.absolutePath
                        updateJourney(journey)
                        null
                    },
                    deleteImgPath = { imgPath -> }, // 添加模式无需删除回调
                    aspectRatio = aspectRatio,
                    addIconSize = iconSize,
                    elevation = elevation,
                    shape = shape,
                    delIconSize = delIconSize
                )
            }
        }
    }
}