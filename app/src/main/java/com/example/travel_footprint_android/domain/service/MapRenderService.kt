// app/src/main/java/com/example/travel_footprint_android/domain/service/MapRenderService.kt
package com.example.travel_footprint_android.domain.service

import android.graphics.*
import android.view.View
import com.example.travel_footprint_android.data.entity.Footprint
import javax.inject.Inject
import javax.inject.Singleton
import java.io.FileOutputStream

@Singleton
class MapRenderService @Inject constructor(
    private val handDrawEngine: HandDrawEngine,
    private val trailAnimator: TrailAnimator,
    private val localFileManager: LocalFileManager
) {

    data class MapRenderConfig(
        val style: HandDrawStyle = HandDrawStyle.WATERCOLOR,
        val showTrail: Boolean = true,
        val showMarkers: Boolean = true,
        val trailColor: Int = Color.parseColor("#8B4513"),
        val markerColor: Int = Color.parseColor("#CD5C5C")
    )

    private var currentConfig = MapRenderConfig()
    private var mapView: View? = null

    /**
     * 加载手绘地图
     */
    fun loadHandDrawMap(style: HandDrawStyle, texturePath: String?) {
        currentConfig = currentConfig.copy(style = style)

        // 这里调用地图SDK的API加载手绘风格地图
        // 具体实现取决于选择的地图SDK
    }

    /**
     * 绘制足迹标记
     */
    fun drawFootprintMarkers(
        canvas: Canvas,
        footprints: List<Footprint>,
        customIcons: Map<String, Bitmap>? = null
    ) {
        val paint = Paint().apply {
            color = currentConfig.markerColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        footprints.forEach { footprint ->
            // 这里需要将足迹坐标转换为屏幕坐标
            // val screenPoint = mapView?.toScreenPoint(footprint.location)

            // 绘制标记
            canvas.drawCircle(100f, 100f, 20f, paint)  // 示例坐标

            // 绘制标题
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 30f
                isAntiAlias = true
            }
            canvas.drawText(footprint.title, 120f, 100f, textPaint)
        }
    }

    /**
     * 绘制轨迹线
     */
    fun drawTrailLine(
        canvas: Canvas,
        points: List<Pair<Double, Double>>,
        animated: Boolean,
        color: Int
    ) {
        if (points.size < 2) return

        val path = handDrawEngine.generateSketchPath(points, 1.5f)
        val paint = Paint().apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = 8f
            isAntiAlias = true
            pathEffect = handDrawEngine.createPencilStroke(8f, 0.5f)
        }

        if (animated) {
            trailAnimator.animatePathDraw(path, 2000, Easing.LINEAR) { progress, animatedPath ->
                // 动画更新时绘制
                canvas.drawPath(animatedPath, paint)
            }
        } else {
            canvas.drawPath(path, paint)
        }
    }

    /**
     * 截图导出
     */
    fun captureForExport(includeUI: Boolean): Bitmap? {
        mapView?.let { view ->
            val bitmap = Bitmap.createBitmap(
                view.width,
                view.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return bitmap
        }
        return null
    }

    /**
     * 渲染到文件
     */
    fun renderToFile(outputPath: String): Boolean {
        val bitmap = captureForExport(true) ?: return false
        return try {
            FileOutputStream(outputPath).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 设置地图视图
     */
    fun setMapView(view: View) {
        this.mapView = view
    }

    /**
     * 更新配置
     */
    fun updateConfig(config: MapRenderConfig) {
        this.currentConfig = config
    }
}