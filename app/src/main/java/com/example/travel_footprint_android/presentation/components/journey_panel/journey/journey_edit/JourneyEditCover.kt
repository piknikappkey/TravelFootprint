package com.example.travel_footprint_android.presentation.components.journey_panel.journey.journey_edit

/**
 * 旅程编辑封面组件
 *
 * 用途：
 * - 在旅程编辑页面中提供封面图片的编辑区域
 * - 属于旅程面板(journey_panel2) → 旅程编辑(journey_edit) → 封面(cover) 层级中的表单组件
 *
 * 功能：
 * - 展示"封面："文字标签，提示用户当前编辑的是封面区域
 * - 提供一个方形图片编辑区域，支持封面图片的添加、展示、删除
 * - 图片编辑区域宽高比为 1.2:1（横版矩形），适合封面展示
 * - 始终显示删除图标，方便用户移除已设置的封面
 *
 * 关联组件：
 * - Journey(Room 实体): 包含 coverImagePath 字段，存储封面图片路径
 * - ImageSquare2: 方形图片编辑组件，支持从相册选取图片、展示图片、删除图片
 * - TextMedium: 自定义文本组件，使用 FFRuanMengChuLianTi 字体，支持首行缩进
 *
 * 实现逻辑：
 * - 使用 TextMedium 显示"封面："标签，左侧留 15.dp 边距
 * - 使用 Spacer 在标签和图片之间保留 2.dp 间距
 * - 通过 journey.coverImagePath 获取当前封面图片路径传给 ImageSquare2
 * - updateImgPath / deleteImgPath 由外部传入，将图片变更同步到 ViewModel 或数据库
 * - ImageSquare2 配置 aspectRatio=1.2f(横版)、addIconSize=0.3f(按钮比例)、showDelIcon=true(显示删除)
 */

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation.components.image_square.ImageSquare
import com.example.travel_footprint_android.presentation.components.text.text_medium.TextMedium
import java.io.File

// 旅程编辑封面组件：组合标签文字与方形图片编辑器，支持封面的展示/添加/删除
@Composable
fun JourneyEditCover(
    journey: Journey, // 当前编辑的旅程实体，从中读取 coverImagePath 作为封面图片路径
    updateImgPath: (File) -> File?, // 图片更新回调：将新选取的图片文件同步到 ViewModel/数据库，返回处理后的 File
    deleteImgPath: (String) -> Unit, // 图片删除回调：传入被删除图片的路径，同步移除数据库中的引用
) {
    // 显示"封面："标签，15.dp 左侧边距，无首行缩进
    TextMedium(
        text = "封面：",
        firstLine = 0,
        modifier = Modifier.padding(horizontal = 15.dp)
    )
    // 标签与图片编辑器之间的 2.dp 间距
    Spacer(Modifier.padding(2.dp))
    // 方形图片编辑器：展示/添加/删除封面图片，横版比例 1.2:1，显示删除按钮
    ImageSquare(
        imgPath = journey.coverImagePath,
        updateImgPath = updateImgPath,
        deleteImgPath = deleteImgPath,
        modifier = Modifier.padding(horizontal = 40.dp),
        aspectRatio = 1.2f,
        addIconSize = .3f,
        showDelIcon = true,
    )
}