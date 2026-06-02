/*
 * 文件名：Image.kt
 * 包路径：presentation2.components.image_square.image1
 *
 * 【用途】
 * 本地文件图片显示可组合组件。将设备本地存储中的图片文件（File）异步加载并渲染到 Compose UI 中，
 * 是项目中展示用户拍摄/选择的照片的基础组件。
 *
 * 【功能】
 * 1. 异步图片加载：通过 Coil 库的 `rememberAsyncImagePainter` 从本地 File 路径异步加载图片，
 *    避免主线程阻塞
 * 2. 硬件加速：启用 `allowHardware(true)` 让 Coil 使用硬件 Bitmap 渲染，提升大图加载性能
 * 3. 尺寸控制：以 1080x1080 的固定尺寸请求解码，使用 `Scale.FILL` 策略按比例缩放填充目标尺寸
 * 4. 裁剪显示：通过 `ContentScale.Crop` 将加载后的图片缩放裁剪以填充满整个容器区域，
 *    超出部分被裁剪，保证显示区域被完整覆盖
 * 5. 错误处理：加载失败时通过 `onError` 回调输出 Log.e 日志，便于调试排查
 *
 * 【关联组件】
 * - Coil 库（io.coil-kt:coil-compose:2.5.0）：异步图片加载框架，项目依赖在 app/build.gradle.kts 中声明
 * - present2.components.image_square 包：同包下可能包含其他图片相关组件（如 AddIcon 等）
 *
 * 【简单实现逻辑】
 * 1. 通过 `LocalContext.current` 获取当前 Composable 上下文
 * 2. 使用 `rememberAsyncImagePainter` 创建 Coil 异步图片 painter：
 *    - 构建 ImageRequest，设定数据源为传入的 File、目标尺寸 1080x1080、FILL 缩放策略、启用硬件加速
 *    - onError 回调中记录加载失败日志
 * 3. 将 painter 传入 Compose 原生 `Image` 组件，设置 ContentScale.Crop 按裁剪模式显示
 *
 * 【被引用位置】
 * - 项目中所有需要从本地文件显示图片的场景（如旅程封面、足迹图片等）
 */

package com.example.travel_footprint_android.presentation.components.image_square.custom_image

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import java.io.File

/**
 * 本地文件图片加载组件：使用 Coil 异步加载本地 File 并显示
 *
 * @param modifier       外部修饰符，用于控制 Image 组件的位置、大小、边距等
 * @param savedImageFile 本地存储的图片文件对象，Coil 直接从此 File 读取数据
 */
@Composable
fun CustomImage(
    modifier: Modifier = Modifier,
    savedImageFile: File,
) {
    // 获取当前组合上下文（Context），供 Coil 构建 ImageRequest 使用
    val context = LocalContext.current

    // 使用 Coil 的 rememberAsyncImagePainter 创建异步加载 painter：
    // - 从 savedImageFile 读取图片数据
    // - 解码尺寸限制为 1080x1080，节省内存
    // - Scale.FILL 使图片按比例缩放填满目标尺寸
    // - allowHardware(true) 启用硬件 Bitmap 加速渲染
    // - onError 捕获加载失败并输出错误日志
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(savedImageFile)
            .size(Size(1080, 1080))
            .scale(coil.size.Scale.FILL)  // 按比例缩放
            .allowHardware(true)
            .build(),
        onError = { error ->
            Log.e("ImageUpload", "Coil 加载失败: ${error.result.throwable}")
        }
    )

    // 使用 Compose 原生 Image 组件渲染 painter，ContentScale.Crop 使图片裁剪填满容器
    Image(
        painter = painter,
        contentDescription = "选择的图片",
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}
