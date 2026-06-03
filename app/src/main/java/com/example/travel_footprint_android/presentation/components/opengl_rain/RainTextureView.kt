package com.example.travel_footprint_android.presentation.components.opengl_rain

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES30
import android.view.TextureView
import java.util.concurrent.atomic.AtomicBoolean

class RainTextureView(context: Context) : TextureView(context), TextureView.SurfaceTextureListener {

    private lateinit var simulation: RainSimulation
    private var renderer: RainGLRenderer? = null
    private var bgStreaks: BackgroundRainStreaks? = null

    private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE

    private val running = AtomicBoolean(false)
    private var renderThread: Thread? = null

    @Volatile private var surfaceWidth = 1
    @Volatile private var surfaceHeight = 1
    @Volatile private var pendingRect: FloatArray? = null
    @Volatile private var pendingTextCollisions: List<RainTextCollision> = emptyList()

    init {
        isOpaque = false
        surfaceTextureListener = this
    }

    fun setCollisionRect(left: Float, top: Float, right: Float, bottom: Float, cornerRadius: Float = 0f) {
        val rect = floatArrayOf(left, top, right, bottom, cornerRadius)
        pendingRect = rect
        if (::simulation.isInitialized) {
            simulation.pendingCollisionRect = rect
        }
    }

    fun clearCollisionRect() {
        pendingRect = null
        if (::simulation.isInitialized) {
            simulation.pendingCollisionRect = null
        }
    }

    fun setTextCollisions(collisions: List<RainTextCollision>) {
        pendingTextCollisions = collisions
        if (::simulation.isInitialized) {
            simulation.pendingTextCollisions = collisions
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
        simulation = RainSimulation(width, height).also {
            it.pendingCollisionRect = pendingRect
            it.pendingTextCollisions = pendingTextCollisions
            it.init()
        }
        startRenderLoop(surface)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        stopRenderLoop()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit

    private fun startRenderLoop(surface: SurfaceTexture) {
        running.set(true)
        renderThread = Thread({
            try {
                initEGL(surface)
                val r = RainGLRenderer()
                renderer = r
                r.init()

                val w0 = surfaceWidth
                val h0 = surfaceHeight
                val streaks = BackgroundRainStreaks(w0, h0)
                bgStreaks = streaks

                var lastFrameNs = System.nanoTime()

                while (running.get()) {
                    val frameStart = System.nanoTime()
                    val dt = ((frameStart - lastFrameNs).coerceIn(0L, 50_000_000L)) / 1_000_000_000f
                    lastFrameNs = frameStart

                    simulation.update()
                    streaks.update(dt)

                    val w = surfaceWidth
                    val h = surfaceHeight
                    GLES30.glViewport(0, 0, w, h)
                    GLES30.glClearColor(0f, 0f, 0f, 0f)
                    GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
                    r.draw(simulation, w, h, streaks)
                    EGL14.eglSwapBuffers(eglDisplay, eglSurface)

                    val elapsed = System.nanoTime() - frameStart
                    val sleepNs = FRAME_INTERVAL_NS - elapsed
                    if (sleepNs > 0L) {
                        Thread.sleep(sleepNs / 1_000_000L, (sleepNs % 1_000_000L).toInt())
                    }
                }

                r.release()
            } finally {
                releaseEGL()
            }
        }, "RainRenderThread")
        renderThread!!.start()
    }

    private fun stopRenderLoop() {
        running.set(false)
        renderThread?.join(3_000L)
        renderThread = null
        renderer = null
        bgStreaks = null
    }

    private fun initEGL(surface: SurfaceTexture) {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        EGL14.eglInitialize(eglDisplay, null, 0, null, 0)

        val configAttribs = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE,    EGL14.EGL_WINDOW_BIT,
            EGL14.EGL_RED_SIZE,        8,
            EGL14.EGL_GREEN_SIZE,      8,
            EGL14.EGL_BLUE_SIZE,       8,
            EGL14.EGL_ALPHA_SIZE,      8,
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(eglDisplay, configAttribs, 0, configs, 0, 1, numConfigs, 0)

        val ctxAttribs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE)
        eglContext = EGL14.eglCreateContext(
            eglDisplay, configs[0]!!, EGL14.EGL_NO_CONTEXT, ctxAttribs, 0
        )

        val surfAttribs = intArrayOf(EGL14.EGL_NONE)
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, configs[0]!!, surface, surfAttribs, 0)

        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
    }

    private fun releaseEGL() {
        EGL14.eglMakeCurrent(
            eglDisplay,
            EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_CONTEXT
        )
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglDestroySurface(eglDisplay, eglSurface)
            eglSurface = EGL14.EGL_NO_SURFACE
        }
        if (eglContext != EGL14.EGL_NO_CONTEXT) {
            EGL14.eglDestroyContext(eglDisplay, eglContext)
            eglContext = EGL14.EGL_NO_CONTEXT
        }
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglTerminate(eglDisplay)
            eglDisplay = EGL14.EGL_NO_DISPLAY
        }
    }

    companion object {
        private const val FRAME_INTERVAL_NS = 16_666_667L/2
    }
}
