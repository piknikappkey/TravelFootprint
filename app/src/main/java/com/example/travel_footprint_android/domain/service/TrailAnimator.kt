// app/src/main/java/com/example/travel_footprint_android/domain/service/TrailAnimator.kt
package com.example.travel_footprint_android.domain.service

import android.animation.ValueAnimator
import android.graphics.*
import android.view.animation.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class TrailAnimator @Inject constructor() {

    private var animator: ValueAnimator? = null

    private val _animationProgress = MutableStateFlow(0f)
    val animationProgress: StateFlow<Float> = _animationProgress

    /**
     * 动画绘制路径
     */
    fun animatePathDraw(
        path: Path,
        duration: Long,
        easeType: Easing,
        onProgress: (Float, Path) -> Unit
    ) {
        val pathMeasure = PathMeasure(path, false)
        val totalLength = pathMeasure.length
        val animatedPath = Path()

        animator?.cancel()

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = getInterpolator(easeType)

            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                _animationProgress.value = progress

                animatedPath.reset()
                val length = totalLength * progress
                pathMeasure.getSegment(0f, length, animatedPath, true)

                onProgress(progress, animatedPath)
            }

            start()
        }
    }

    /**
     * 动画虚线偏移
     */
    fun animateDashOffset(
        path: Path,
        dashPattern: FloatArray,
        duration: Long = 2000,
        onUpdate: (PathEffect) -> Unit
    ) {
        animator?.cancel()

        animator = ValueAnimator.ofFloat(0f, dashPattern.sum()).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()

            addUpdateListener { animation ->
                val offset = animation.animatedValue as Float
                val effect = DashPathEffect(dashPattern, offset)
                onUpdate(effect)
            }

            start()
        }
    }

    /**
     * 暂停动画
     */
    fun pauseAnimation() {
        animator?.pause()
    }

    /**
     * 恢复动画
     */
    fun resumeAnimation() {
        animator?.resume()
    }

    /**
     * 停止动画
     */
    fun stopAnimation() {
        animator?.cancel()
        animator = null
        _animationProgress.value = 0f
    }

    /**
     * 设置循环
     */
    fun setLoopEnabled(enabled: Boolean) {
        animator?.repeatCount = if (enabled) ValueAnimator.INFINITE else 0
    }

    /**
     * 挂起函数形式的动画
     */
    suspend fun animatePathSuspend(
        path: Path,
        duration: Long,
        easeType: Easing
    ): Result<Path> = suspendCancellableCoroutine { continuation ->
        val resultPath = Path()

        animatePathDraw(path, duration, easeType) { progress, animatedPath ->
            resultPath.set(animatedPath)

            if (progress >= 1f) {
                continuation.resume(Result.success(resultPath))
            }
        }

        continuation.invokeOnCancellation {
            stopAnimation()
        }
    }

    /**
     * 获取插值器
     */
    private fun getInterpolator(easeType: Easing): Interpolator {
        return when (easeType) {
            Easing.LINEAR -> LinearInterpolator()
            Easing.EASE_IN_OUT_QUAD -> AccelerateDecelerateInterpolator()
            Easing.EASE_OUT_BOUNCE -> BounceInterpolator()
            Easing.EASE_IN_ELASTIC -> AnticipateInterpolator()
        }
    }

    /**
     * 创建贝塞尔路径（用于平滑曲线）
     */
    fun createBezierPath(points: List<PointF>): Path {
        val path = Path()
        if (points.size < 2) return path

        path.moveTo(points[0].x, points[0].y)

        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]

            // 计算控制点
            val c1x = p1.x + (p2.x - p1.x) * 0.3f
            val c1y = p1.y
            val c2x = p2.x - (p2.x - p1.x) * 0.3f
            val c2y = p2.y

            path.cubicTo(c1x, c1y, c2x, c2y, p2.x, p2.y)
        }

        return path
    }
}