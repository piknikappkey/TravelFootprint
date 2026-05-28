package com.example.travel_footprint_android.presentation2.components.bg_box

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
    imgList: List<Int>,
    modifier: Modifier = Modifier,
    drawRectColor: Color = Color.White.copy(alpha = .3f),
    composable: @Composable (() -> Unit)
) {
    val starTime = System.currentTimeMillis()

    if (imgList.isEmpty()) {
//    if (true) {
        Box { composable() }
        return
    }

    val context = LocalContext.current
    val screenSize = remember { getScreenSize(context) }
    
    val randIndex by remember { mutableStateOf(Random.nextInt(0, imgList.size)) }
    val selectedResId = imgList[randIndex]

    var bgBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }

    LaunchedEffect(selectedResId) {
        val request = ImageRequest.Builder(context)
            .data(selectedResId)
            .allowHardware(false)
            .build()

        val result = imageLoader.execute(request)
        if (result is SuccessResult) {
            bgBitmap = result.drawable.toBitmap().asImageBitmap()
        }
    }

    Box(
        modifier = modifier
            .background(Color(0xFFFCF1EB))
            .drawBehind {
                bgBitmap?.let { bitmap ->
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val imageWidth = bitmap.width.toFloat()
                    val imageHeight = bitmap.height.toFloat()

                    val scale = maxOf(canvasWidth / imageWidth, canvasHeight / imageHeight)
                    val scaledWidth = imageWidth * scale
                    val scaledHeight = imageHeight * scale

                    drawImage(
                        image = bitmap,
                        dstOffset = IntOffset(
                            ((canvasWidth - scaledWidth) / 2).roundToInt(),
                            ((canvasHeight - scaledHeight) / 2).roundToInt()
                        ),
                        dstSize = IntSize(scaledWidth.roundToInt(), scaledHeight.roundToInt())
                    )

                    drawRect(color = drawRectColor)
                }
            }
    ) {
        composable()
    }

    Log.d("ComposeTime", "BGImgBox: ${System.currentTimeMillis() - starTime}")
}

private fun getScreenSize(context: android.content.Context): Point {
    val displayMetrics = context.resources.displayMetrics
    return Point(displayMetrics.widthPixels, displayMetrics.heightPixels)
}