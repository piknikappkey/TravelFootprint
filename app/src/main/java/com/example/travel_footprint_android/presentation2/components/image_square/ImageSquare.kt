package com.example.travel_footprint_android.presentation2.components.image_square

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.travel_footprint_android.R
import com.example.travel_footprint_android.data.entity.Journey
import com.example.travel_footprint_android.ui.theme.BGLight0
import com.example.travel_footprint_android.ui.theme.FontDark6
import com.example.travel_footprint_android.ui.theme.SecondColor3
import com.example.travel_footprint_android.ui.theme.SecondColor4
import java.io.File

@Composable
fun ImageSquare(
    journey: Journey,
    updateJourney: (Journey) -> Unit
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp)
    ) {
        // 设置选中和未选中图片样式
        val modifier =  Modifier
            .fillMaxWidth()                // 宽度占满父容器
            .aspectRatio(1.2f)      // 宽高比1.2：1
            .shadow(
                elevation = 8.dp,                 // 阴影高度
                shape = RoundedCornerShape(16.dp), // 与圆角一致
                clip = true                        // 同时按照该形状裁剪内容
            )
        // 显示图片
        if (savedImageFile != null) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = savedImageFile,
                    onError = { error ->
                        Log.e("ImageUpload", "Coil 加载失败: ${error.result.throwable}")
                    }
                ),
                contentDescription = "选择的图片",
                modifier = modifier,
                contentScale = ContentScale.Crop
            )

            val iconSize = 25.dp
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = iconSize / 3, y = -(iconSize / 3))
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, SecondColor3, RoundedCornerShape(16.dp))
                    .background(BGLight0.copy(alpha = 0.8f))
                    .clickable {
                        // 删除数据库路径
                        journey.coverImagePath = ""
                        updateJourney(journey)
                        savedImageFile = null
                    },
            ) {
                Image(
                    modifier = Modifier
                        .size(iconSize),
                    painter = painterResource(id = R.drawable.ic_delete3),
                    contentDescription = "删除图标",
                    colorFilter = ColorFilter.tint(SecondColor4),
                )
            }
        } else {
            Box(
                modifier = modifier
                    .background(BGLight0)
                    .clickable { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            ) {
                Image(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(.3f),
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "删除图标",
                    colorFilter = ColorFilter.tint(FontDark6),
                )
            }
        }
    }

//    Column(
//        modifier = Modifier.fillMaxSize().padding(16.dp),
//        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
//    ) {
//        Button(onClick = {
//            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//        }) {
//            Text("从相册选择图片")
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // 显示图片
//        if (savedImageFile != null) {
//            Image(
//                painter = rememberAsyncImagePainter(
//                    model = savedImageFile,
//                    onError = { error ->
//                        Log.e("ImageUpload", "Coil 加载失败: ${error.result.throwable}")
//                    }
//                ),
//                contentDescription = "选择的图片",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(300.dp),
//                contentScale = ContentScale.Fit
//            )
//        } else {
//            Text("还未选择图片")
//        }
//    }
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