package com.example.travel_footprint_android.presentation2.components.image_square

/**
 * ImageSquare2 - 方形图片选择与展示组件
 *
 * 【用途】
 *  - 提供一个可添加、展示、删除图片的方形区域组件
 *  - 支持从系统相册选取图片并复制到应用内部存储
 *  - 常用于旅程/足迹编辑页面中展示封面图片或照片列表
 *
 * 【功能】
 *  - 1. 图片展示：当 imgPath 有效且文件存在时，通过 Image1（Coil）加载显示图片
 *  - 2. 图片添加：当 imgPath 为空时，显示 IconAdd 按钮，点击触发系统相册选择器
 *  - 3. 图片删除：当 showDelIcon 为 true 时，右上角显示 DeleteIcon，点击删除当前图片
 *  - 4. 延迟加载：通过 lazy 参数控制是否延迟加载，避免大量图片同时加载造成卡顿
 *  - 5. 文件存储：选中的图片通过 copyToInternalStorage2 复制到应用 filesDir 目录
 *
 * 【关联组件】
 *  - Image1：基于 Coil 的图片加载组件，使用 rememberAsyncImagePainter 加载本地文件
 *  - IconAdd：添加图标按钮组件（"+"号），点击触发图片选择
 *  - DeleteIcon：删除图标按钮组件，位于右上角，用于移除当前图片
 *  - ImageLoadControl：全局单例对象，通过计数器 + 递增延时控制并发图片加载顺序
 *  - copyToInternalStorage2：工具函数，将 Uri 内容复制到内部存储
 *
 * 【简单实现逻辑】
 *  - 1. LaunchedEffect 在组件进入组合时检查 imgPath 是否有效，决定显示图片或添加按钮
 *  - 2. 延迟模式下通过 ImageLoadControl.loadImageStart() 获取递增延时，避免并发加载
 *  - 3. pickMedia 通过 ActivityResultContracts.PickVisualMedia 选择图片
 *  - 4. 选中后协程调用 copyToInternalStorage2 复制文件，通过 updateImgPath 回调通知外部
 *  - 5. 三态渲染：savedImageFile 有效 → 显示图片；正在延迟加载 → 显示灰色占位；其他 → 显示添加按钮
 */

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation2.components.icon.icon_add.IconAdd
import com.example.travel_footprint_android.presentation2.components.image_square.delete_icon.DeleteIcon
import com.example.travel_footprint_android.presentation2.components.image_square.image1.Image1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @param lazy 是否延迟加载图片。当为 true 时，先显示占位图，延迟一小段时间后再开始加载，
 *             用于避免大量图片同时加载造成的卡顿（如 Reminiscence 中的多张回忆图片）。
 */
// 方形图片组件：支持展示、添加（相册选取）、删除图片，可选延迟加载
@Composable
fun ImageSquare2(
    imgPath: String,
    updateImgPath: (File) -> File? = { file -> file},
    deleteImgPath: (String) -> Unit = { string -> },
    modifier: Modifier = Modifier,
    aspectRatio: Float = 1f,
    addIconSize: Float = .3f,
    elevation: Dp = 2.dp,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    delIconSize: Dp = 25.dp,
    showDelIcon: Boolean = false,
    lazy: Boolean = true,
) {
    val starTime = System.currentTimeMillis()

    val context = LocalContext.current

    // 保存当前有效的图片文件对象，null 表示无图片
    var savedImageFile by remember { mutableStateOf<File?>(null) }
    // 标记是否正在加载图片，非延迟模式且有路径时直接开始加载
    var isLoading by remember { mutableStateOf(!lazy && imgPath.isNotEmpty()) }

    // 延迟加载逻辑：根据 lazy 参数决定立即加载还是延时分批加载
    LaunchedEffect(lazy, imgPath) {
        if (!lazy) {
            // 非延迟模式，立即检查文件并加载
            if (imgPath.isNotEmpty()) {
                val exists = withContext(Dispatchers.IO) {
                    File(imgPath).exists()
                }
                savedImageFile = if (exists) File(imgPath) else null
                isLoading = false
            } else {
                savedImageFile = null
                isLoading = false
            }
        } else {
            // 延迟模式：先显示占位图，通过 ImageLoadControl 获取递增延时避免并发 IO
            isLoading = true
            if (imgPath.isNotEmpty()) {
                try {
                    delay(ImageLoadControl.loadImageStart())   // 计数器 +1，返回递增延时
                    val exists = withContext(Dispatchers.IO) { File(imgPath).exists() }
                    savedImageFile = if (exists) File(imgPath) else null
                } finally {
                    ImageLoadControl.loadImageOver()            // 加载完成后计数器 -1
                    isLoading = false
                }
            } else {
                savedImageFile = null
                isLoading = false
            }
        }
    }

    // 系统相册图片选择器启动器：使用 Photo Picker API 选择单张图片
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) {
            Toast.makeText(context, "未选择图片", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        // IO 协程中将选中图片复制到内部存储，回到主线程更新状态
        CoroutineScope(Dispatchers.IO).launch {
            val file = copyToInternalStorage2(context, uri)
            
            withContext(Dispatchers.Main) {
                if (file != null) {
                    savedImageFile = updateImgPath(file)
                    Log.d("ImageUpload", "文件路径: ${file.absolutePath}")
                } else {
                    Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(8.dp)
        ) {
            // 图片展示修饰符：全宽、等比例、阴影、白色背景
            val modifierImg = remember {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .shadow(
                        elevation = elevation,
                        shape = shape,
                        clip = true
                    )
                    .background(
                        color = Color(0xffffffff),
                    )
            }
            // 延迟加载模式且正在加载中时，显示灰色占位图
            val isLazyLoading = lazy && isLoading && imgPath.isNotEmpty()
            if (savedImageFile != null) {
                // 有图片：通过 Image1（Coil）加载显示，可选显示删除图标
                Image1(modifierImg, savedImageFile!!)
                if (showDelIcon) {
                    DeleteIcon(
                        Modifier.align(Alignment.TopEnd),
                        iconSize = delIconSize,
                        {
                            deleteImgPath(savedImageFile!!.absolutePath)
                            savedImageFile = null
                        }
                    )
                }
            } else if (isLazyLoading) {
                // 延迟加载中不显示内容，仅展示灰色占位背景
            } else {
                // 无图片：显示添加图标按钮，点击启动系统相册选择器
                IconAdd(
                    modifier = modifierImg,
                    iconSize = addIconSize,
                    clickable = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )
            }
        }
    }

    Log.d("ComposeTime", "ImageSquare2: ${System.currentTimeMillis() - starTime}")
}

// 将 Uri 对应的图片内容复制到应用内部存储目录，返回复制后的 File 对象
fun copyToInternalStorage2(context: Context, uri: Uri): File? {
    return try {
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val outputFile = File(context.filesDir, fileName)
        // 从 ContentResolver 读取输入流，写入到 filesDir 目录
        context.contentResolver.openInputStream(uri)?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        outputFile
    } catch (e: Exception) {
        Log.e("ImageUpload", "复制文件异常", e)
        null
    }
}

// 全局单例：控制图片并发加载顺序，通过计数器为每张图片分配递增的加载延时
object ImageLoadControl {
    // 当前正在加载或等待加载的图片数量
    var loadImage = 0

    // 开始加载一张图片，计数器 +1，返回递增的延迟毫秒数（每张间隔 200ms + 基础 300ms）
    fun loadImageStart(): Long {
        loadImage++
        return ((loadImage - 1) * 200 + 300).toLong()
    }

    // 图片加载完成，计数器 -1
    fun loadImageOver() {
        loadImage--
    }
}