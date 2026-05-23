//package com.example.travel_footprint_android.presentation2.components.bg_box
//
//import android.graphics.BitmapFactory
//import androidx.compose.foundation.layout.Box
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.drawBehind
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.IntOffset
//import androidx.compose.ui.unit.IntSize
//import kotlin.math.roundToInt
//import kotlin.random.Random
//
//@Composable
//fun BGImgBox(
//    imgList: List<Int>,
//    modifier: Modifier = Modifier,
//    drawRectColor: Color = Color.White.copy(alpha = .3f),
//    composable: @Composable (() -> Unit)
//) {
//    if (imgList.isEmpty()) {
//        Box{
//            composable()
//        }
//    }
//    val context = LocalContext.current
//    val randIndex by remember { mutableStateOf(Random.nextInt(0, imgList.size)) }
//    val bgBitmap = remember(randIndex) {
//        val bitmap = BitmapFactory.decodeResource(context.resources, imgList[randIndex])
//        bitmap.asImageBitmap()
//    }
//
//    Box(
//        modifier = modifier
//            .drawBehind {
//                val canvasWidth = size.width
//                val canvasHeight = size.height
//                val imageWidth = bgBitmap.width.toFloat()
//                val imageHeight = bgBitmap.height.toFloat()
//
//                // 计算缩放比例，取较大值以保证全覆盖
//                val scale = maxOf(canvasWidth / imageWidth, canvasHeight / imageHeight)
//                val scaledWidth = imageWidth * scale
//                val scaledHeight = imageHeight * scale
//
//                // 将缩放后的图片居中绘制
//                drawImage(
//                    image = bgBitmap,
//                    dstOffset = IntOffset(
//                        ((canvasWidth - scaledWidth) / 2).roundToInt(),
//                        ((canvasHeight - scaledHeight) / 2).roundToInt()
//                    ),
//                    dstSize = IntSize(scaledWidth.roundToInt(), scaledHeight.roundToInt())
//                )
//
//                // 半透明遮罩
//                drawRect(color = drawRectColor)
//            }
//    ) {
//        composable()
//    }
//}