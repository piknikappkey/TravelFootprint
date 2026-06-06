package com.example.travel_footprint_android.presentation.components.opengl_rain

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.sqrt

class RainGLRenderer {

    private val DROP_VERT = """
        #version 300 es
        layout(location = 0) in vec2 a_position;
        layout(location = 1) in float a_alpha;
        uniform vec2 u_resolution;
        out float v_alpha;
        void main() {
            vec2 ndc = (a_position / u_resolution) * 2.0 - 1.0;
            ndc.y = -ndc.y;
            gl_Position = vec4(ndc, 0.0, 1.0);
            v_alpha = a_alpha;
        }
    """.trimIndent()

    private val DROP_FRAG = """
        #version 300 es
        precision mediump float;
        in float v_alpha;
        uniform vec4 u_color;
        out vec4 fragColor;
        void main() {
            fragColor = vec4(u_color.rgb, u_color.a * v_alpha);
        }
    """.trimIndent()

    private val FRONT_DROP_VERT = """
        #version 300 es
        layout(location = 0) in vec2 a_position;
        layout(location = 1) in float a_alpha;
        uniform vec2 u_resolution;
        uniform float u_pt_base;
        uniform float u_pt_range;
        out float v_alpha;
        void main() {
            vec2 ndc = (a_position / u_resolution) * 2.0 - 1.0;
            ndc.y = -ndc.y;
            gl_Position = vec4(ndc, 0.0, 1.0);
            gl_PointSize = u_pt_base + a_alpha * u_pt_range;
            v_alpha = a_alpha;
        }
    """.trimIndent()

    private val FRONT_DROP_FRAG = """
        #version 300 es
        precision mediump float;
        in float v_alpha;
        uniform vec4 u_color;
        out vec4 fragColor;
        void main() {
            vec2 p = gl_PointCoord * 2.0 - 1.0;
            float streak = 1.0 - smoothstep(0.12, 0.55, length(vec2(p.x * 6.0, p.y * 0.55)));
            float taper  = 1.0 - p.y * p.y * 0.6;
            float fade   = streak * taper;
            fade = clamp(fade, 0.0, 1.0);
            float a = u_color.a * v_alpha * fade * 0.5;
            fragColor = vec4(u_color.rgb, a);
        }
    """.trimIndent()

    private val SPLASH_VERT = """
        #version 300 es
        layout(location = 0) in vec2 a_position;
        layout(location = 1) in float a_alpha;
        uniform vec2 u_resolution;
        uniform float u_pt_base;
        uniform float u_pt_range;
        out float v_alpha;
        void main() {
            vec2 ndc = (a_position / u_resolution) * 2.0 - 1.0;
            ndc.y = -ndc.y;
            gl_Position = vec4(ndc, 0.0, 1.0);
            gl_PointSize = u_pt_base + a_alpha * u_pt_range;
            v_alpha = a_alpha;
        }
    """.trimIndent()

    private val SPLASH_FRAG = """
        #version 300 es
        precision mediump float;
        in float v_alpha;
        uniform vec4 u_color;
        out vec4 fragColor;
        void main() {
            float d = length(gl_PointCoord - vec2(0.5)) * 2.0;
            float fade = 1.0 - smoothstep(0.0, 1.0, d);
            fade *= fade;
            fragColor = vec4(u_color.rgb, u_color.a * v_alpha * fade);
        }
    """.trimIndent()

    private val METABALL_SPLAT_VERT = """
        #version 300 es
        layout(location = 0) in vec2 a_position;
        layout(location = 1) in float a_alpha;
        uniform vec2 u_resolution;
        uniform float u_pt_base;
        uniform float u_pt_range;
        out float v_alpha;
        void main() {
            vec2 ndc = (a_position / u_resolution) * 2.0 - 1.0;
            ndc.y = -ndc.y;
            gl_Position = vec4(ndc, 0.0, 1.0);
            gl_PointSize = u_pt_base + a_alpha * u_pt_range;
            v_alpha = a_alpha;
        }
    """.trimIndent()

    private val METABALL_SPLAT_FRAG = """
        #version 300 es
        precision highp float;
        in float v_alpha;
        out vec4 fragColor;
        void main() {
            vec2 p   = gl_PointCoord * 2.0 - 1.0;
            float r2 = dot(p, p);
            if (r2 > 1.0) discard;
            float z      = sqrt(1.0 - r2);
            vec3  normal = vec3(p.x, p.y, z);
            float alpha = exp(-r2 * 2.5) * v_alpha;
            fragColor = vec4(normal * alpha, alpha);
        }
    """.trimIndent()

    private val METABALL_COMPOSITE_VERT = """
        #version 300 es
        out vec2 v_uv;
        const vec2 POS[6] = vec2[](
            vec2(-1.0,-1.0), vec2(1.0,-1.0), vec2(-1.0,1.0),
            vec2(-1.0, 1.0), vec2(1.0,-1.0), vec2(1.0, 1.0)
        );
        void main() {
            vec2 pos  = POS[gl_VertexID];
            gl_Position = vec4(pos, 0.0, 1.0);
            v_uv = pos * 0.5 + 0.5;
        }
    """.trimIndent()

    private val METABALL_COMPOSITE_FRAG = """
        #version 300 es
        precision mediump float;
        uniform sampler2D u_fbo_tex;
        uniform float     u_threshold;
        in  vec2 v_uv;
        out vec4 fragColor;
        void main() {
            vec4  acc   = texture(u_fbo_tex, v_uv);
            float alpha = acc.a;
            if (alpha < 0.005) discard;
            vec3 normal = normalize(acc.rgb / max(alpha, 0.001));
            float shape = smoothstep(u_threshold * 0.75, u_threshold * 1.25, alpha);
            if (shape < 0.01) discard;
            vec3  lightDir  = normalize(vec3(0.35, 0.55, 1.0));
            vec3  viewDir   = vec3(0.0, 0.0, 1.0);
            vec3  halfDir   = normalize(viewDir + lightDir);
            float spec      = pow(max(dot(normal, halfDir), 0.0), 28.0);
            float diff      = max(dot(normal, lightDir), 0.0) * 0.25 + 0.45;
            vec3  baseColor = vec3(0.45, 0.72, 1.0);
            vec3  specColor = vec3(0.68, 0.84, 1.0);
            vec3  color     = baseColor * diff + specColor * spec * 0.65;
            fragColor = vec4(color, shape * 0.78);
        }
    """.trimIndent()

    companion object {
        private const val FRONT_DROP_PT_BASE  = 3f
        private const val FRONT_DROP_PT_RANGE = 7f

        private const val SPLASH_PT_BASE  = 20f
        private const val SPLASH_PT_RANGE = 18f

        private const val METABALL_PT_BASE      = 26f
        private const val METABALL_PT_RANGE     = 18f
        private const val METABALL_ALPHA_THRESHOLD = 0.42f

        private const val SPLASH_MAX_VY = 18f

        private const val FLOATS_PER_VERT = 3
    }

    private var dropProgram   = 0
    private var frontDropProgram = 0
    private var splashProgram = 0
    private var metaballSplatProgram = 0
    private var metaballCompositeProgram = 0
    private var vboId         = 0

    private var metaballFboId  = 0
    private var metaballTexId  = 0
    private var metaballFboW   = 0
    private var metaballFboH   = 0
    private var useMetaball    = false

    private var initialized = false

    private var dropResolutionLoc = -1
    private var dropColorLoc      = -1
    private var frontDropResolutionLoc = -1
    private var frontDropColorLoc      = -1
    private var frontDropPtBaseLoc     = -1
    private var frontDropPtRangeLoc    = -1
    private var splashResolutionLoc = -1
    private var splashColorLoc      = -1
    private var splashPtBaseLoc     = -1
    private var splashPtRangeLoc    = -1
    private var splatResLoc   = -1
    private var splatPtBaseLoc  = -1
    private var splatPtRangeLoc = -1
    private var compFboTexLoc   = -1
    private var compThresholdLoc = -1

    private var floatBuffer: FloatBuffer? = null
    private var bufferCapacity = 0
    private var startTimeNs = 0L

    fun init() {
        dropProgram   = buildProgram(DROP_VERT,   DROP_FRAG)
        frontDropProgram = buildProgram(FRONT_DROP_VERT, FRONT_DROP_FRAG)
        splashProgram = buildProgram(SPLASH_VERT, SPLASH_FRAG)

        metaballSplatProgram     = buildProgram(METABALL_SPLAT_VERT,     METABALL_SPLAT_FRAG)
        metaballCompositeProgram = buildProgram(METABALL_COMPOSITE_VERT, METABALL_COMPOSITE_FRAG)

        dropResolutionLoc   = GLES30.glGetUniformLocation(dropProgram,   "u_resolution")
        dropColorLoc        = GLES30.glGetUniformLocation(dropProgram,   "u_color")
        frontDropResolutionLoc = GLES30.glGetUniformLocation(frontDropProgram, "u_resolution")
        frontDropColorLoc      = GLES30.glGetUniformLocation(frontDropProgram, "u_color")
        frontDropPtBaseLoc     = GLES30.glGetUniformLocation(frontDropProgram, "u_pt_base")
        frontDropPtRangeLoc    = GLES30.glGetUniformLocation(frontDropProgram, "u_pt_range")
        splashResolutionLoc = GLES30.glGetUniformLocation(splashProgram, "u_resolution")
        splashColorLoc      = GLES30.glGetUniformLocation(splashProgram, "u_color")
        splashPtBaseLoc     = GLES30.glGetUniformLocation(splashProgram, "u_pt_base")
        splashPtRangeLoc    = GLES30.glGetUniformLocation(splashProgram, "u_pt_range")
        splatResLoc    = GLES30.glGetUniformLocation(metaballSplatProgram, "u_resolution")
        splatPtBaseLoc = GLES30.glGetUniformLocation(metaballSplatProgram, "u_pt_base")
        splatPtRangeLoc= GLES30.glGetUniformLocation(metaballSplatProgram, "u_pt_range")
        compFboTexLoc   = GLES30.glGetUniformLocation(metaballCompositeProgram, "u_fbo_tex")
        compThresholdLoc= GLES30.glGetUniformLocation(metaballCompositeProgram, "u_threshold")

        val ids = IntArray(1)
        GLES30.glGenBuffers(1, ids, 0)
        vboId = ids[0]

        startTimeNs = System.nanoTime()
        initialized = true
    }

    fun draw(simulation: RainSimulation, screenWidth: Int, screenHeight: Int,
             bgStreaks: BackgroundRainStreaks? = null) {
        if (!initialized || !simulation.initOK) return

        val positions  = simulation.positionBuffer
        val velocities = simulation.velocityBuffer
        val prop       = simulation.proportion
        val infos      = simulation.groupInfos
        val infoCount  = simulation.groupInfoCount

        var splashCount = 0
        for (idx in 0 until infoCount) {
            val info = infos[idx]
            if (!info.cohesive) splashCount += info.particleCount
        }

        val bgFloats = if (bgStreaks != null) BackgroundRainStreaks.TOTAL_FLOATS else 0
        val totalFloats = bgFloats + splashCount * FLOATS_PER_VERT
        var bgVertCount = 0
        var splashOffset = 0
        var actualSplashCount = 0
        if (totalFloats > 0) {
            ensureBuffer(totalFloats)
            val buf = floatBuffer!!
            buf.clear()

            bgVertCount = if (bgStreaks != null) {
                val posBefore = buf.position()
                bgStreaks.fillVertices(buf)
                (buf.position() - posBefore) / FLOATS_PER_VERT
            } else 0

            splashOffset = bgVertCount

            for (idx in 0 until infoCount) {
                val info = infos[idx]
                if (info.cohesive) continue
                val start = info.bufferStart
                val end   = start + info.particleCount
                for (i in start until end) {
                    val vx    = velocities[i].x
                    val vyVal = velocities[i].y
                    if (vyVal > SPLASH_MAX_VY) continue
                    val px    = positions[i].x * prop
                    val py    = positions[i].y * prop
                    val spd   = sqrt(vx * vx + vyVal * vyVal)
                    val energy = (spd * prop * 0.00022f).coerceIn(0.25f, 1.0f)
                    buf.put(px); buf.put(py); buf.put(energy)
                    actualSplashCount++
                }
            }

            buf.flip()
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
            GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                buf.limit() * 4,
                buf,
                GLES30.GL_STREAM_DRAW
            )
        }

        val stride = FLOATS_PER_VERT * 4

        GLES30.glEnable(GLES30.GL_BLEND)

        if (totalFloats <= 0) {
            GLES30.glDisable(GLES30.GL_BLEND)
            return
        }

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glEnableVertexAttribArray(1)

        if (bgVertCount > 0) {
            GLES30.glUseProgram(dropProgram)
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
            GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, stride, 0)
            GLES30.glVertexAttribPointer(1, 1, GLES30.GL_FLOAT, false, stride, 8)

            setResolution(dropResolutionLoc, screenWidth.toFloat(), screenHeight.toFloat())
            setColor(dropColorLoc, 0.75f, 0.85f, 0.98f, 1.0f)
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, bgVertCount)
        }

        if (actualSplashCount > 0) {
            if (screenWidth != metaballFboW || screenHeight != metaballFboH) {
                createOrResizeMetaballFbo(screenWidth, screenHeight)
            }

            if (useMetaball) {
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, metaballFboId)
                GLES30.glViewport(0, 0, metaballFboW, metaballFboH)
                GLES30.glClearColor(0f, 0f, 0f, 0f)
                GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

                GLES30.glUseProgram(metaballSplatProgram)
                GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE)

                GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboId)
                GLES30.glEnableVertexAttribArray(0)
                GLES30.glEnableVertexAttribArray(1)
                GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, stride, 0)
                GLES30.glVertexAttribPointer(1, 1, GLES30.GL_FLOAT, false, stride, 8)

                setResolution(splatResLoc, screenWidth.toFloat(), screenHeight.toFloat())
                GLES30.glUniform1f(splatPtBaseLoc,  METABALL_PT_BASE)
                GLES30.glUniform1f(splatPtRangeLoc, METABALL_PT_RANGE)
                GLES30.glDrawArrays(GLES30.GL_POINTS, splashOffset, actualSplashCount)
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
                GLES30.glViewport(0, 0, screenWidth, screenHeight)

                GLES30.glUseProgram(metaballCompositeProgram)
                GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

                GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, metaballTexId)
                GLES30.glUniform1i(compFboTexLoc, 0)
                GLES30.glUniform1f(compThresholdLoc, METABALL_ALPHA_THRESHOLD)

                GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
                GLES30.glDisableVertexAttribArray(0)
                GLES30.glDisableVertexAttribArray(1)

                GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6)
            } else {
                GLES30.glUseProgram(splashProgram)
                GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
                GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, stride, 0)
                GLES30.glVertexAttribPointer(1, 1, GLES30.GL_FLOAT, false, stride, 8)

                setResolution(splashResolutionLoc, screenWidth.toFloat(), screenHeight.toFloat())
                setColor(splashColorLoc, 0.55f, 0.78f, 1.0f, 0.85f)
                GLES30.glUniform1f(splashPtBaseLoc,  SPLASH_PT_BASE)
                GLES30.glUniform1f(splashPtRangeLoc, SPLASH_PT_RANGE)
                GLES30.glDrawArrays(GLES30.GL_POINTS, splashOffset, actualSplashCount)
            }
        }

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
        GLES30.glDisable(GLES30.GL_BLEND)
    }

    fun release() {
        if (vboId != 0) {
            GLES30.glDeleteBuffers(1, intArrayOf(vboId), 0)
            vboId = 0
        }
        if (dropProgram != 0){ GLES30.glDeleteProgram(dropProgram);   dropProgram = 0 }
        if (frontDropProgram != 0) {
            GLES30.glDeleteProgram(frontDropProgram)
            frontDropProgram = 0
        }
        if (splashProgram != 0) { GLES30.glDeleteProgram(splashProgram); splashProgram = 0 }
        if (metaballSplatProgram != 0) {
            GLES30.glDeleteProgram(metaballSplatProgram)
            metaballSplatProgram = 0
        }
        if (metaballCompositeProgram != 0) {
            GLES30.glDeleteProgram(metaballCompositeProgram)
            metaballCompositeProgram = 0
        }
        if (metaballFboId != 0) {
            GLES30.glDeleteFramebuffers(1, intArrayOf(metaballFboId), 0)
            metaballFboId = 0
        }
        if (metaballTexId != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(metaballTexId), 0)
            metaballTexId = 0
        }
        initialized = false
        useMetaball = false
    }

    private fun ensureBuffer(floatCount: Int) {
        if (floatCount > bufferCapacity) {
            val capacity = (floatCount * 1.5f).toInt().coerceAtLeast(4096)
            floatBuffer = ByteBuffer
                .allocateDirect(capacity * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
            bufferCapacity = capacity
        }
    }

    private fun createOrResizeMetaballFbo(w: Int, h: Int) {
        if (metaballFboId != 0) {
            GLES30.glDeleteFramebuffers(1, intArrayOf(metaballFboId), 0)
            metaballFboId = 0
        }
        if (metaballTexId != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(metaballTexId), 0)
            metaballTexId = 0
        }

        val fboIds = IntArray(1)
        val texIds = IntArray(1)
        GLES30.glGenFramebuffers(1, fboIds, 0)
        GLES30.glGenTextures(1, texIds, 0)

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texIds[0])
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA16F,
            w, h, 0, GLES30.GL_RGBA, GLES30.GL_HALF_FLOAT, null
        )
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fboIds[0])
        GLES30.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
            GLES30.GL_TEXTURE_2D, texIds[0], 0
        )

        val status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)
        if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texIds[0])
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA8,
                w, h, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null
            )
            GLES30.glFramebufferTexture2D(
                GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D, texIds[0], 0
            )
            val status2 = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)
            if (status2 != GLES30.GL_FRAMEBUFFER_COMPLETE) {
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
                GLES30.glDeleteFramebuffers(1, fboIds, 0)
                GLES30.glDeleteTextures(1, texIds, 0)
                useMetaball = false
                return
            }
        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)

        metaballFboId = fboIds[0]
        metaballTexId = texIds[0]
        metaballFboW  = w
        metaballFboH  = h
        useMetaball   = true
    }

    private fun setResolution(loc: Int, w: Float, h: Float) {
        GLES30.glUniform2f(loc, w, h)
    }

    private fun setColor(loc: Int, r: Float, g: Float, b: Float, a: Float) {
        GLES30.glUniform4f(loc, r, g, b, a)
    }

    private fun buildProgram(vertSrc: String, fragSrc: String): Int {
        val vs = compileShader(GLES30.GL_VERTEX_SHADER,   vertSrc)
        val fs = compileShader(GLES30.GL_FRAGMENT_SHADER, fragSrc)
        return GLES30.glCreateProgram().also { prog ->
            GLES30.glAttachShader(prog, vs)
            GLES30.glAttachShader(prog, fs)
            GLES30.glLinkProgram(prog)
            val linkStatus = IntArray(1)
            GLES30.glGetProgramiv(prog, GLES30.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                val infoLog = GLES30.glGetProgramInfoLog(prog)
                GLES30.glDeleteProgram(prog)
                GLES30.glDeleteShader(vs)
                GLES30.glDeleteShader(fs)
                error("Failed to link rain program: $infoLog")
            }
            GLES30.glDeleteShader(vs)
            GLES30.glDeleteShader(fs)
        }
    }

    private fun compileShader(type: Int, src: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, src)
        GLES30.glCompileShader(shader)
        val compileStatus = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val infoLog = GLES30.glGetShaderInfoLog(shader)
            GLES30.glDeleteShader(shader)
            error("Failed to compile rain shader: $infoLog")
        }
        return shader
    }
}
