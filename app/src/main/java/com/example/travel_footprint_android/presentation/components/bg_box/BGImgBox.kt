/**
 * BGImgBox - 随机背景图片容器组件
 *
 * 用途：
 * - 为页面或卡片提供带随机背景图片的 Box 容器，用于装饰性背景展示
 * - 在旅程详情页（JourneyDetails）、旅程列表项（JourneyItem3/4/5）、足迹详情等组件中广泛使用
 * - 图片以 cover 模式居中裁剪填充容器，上方覆盖半透明白色遮罩层保持内容可读性
 * - 若未提供图片列表（或列表为空），则退化为普通 Box 容器
 *
 * 功能：
 * - 随机图片选择：从传入的图片资源 ID 列表中随机选取一张作为背景
 * - 异步图片加载：通过 Coil ImageLoader 异步加载 drawable 资源，避免阻塞主线程
 * - Canvas 绘制背景：使用 drawBehind 在内容后方绘制背景图片和半透明遮罩层
 * - Cover 缩放模式：图片按容器比例等比例缩放，以覆盖整个容器（类似 ImageScale.Crop）
 * - 性能日志：测量并输出组件渲染耗时，用于性能调试
 *
 * 关联组件：
 * - 无项目内自定义依赖，仅依赖 Coil 图片加载库（coil-kt:coil-compose:2.5.0）
 * - 作为基础容器组件，被 BGBox/BGColumn/BGRow 等上层布局组件以及各页面组件直接或间接使用
 *
 * 实现逻辑：
 * 1. 检查 imgList 是否为空：为空则直接返回普通 Box 包裹内容
 * 2. 获取屏幕尺寸（用于后续可能的缩放计算参考）和设备 Context
 * 3. 通过 remember + Random.nextInt 从列表中随机选取一张图片资源 ID
 * 4. 创建 Coil ImageLoader 实例（启用 crossfade 过渡效果）
 * 5. LaunchedEffect 监听 selectedResId 变化，异步加载图片为 ImageBitmap：
 *    - 构建 ImageRequest，设置 allowHardware(false) 确保可在 Canvas 上绘制
 *    - 加载成功后转换为 compose ImageBitmap 存入状态
 * 6. Box 容器中通过 drawBehind 自定义绘制：
 *    - 先绘制纯色背景（#FFFCF1EB）
 *    - 计算 cover 缩放比例并居中绘制背景图片
 *    - 最后绘制半透明白色遮罩层（drawRect），确保上层内容清晰可见
 * 7. 记录并输出渲染耗时日志
 */
package com.example.travel_footprint_android.presentation.components.bg_box

import android.graphics.Point
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun BGImgBox(
    imgList: List<Int>, // 背景图片资源 ID 列表，随机选取其中一张
    modifier: Modifier = Modifier, // 应用于外层 Box 的修饰符（在 drawBehind 之后）
    drawRectColor: Color = Color.White.copy(alpha = .3f), // 图片上方遮罩层颜色，默认 30% 透明白色
    contentAlignment: Alignment = Alignment.TopStart, // 内容在容器内的对齐方式
    alpha: Float = 1f,
    composable: @Composable () -> Unit, // 容器内的子组件内容
) {
    // 记录渲染开始时间，用于性能日志
    val starTime = System.currentTimeMillis()

    // 图片列表为空时 → 退化为普通 Box，不加载背景图片
    if (imgList.isEmpty()) {
        Box { composable() }
        return
    }

    // 获取当前 Context 和屏幕尺寸
    val context = LocalContext.current
    val screenSize = remember { getScreenSize(context) }
    
    // 随机选取一张图片资源 ID（remember 确保重组时不重新随机）
    val randIndex by remember { mutableStateOf(Random.nextInt(0, imgList.size)) }
    val selectedResId = imgList[randIndex]

    // 存储异步加载后的背景图片 Bitmap（null 时仅显示纯色背景）
    var bgBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    // 创建 Coil ImageLoader 实例，启用 crossfade 过渡效果
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }

    // 图片资源 ID 变化时 → 异步加载图片为 compose ImageBitmap
    LaunchedEffect(selectedResId) {
        val request = ImageRequest.Builder(context)
            .data(selectedResId) // 加载 drawable 资源
            .allowHardware(false) // 禁用硬件位图，Canvas 绘制需要软件位图
            .build()

        val result = imageLoader.execute(request)
        if (result is SuccessResult) {
            bgBitmap = result.drawable.toBitmap().asImageBitmap()
        }
    }

    // 外层容器：绘制背景色 → 背景图片 → 半透明遮罩 → 子组件内容
    Box(
        modifier = Modifier
            .background(Color(0xFFFCF1EB)) // 底色：浅粉色，图片未加载或为空时的兜底背景
            .drawBehind {
                bgBitmap?.let { bitmap ->
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val imageWidth = bitmap.width.toFloat()
                    val imageHeight = bitmap.height.toFloat()

                    // Cover 模式缩放：取宽高比例中较大者，确保图片完全覆盖容器
                    val scale = maxOf(canvasWidth / imageWidth, canvasHeight / imageHeight)
                    val scaledWidth = imageWidth * scale
                    val scaledHeight = imageHeight * scale

                    // 居中绘制缩放后的图片
                    drawImage(
                        image = bitmap,
                        dstOffset = IntOffset(
                            ((canvasWidth - scaledWidth) / 2).roundToInt(),
                            ((canvasHeight - scaledHeight) / 2).roundToInt()
                        ),
                        dstSize = IntSize(scaledWidth.roundToInt(), scaledHeight.roundToInt()),
                        alpha = alpha
                    )

                    // 半透明遮罩层：降低背景图片的视觉强度，确保上层文字/组件可读
                    drawRect(color = drawRectColor)
                }
            }
            .then(modifier), // 在自定义绘制之后应用外部传入的修饰符
        contentAlignment = contentAlignment
    ) {
        composable()
    }

    // 性能日志：输出组件渲染耗时
    Log.d("ComposeTime", "BGImgBox: ${System.currentTimeMillis() - starTime}")
}

// 获取设备屏幕尺寸（像素），用于辅助背景缩放计算
private fun getScreenSize(context: android.content.Context): Point {
    val displayMetrics = context.resources.displayMetrics
    return Point(displayMetrics.widthPixels, displayMetrics.heightPixels)
}
