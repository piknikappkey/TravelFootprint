package com.example.travel_footprint_android.presentation2.components.image_square

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation2.components.image_square.add_icon.AddIcon
import com.example.travel_footprint_android.presentation2.components.image_square.delete_icon.DeleteIcon
import com.example.travel_footprint_android.presentation2.components.image_square.image1.Image1
import java.io.File

@Composable
fun ImageSquare2(
    imgPath: String,
    updateImgPath: (File) -> Unit,
    deleteImgPath: (String) -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 1f, // 宽高比
    addIconSize: Float = .3f, // “+”图标大小（相较于整个页面）
    elevation: Dp = 8.dp, // 阴影大小
    shape: RoundedCornerShape = RoundedCornerShape(16.dp), // 圆角
    delIconSize: Dp = 25.dp // “X”图标大小
) {
    val context = LocalContext.current

    /**
     * savedImageFile存储用户选择的（或数据库中存储的）图片数据，在ui层中显示
     */
    var savedImageFile by remember { mutableStateOf<File?>(null) }

    // 当imgPath更改时，切换图片
    LaunchedEffect(imgPath) {
        Log.d("ImageUpload", "imgPath: $imgPath")
        if (imgPath.isNotEmpty()) {
            val file = File(imgPath)
            savedImageFile = if (file.exists()) file else null
        }
    }

    // 选择图片
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) {
            Toast.makeText(context, "未选择图片", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        // 复制到内部存储，并将路径存储到数据库，返回 File 对象
        val file = copyToInternalStorage2(context, uri)
        if (file != null) {
            // 存储到数据库，并根据需要，显示/不显示该图片
            updateImgPath(file)
//            savedImageFile = updateImgPath(file)
            Log.d("ImageUpload", "文件路径: ${file.absolutePath}")
        } else {
            Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // 设置选中和未选中图片样式
            val modifierImg =  Modifier
                .fillMaxWidth()                // 宽度占满父容器
                .aspectRatio(aspectRatio)      // 宽高比
                .shadow(
                    elevation = elevation,                 // 阴影高度
                    shape = shape, // 圆角
                    clip = true                        // 同时按照该形状裁剪内容
                )
            // 显示图片
            if (savedImageFile != null) {
                // 图片
                Image1(modifierImg, savedImageFile!!)
                // 删除图标
                DeleteIcon(
                    Modifier.align(Alignment.TopEnd),
                    iconSize = delIconSize,
                    {
                        // 删除数据库路径
                        deleteImgPath(savedImageFile!!.absolutePath)
//                        savedImageFile = null
                    }
                )
            } else {
                // 添加图标
                AddIcon(
                    modifierImg,
                    iconSize = addIconSize,
                    { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) })
            }
        }
    }
}

/**
 * 将图片 URI 复制到应用内部存储，返回保存的 File
 */
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