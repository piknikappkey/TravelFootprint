package com.example.travel_footprint_android.presentation2.components.image_square.image1

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun Image1(
    modifier: Modifier = Modifier,
    savedImageFile: File,
) {
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
}