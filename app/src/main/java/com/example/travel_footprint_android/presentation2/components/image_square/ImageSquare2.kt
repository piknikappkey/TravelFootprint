package com.example.travel_footprint_android.presentation2.components.image_square

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.travel_footprint_android.presentation2.components.image_square.add_icon.AddIcon
import com.example.travel_footprint_android.presentation2.components.image_square.delete_icon.DeleteIcon
import com.example.travel_footprint_android.presentation2.components.image_square.image1.Image1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
) {
    val context = LocalContext.current

    var savedImageFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(imgPath) {
        Log.d("ImageUpload", "imgPath: $imgPath")
        if (imgPath.isNotEmpty()) {
            val exists = withContext(Dispatchers.IO) {
                File(imgPath).exists()
            }
            savedImageFile = if (exists) File(imgPath) else null
            return@LaunchedEffect
        }
        savedImageFile = null
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
        ) {
            val modifierImg = Modifier
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
            } else {
                AddIcon(
                    modifierImg,
                    iconSize = addIconSize,
                    { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) })
            }
        }
    }
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
