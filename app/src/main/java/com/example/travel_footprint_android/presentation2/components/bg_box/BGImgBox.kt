package com.example.travel_footprint_android.presentation2.components.bg_box

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.random.Random

private object BitmapCache {
    private const val maxCacheSize = 8
    private val cache = LinkedHashMap<Int, Bitmap>(maxCacheSize, 0.75f, true)
    private val optionsCache = LinkedHashMap<Int, BitmapFactory.Options>(maxCacheSize, 0.75f, true)

    fun get(resId: Int): Bitmap? = synchronized(cache) { cache[resId] }

    fun put(resId: Int, bitmap: Bitmap) = synchronized(cache) {
        if (cache.size >= maxCacheSize) {
            val oldestKey = cache.keys.first()
            cache.remove(oldestKey)?.recycle()
            optionsCache.remove(oldestKey)
        }
        cache[resId] = bitmap
    }

    fun getOptions(resId: Int): BitmapFactory.Options? = synchronized(optionsCache) { optionsCache[resId] }

    fun putOptions(resId: Int, options: BitmapFactory.Options) = synchronized(optionsCache) {
        optionsCache[resId] = options
    }
}

@Composable
fun BGImgBox(
    imgList: List<Int>,
    modifier: Modifier = Modifier,
    drawRectColor: Color = Color.White.copy(alpha = .3f),
    composable: @Composable (() -> Unit)
) {
    if (imgList.isEmpty()) {
        Box { composable() }
        return
    }

    val context = LocalContext.current
    val screenSize = remember { getScreenSize(context) }
    
    val randIndex by remember { mutableStateOf(Random.nextInt(0, imgList.size)) }
    val selectedResId = imgList[randIndex]

    var bgBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(selectedResId) {
        bgBitmap = loadBitmapAsync(context, selectedResId, screenSize)
    }

    Box(
        modifier = modifier
            .background(
                color = Color.White
            )
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
}

private suspend fun loadBitmapAsync(
    context: Context,
    resId: Int,
    screenSize: Point
): androidx.compose.ui.graphics.ImageBitmap? = withContext(Dispatchers.IO) {
    BitmapCache.get(resId)?.let {
        return@withContext it.asImageBitmap()
    }

    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeResource(context.resources, resId, options)

    val sampleSize = calculateInSampleSize(options, screenSize.x, screenSize.y)

    options.apply {
        inJustDecodeBounds = false
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.RGB_565
        inMutable = true
        
        val cachedBitmap = BitmapCache.get(resId)
        if (cachedBitmap != null && cachedBitmap.isMutable) {
            inBitmap = cachedBitmap
        }
    }

    val bitmap = BitmapFactory.decodeResource(context.resources, resId, options)
    bitmap?.let {
        BitmapCache.put(resId, it)
        BitmapCache.putOptions(resId, options)
    }

    bitmap?.asImageBitmap()
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

private fun getScreenSize(context: Context): Point {
    val displayMetrics = context.resources.displayMetrics
    return Point(displayMetrics.widthPixels, displayMetrics.heightPixels)
}
