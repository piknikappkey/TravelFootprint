package com.example.travel_footprint_android.presentation.components.bg_animotion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 太阳光晕背景动效组件 - 增强版
 *
 * 模拟真实太阳效果：
 * - 多层光晕（外发光、弥散光、主光晕）
 * - 动态光线（长度、宽度、透明度变化）
 * - 柔和脉动
 * - 真实感日冕效果
 *
 * @param modifier 外部 Modifier
 * @param isVisible 是否显示
 * @param sunPosition 太阳中心相对位置，x/y 均在 0..1 之间，默认右上角
 * @param sunRadius 太阳本体半径（dp 像素值）
 * @param haloRadius 光晕半径（dp 像素值）
 * @param sunColor 太阳主色
 * @param haloColor 光晕主色
 * @param rayColor 光线颜色
 * @param rayCount 光线数量（越多越细腻）
 * @param rayMaxLength 光线最大延伸长度
 * @param animationDuration 光线旋转一周的时间（毫秒）
 */
@Composable
fun SunGlowEffect(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    sunPosition: Offset = Offset(0.78f, 0.15f),
    sunRadius: Float = 55f,
    haloRadius: Float = 280f,
    sunColor: Color = Color(0xFFFFF5E0),
    haloColor: Color = Color(0xFFFFB347),
    rayColor: Color = Color(0xFFFFE4A0),
    rayCount: Int = 24,
    rayMaxLength: Float = 500f,
    animationDuration: Int = 300000
) {
    if (!isVisible) return

    val infiniteTransition = rememberInfiniteTransition(label = "sun_enhanced")

    // 缓慢旋转
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing)
        ),
        label = "sun_rotation"
    )

    // 柔和脉动（光晕大小变化）
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing)
        ),
        label = "sun_pulse"
    )

    // 光线闪烁（独立于旋转的亮度变化）
    val rayGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing)
        ),
        label = "ray_glow"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(
            x = sunPosition.x * size.width,
            y = sunPosition.y * size.height
        )

        val pulseScale = 1f + pulse * 0.06f
        val glowIntensity = 0.85f + rayGlow * 0.15f

        // ============================================================
        // 1. 最外层超弥散光晕（大型环境光）
        // ============================================================
        drawCircle(
            color = haloColor.copy(alpha = 0.04f * glowIntensity),
            radius = haloRadius * pulseScale * 2.8f,
            center = center
        )

        drawCircle(
            color = haloColor.copy(alpha = 0.06f * glowIntensity),
            radius = haloRadius * pulseScale * 2.0f,
            center = center
        )

        // ============================================================
        // 2. 中层光晕（暖色弥散）
        // ============================================================
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    haloColor.copy(alpha = 0.12f * glowIntensity),
                    haloColor.copy(alpha = 0.06f * glowIntensity),
                    Color.Transparent
                ),
                center = center,
                radius = haloRadius * pulseScale * 1.5f
            ),
            radius = haloRadius * pulseScale * 1.5f,
            center = center
        )

        // ============================================================
        // 3. 主光晕层（带渐变）
        // ============================================================
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    sunColor.copy(alpha = 0.5f * glowIntensity),
                    haloColor.copy(alpha = 0.2f * glowIntensity),
                    haloColor.copy(alpha = 0.08f * glowIntensity),
                    Color.Transparent
                ),
                center = center,
                radius = haloRadius * pulseScale
            ),
            radius = haloRadius * pulseScale,
            center = center
        )

        // ============================================================
        // 4. 真实感光线系统（多层次、渐变长度和透明度）
        // ============================================================
        rotate(degrees = rotation, pivot = center) {
            val angleStep = 360f / rayCount

            for (i in 0 until rayCount) {
                val angle = Math.toRadians((i * angleStep).toDouble())
                val cosA = cos(angle).toFloat()
                val sinA = sin(angle).toFloat()

                // 光线长度随机变化（模拟真实太阳的不规则日冕）
                val lengthVariation = 0.6f + 0.4f * (0.5f + 0.5f * sin(i * 1.7f + pulse * 2.3f))
                val currentLength = sunRadius + (rayMaxLength - sunRadius * 0.5f) * lengthVariation

                // 光线宽度：从根部到尖端逐渐变细
                val baseWidth = 2.5f + 3f * (0.5f + 0.5f * sin(i * 2.1f + pulse * 1.1f))
                val tipWidth = 0.5f + 0.8f * (0.5f + 0.5f * cos(i * 1.3f + pulse * 0.7f))

                // 光线透明度：不同光线透明度不同，更自然
                val alphaBase = 0.15f + 0.25f * (0.5f + 0.5f * sin(i * 1.9f + rotation * 0.01f))
                val alphaFactor = alphaBase * glowIntensity

                // 光线起点（从太阳边缘开始）
                val startX = center.x + cosA * sunRadius
                val startY = center.y + sinA * sunRadius

                // 光线终点
                val endX = center.x + cosA * (sunRadius + currentLength)
                val endY = center.y + sinA * (sunRadius + currentLength)

                // 绘制带渐变宽度的光线（用梯形近似）
                // 使用两条线绘制更宽的光线效果
                val widthFactor = 1f + 0.5f * (1f - lengthVariation) // 短光线更宽

                // 主光线
                drawLine(
                    color = rayColor.copy(alpha = alphaFactor * 0.9f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = baseWidth * widthFactor * 0.7f
                )

                // 辅光线（更细更淡，增加层次）
                drawLine(
                    color = rayColor.copy(alpha = alphaFactor * 0.35f),
                    start = Offset(
                        startX + cosA * 1.5f,
                        startY + sinA * 1.5f
                    ),
                    end = Offset(
                        endX + cosA * 3f,
                        endY + sinA * 3f
                    ),
                    strokeWidth = baseWidth * widthFactor * 0.3f
                )

                // ============================================================
                // 5. 光线尖端微光（增加细节）
                // ============================================================
                if (currentLength > sunRadius + 30f) {
                    drawCircle(
                        color = rayColor.copy(alpha = alphaFactor * 0.15f),
                        radius = 4f + 6f * (0.5f + 0.5f * sin(i * 3.7f + pulse * 2.0f)),
                        center = Offset(endX, endY)
                    )
                }
            }
        }

        // ============================================================
        // 6. 额外：随机日冕丝状光（让光线更丰富）
        // ============================================================
        rotate(degrees = rotation * 0.7f + 15f, pivot = center) {
            val extraRayCount = 8
            val angleStep = 360f / extraRayCount
            for (i in 0 until extraRayCount) {
                val angle = Math.toRadians((i * angleStep + pulse * 5f).toDouble())
                val cosA = cos(angle).toFloat()
                val sinA = sin(angle).toFloat()

                val length = sunRadius + rayMaxLength * (0.7f + 0.3f * (0.5f + 0.5f * sin(i * 2.5f + pulse * 1.5f)))

                drawLine(
                    color = rayColor.copy(alpha = 0.06f * glowIntensity),
                    start = Offset(
                        center.x + cosA * (sunRadius * 0.5f),
                        center.y + sinA * (sunRadius * 0.5f)
                    ),
                    end = Offset(
                        center.x + cosA * length,
                        center.y + sinA * length
                    ),
                    strokeWidth = 1.5f + 2f * (0.5f + 0.5f * sin(i * 4.1f))
                )
            }
        }

        // ============================================================
        // 7. 太阳本体（多层绘制，更有层次感）
        // ============================================================

        // 7.1 太阳内层高光（中心最亮）
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.98f),
                    sunColor.copy(alpha = 0.9f),
                    sunColor.copy(alpha = 0.7f),
                    haloColor.copy(alpha = 0.3f)
                ),
                center = center,
                radius = sunRadius
            ),
            radius = sunRadius,
            center = center
        )

        // 7.2 太阳边缘光晕（日冕内层）
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    sunColor.copy(alpha = 0.3f * glowIntensity),
                    haloColor.copy(alpha = 0.15f * glowIntensity)
                ),
                center = center,
                radius = sunRadius * 1.6f
            ),
            radius = sunRadius * 1.6f,
            center = center
        )

        // 7.3 太阳表面细节光斑（增加真实感）
        val spotCount = 6
        for (j in 0 until spotCount) {
            val spotAngle = j * 2.1f + pulse * 0.5f
            val dist = sunRadius * (0.25f + 0.35f * (0.5f + 0.5f * sin(j * 3.7f + pulse * 1.3f)))
            val spotX = center.x + cos(spotAngle) * dist
            val spotY = center.y + sin(spotAngle) * dist

            drawCircle(
                color = Color.White.copy(alpha = 0.15f + 0.1f * (0.5f + 0.5f * sin(j * 5.1f + pulse * 2.0f))),
                radius = sunRadius * (0.12f + 0.08f * (0.5f + 0.5f * cos(j * 2.3f))),
                center = Offset(spotX, spotY)
            )
        }

        // ============================================================
        // 8. 终极光晕 - 镜头光晕效果（模拟相机眩光）
        // ============================================================
        val lensFlareSize = sunRadius * 2.5f * (0.5f + 0.5f * sin(pulse * 3.14f * 2f))
        drawCircle(
            color = Color.White.copy(alpha = 0.025f * glowIntensity),
            radius = lensFlareSize,
            center = Offset(
                center.x - sunRadius * 0.4f,
                center.y - sunRadius * 0.4f
            )
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.015f * glowIntensity),
            radius = lensFlareSize * 0.6f,
            center = Offset(
                center.x + sunRadius * 0.7f,
                center.y - sunRadius * 0.5f
            )
        )
    }
}