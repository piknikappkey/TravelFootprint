package com.example.travel_footprint_android.presentation.components.opengl_rain

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun OpenGLRainBackground(
    modifier: Modifier = Modifier,
    show: Boolean = true,
    collisionRect: Rect? = null,
    collisionCornerRadiusPx: Float = 0f,
    textCollisions: List<RainTextCollision> = emptyList(),
) {
    if (!show) return

    val textureView = remember { mutableMapOf<String, RainTextureView>() }

    AndroidView(
        factory = { context ->
            RainTextureView(context).also { view ->
                textureView["view"] = view
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { view ->
            view.setTextCollisions(textCollisions)
            if (collisionRect != null) {
                view.setCollisionRect(
                    collisionRect.left,
                    collisionRect.top,
                    collisionRect.right,
                    collisionRect.bottom,
                    collisionCornerRadiusPx
                )
            } else {
                view.clearCollisionRect()
            }
        },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose {
            textureView.clear()
        }
    }
}
