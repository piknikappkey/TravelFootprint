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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.presentation2.components.image_square.add_icon.AddIcon
import com.example.travel_footprint_android.presentation2.components.image_square.delete_icon.DeleteIcon
import com.example.travel_footprint_android.presentation2.components.image_square.image1.Image1
import java.io.File

@Composable
fun ImageSquare(
    journey: Journey,
    updateJourney: (Journey) -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 1f,
    iconSize: Float = .3f,
) {
    val context = LocalContext.current

    /**
     * savedImageFile存储用户选择的（或数据库中存储的）图片数据，在ui层中显示
     */
    var savedImageFile by remember { mutableStateOf<File?>(null) }

    // 初始化savedImageFile，如果该旅程有封面图，则直接使用
    LaunchedEffect(journey) {
        Log.d("ImageUpload", "journeyCoverPath: ${journey.coverImagePath}")
        if (journey.coverImagePath.isNotEmpty()) {
            val file = File(journey.coverImagePath)
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
        val file = copyToInternalStorage(context, uri)
        if (file != null) {
            // 存储到数据库
            journey.coverImagePath = file.absolutePath
            updateJourney(journey)
            // 显示到页面上
            savedImageFile = file
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
                .padding(horizontal = 10.dp)
        ) {
            // 设置选中和未选中图片样式
            val modifierImg =  Modifier
                .fillMaxWidth()                // 宽度占满父容器
                .aspectRatio(aspectRatio)      // 宽高比
                .shadow(
                    elevation = 8.dp,                 // 阴影高度
                    shape = RoundedCornerShape(16.dp), // 与圆角一致
                    clip = true                        // 同时按照该形状裁剪内容
                )
            // 显示图片
            if (savedImageFile != null) {
                // 图片
                Image1(modifierImg, savedImageFile!!)
                // 删除图标
                DeleteIcon(
                    Modifier.align(Alignment.TopEnd),
                    iconSize = 25.dp,
                    {
                        // 删除数据库路径
                        journey.coverImagePath = ""
                        updateJourney(journey)
                        savedImageFile = null
                    }
                )
            } else {
                // 添加图标
                AddIcon(
                    modifierImg,
                    iconSize = iconSize,
                    { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) })
            }
        }
    }
}

/**
 * 将图片 URI 复制到应用内部存储，返回保存的 File
 */
fun copyToInternalStorage(context: Context, uri: Uri): File? {
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