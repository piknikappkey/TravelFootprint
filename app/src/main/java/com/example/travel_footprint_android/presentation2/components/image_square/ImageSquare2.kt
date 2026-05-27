package com.example.travel_footprint_android.presentation2.components.image_square

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
import com.example.travel_footprint_android.presentation2.components.image_square.add_icon.AddIcon
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

    var savedImageFile by remember { mutableStateOf<File?>(null) }
    // 标记是否已开始加载（用于延迟加载模式）
    var isLoading by remember { mutableStateOf(!lazy && imgPath.isNotEmpty()) }

    // 延迟加载逻辑：等待一帧后再开始加载，避免首次展开详情时所有图片同时加载
    LaunchedEffect(lazy, imgPath) {
        if (!lazy) {
            // 非延迟模式，立即加载
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
            // 延迟模式：先显示占位图，延迟后开始加载
            isLoading = true
            if (imgPath.isNotEmpty()) {
                try {
                    delay(ImageLoadControl.loadImageStart())   // 计数器 +1，延迟加载
                    val exists = withContext(Dispatchers.IO) { File(imgPath).exists() }
                    savedImageFile = if (exists) File(imgPath) else null
                } finally {
                    ImageLoadControl.loadImageOver()            // 无论如何都 -1
                    isLoading = false
                }
            } else {
                savedImageFile = null
                isLoading = false
            }
        }
    }

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) {
            Toast.makeText(context, "未选择图片", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

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
                // 加载中不显示内容
            } else {
                AddIcon(
                    modifierImg,
                    iconSize = addIconSize,
                    { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) })
            }
        }
    }

    Log.d("ComposeTime", "ImageSquare2: ${System.currentTimeMillis() - starTime}")
}

fun copyToInternalStorage2(context: Context, uri: Uri): File? {
    return try {
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val outputFile = File(context.filesDir, fileName)
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

// 用于控制图片加载顺序，防止高并发io操作
object ImageLoadControl {
    var loadImage = 0

    fun loadImageStart(): Long {
        loadImage++
        return ((loadImage - 1) * 200 + 300).toLong()
    }

    fun loadImageOver() {
        loadImage--
    }
}