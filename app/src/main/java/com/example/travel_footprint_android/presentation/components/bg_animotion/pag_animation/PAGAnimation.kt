//package com.example.travel_footprint_android.presentation.components.bg_animotion.pag_animation
//
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//import org.libpag.PAGFile
//import org.libpag.PAGView
//
//@Composable
//fun PAGAnimation(
//    modifier: Modifier = Modifier,
//    fileName: String,
//    isPlaying: Boolean = true
//) {
//    val context = LocalContext.current
//
//    // 加载PAG文件
//    val pagComposition = remember(fileName) {
//        context.assets.open(fileName).use { inputStream ->
//            PAGFile.Load(inputStream.readBytes())
//        }
//    }
//
//    AndroidView(
//        factory = { ctx ->
//            PAGView(ctx).apply {
//                setComposition(pagComposition)
//                repeatCount = PAGView.REPEAT_COUNT_LOOP
//                if (isPlaying) play()
//            }
//        },
//        modifier = modifier
//    )
//}
//
