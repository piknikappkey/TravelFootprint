package com.example.travel_footprint_android.presentation.components.opengl_rain

import android.graphics.Bitmap
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Settings
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World
import org.jbox2d.particle.ParticleGroup
import org.jbox2d.particle.ParticleGroupDef
import org.jbox2d.particle.ParticleType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GroupRenderInfo {
    var cohesive: Boolean = false
    var bufferStart: Int = 0
    var particleCount: Int = 0
}

private data class EmitterSlot(
    val laneIndex: Int,
    var group: ParticleGroup? = null,
    var spawnAtMs: Long = 0L,
    var startedAtMs: Long = 0L,
    var durationMs: Long = 0L,
)

class RainSimulation(
    val width: Int,
    val height: Int,
    val proportion: Float = 200f
) {
    companion object {
        const val MAX_PARTICLES = 2800

        private const val MAX_GROUPS = 82

        private const val TOTAL_LIFETIME_MS = 4000L

        private const val SEGMENTS_PER_CORNER = 4

        private const val COHESIVE_SPREAD_SQ = 0.35f * 0.35f

        private const val COHESIVE_MIN_VY = 5f

        private const val MAX_RAIN_GRAVITY_Y = 100.25f
        private const val MAX_RAIN_PRESSURE = 0.024f
        private const val MAX_RAIN_DAMPING = 1.0f
        private const val MAX_RAIN_BASE_VELOCITY = 40f
        private const val MAX_RAIN_VELOCITY_VARIANCE_RATIO = 0.15f
        private const val SPAWN_LANE_MIN_COUNT = 10
        private const val SPAWN_LANE_MAX_COUNT = 16
        private const val SPAWN_LANE_WIDTH_PX = 92
        private const val SPAWN_LANE_JITTER_RATIO = 0.32f
        private const val INITIAL_PREWARM_TOP_RATIO = 0.28f
        private const val INITIAL_PREWARM_SPAN_RATIO = 1.48f
        private const val RESPAWN_TOP_MIN_RATIO = 0.10f
        private const val RESPAWN_TOP_MAX_RATIO = 0.40f
        private const val RESPAWN_LANE_PHASE_RATIO = 0.22f
        private const val SLOT_RESPAWN_DELAY_MIN_MS = 38L
        private const val SLOT_RESPAWN_DELAY_MAX_MS = 60L
        private const val GROUP_LIFETIME_MIN_MS = 4200L
        private const val GROUP_LIFETIME_MAX_MS = 6200L
        private const val GROUP_RECYCLE_BOTTOM_RATIO = 1.30f
        private const val TEXT_COLLISION_ALPHA_THRESHOLD = 32
        private const val TEXT_COLLISION_MIN_RUN_PX = 3

        init {
            Settings.maxPolygonVertices = SEGMENTS_PER_CORNER * 4
        }
    }

    var initOK = false
        private set

    private lateinit var world: World

    private val emitterSlots = ArrayList<EmitterSlot>(MAX_GROUPS)

    private val spawnLaneCount = (width / SPAWN_LANE_WIDTH_PX)
        .coerceIn(SPAWN_LANE_MIN_COUNT, SPAWN_LANE_MAX_COUNT)

    @Volatile
    var pendingCollisionRect: FloatArray? = null

    @Volatile
    var pendingTextCollisions: List<RainTextCollision> = emptyList()

    private var appliedCollisionRect: FloatArray? = null
    private var appliedTextCollisionSignatures: List<Int>? = null
    private var collisionBody: Body? = null

    val particleCount: Int get() = world.particleCount

    val positionBuffer: Array<Vec2> get() = world.particlePositionBuffer

    val velocityBuffer: Array<Vec2> get() = world.particleVelocityBuffer

    private val _groupInfoPool = Array(MAX_GROUPS) { GroupRenderInfo() }
    private var _groupInfoCount = 0

    val groupInfoCount: Int get() = _groupInfoCount

    val groupInfos: Array<GroupRenderInfo> get() = _groupInfoPool

    fun init() {
        synchronized(this) {
            world = World(Vec2(0f, MAX_RAIN_GRAVITY_Y))
            world.setParticlePressureStrength(MAX_RAIN_PRESSURE)
            world.setParticleDamping(MAX_RAIN_DAMPING)
            world.particleRadius  = 6f / proportion
            world.particleMaxCount = MAX_PARTICLES

            syncCollisionBody()

            emitterSlots.clear()
            for (i in 0 until MAX_GROUPS) {
                val slot = EmitterSlot(laneIndex = i % spawnLaneCount)
                activateSlot(slot, now = System.currentTimeMillis(), prewarmIndex = i)
                emitterSlots.add(slot)
            }
            initOK = true
        }
    }

    fun update() {
        if (!initOK) return
        synchronized(this) {
            syncCollisionBody()
            val now = System.currentTimeMillis()

            for (slot in emitterSlots) {
                val activeGroup = slot.group
                if (activeGroup != null && shouldRecycleGroup(activeGroup, slot, now)) {
                    world.destroyParticlesInGroup(activeGroup)
                    slot.group = null
                    slot.spawnAtMs = now + nextRespawnDelayMs()
                }
                if (slot.group == null && now >= slot.spawnAtMs) {
                    activateSlot(slot, now)
                }
            }

            world.step(1f / 120f, 8, 3)

            computeGroupInfos()
        }
    }

    private fun applyCollisionRect() {
        val rect = pendingCollisionRect ?: return
        if (rect.contentEquals(appliedCollisionRect)) return

        val left = rect[0] / proportion
        val top = rect[1] / proportion
        val right = rect[2] / proportion
        val bottom = rect[3] / proportion
        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f
        val hw = (right - left) / 2f
        val hh = (bottom - top) / 2f

        if (hw < Settings.linearSlop || hh < Settings.linearSlop) return

        collisionBody?.let {
            world.destroyBody(it)
            collisionBody = null
        }

        val bodyDef = BodyDef().apply {
            type = BodyType.STATIC
            position.set(cx, cy)
        }
        collisionBody = world.createBody(bodyDef)
        collisionBody!!.createFixture(FixtureDef().apply {
            val cr = (if (rect.size > 4) rect[4] / proportion else 0f)
                .coerceAtMost(minOf(hw, hh))
            shape = PolygonShape().also {
                if (cr > 0.01f && hw - cr > Settings.linearSlop && hh - cr > Settings.linearSlop) {
                    val n = SEGMENTS_PER_CORNER
                    val verts = ArrayList<Vec2>(n * 4)
                    val halfPi = (Math.PI / 2.0).toFloat()
                    for (corner in 0 until 4) {
                        val (ocx, ocy, startAngle) = when (corner) {
                            0 -> Triple(hw - cr, -(hh - cr), -halfPi)
                            1 -> Triple(hw - cr, hh - cr, 0f)
                            2 -> Triple(-(hw - cr), hh - cr, halfPi)
                            else -> Triple(-(hw - cr), -(hh - cr), Math.PI.toFloat())
                        }
                        for (s in 0 until n) {
                            val angle = startAngle + halfPi * s / n
                            verts.add(Vec2(ocx + cr * cos(angle), ocy + cr * sin(angle)))
                        }
                    }
                    it.set(verts.toTypedArray(), verts.size)
                } else {
                    it.setAsBox(hw, hh)
                }
            }
            friction = 0.8f
            restitution = 0.5f
            filter.maskBits = 0b01
            filter.groupIndex = 0b01
        })

        appliedCollisionRect = rect.clone()
        appliedTextCollisionSignatures = null
    }

    private fun syncCollisionBody() {
        when {
            pendingTextCollisions.isNotEmpty() -> applyTextCollision()
            pendingCollisionRect != null -> applyCollisionRect()
            else -> {
                destroyCollisionBody()
                appliedCollisionRect = null
                appliedTextCollisionSignatures = null
            }
        }
    }

    private fun applyTextCollision() {
        val collisions = pendingTextCollisions
        if (collisions.isEmpty()) return
        val signatures = collisions.map { buildTextCollisionSignature(it) }
        if (signatures == appliedTextCollisionSignatures) return

        destroyCollisionBody()

        val body = world.createBody(BodyDef().apply { type = BodyType.STATIC })
        var totalFixtureCount = 0

        for (textCollision in collisions) {
            val bitmap = textCollision.bitmap
            if (bitmap.width <= 0 || bitmap.height <= 0) continue

            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            val sampleStepPx = (bitmap.width / 42).coerceIn(4, 8)

            for (xStart in 0 until bitmap.width step sampleStepPx) {
                val xEnd = minOf(bitmap.width, xStart + sampleStepPx)
                var runStart = -1
                for (y in 0 until bitmap.height) {
                    val opaque = isColumnOpaque(pixels, bitmap.width, xStart, xEnd, y)
                    if (opaque) {
                        if (runStart < 0) runStart = y
                    } else if (runStart >= 0) {
                        totalFixtureCount += addTextFixture(body, textCollision, xStart, xEnd, runStart, y)
                        runStart = -1
                    }
                }
                if (runStart >= 0) {
                    totalFixtureCount += addTextFixture(body, textCollision, xStart, xEnd, runStart, bitmap.height)
                }
            }
        }

        if (totalFixtureCount == 0) {
            world.destroyBody(body)
            appliedTextCollisionSignatures = null
            return
        }

        collisionBody = body
        appliedTextCollisionSignatures = signatures
        appliedCollisionRect = null
    }

    private fun addTextFixture(
        body: Body,
        textCollision: RainTextCollision,
        xStart: Int,
        xEnd: Int,
        yStart: Int,
        yEnd: Int,
    ): Int {
        val runHeight = yEnd - yStart
        if (runHeight < TEXT_COLLISION_MIN_RUN_PX) return 0

        val halfWidth = ((xEnd - xStart) * 0.5f) / proportion
        val halfHeight = (runHeight * 0.5f) / proportion
        if (halfWidth < Settings.linearSlop || halfHeight < Settings.linearSlop) return 0

        val centerX = (textCollision.left + xStart + ((xEnd - xStart) * 0.5f)) / proportion
        val centerY = (textCollision.top + yStart + (runHeight * 0.5f)) / proportion
        val shape = PolygonShape().apply {
            setAsBox(halfWidth, halfHeight, Vec2(centerX, centerY), 0f)
        }
        body.createFixture(FixtureDef().apply {
            this.shape = shape
            friction = 0.8f
            restitution = 0.15f
            filter.maskBits = 0b01
            filter.groupIndex = 0b01
        })
        return 1
    }

    private fun isColumnOpaque(
        pixels: IntArray,
        bitmapWidth: Int,
        xStart: Int,
        xEnd: Int,
        y: Int,
    ): Boolean {
        val rowOffset = y * bitmapWidth
        for (x in xStart until xEnd) {
            val alpha = pixels[rowOffset + x] ushr 24
            if (alpha >= TEXT_COLLISION_ALPHA_THRESHOLD) return true
        }
        return false
    }

    private fun buildTextCollisionSignature(textCollision: RainTextCollision): Int {
        val bitmap = textCollision.bitmap
        var result = bitmap.generationId
        result = 31 * result + bitmap.width
        result = 31 * result + bitmap.height
        result = 31 * result + textCollision.left.toBits()
        result = 31 * result + textCollision.top.toBits()
        return result
    }

    private fun destroyCollisionBody() {
        collisionBody?.let {
            world.destroyBody(it)
            collisionBody = null
        }
    }

    private fun computeGroupInfos() {
        val positions = world.particlePositionBuffer
        val velocities = world.particleVelocityBuffer
        var infoIdx = 0

        for (slot in emitterSlots) {
            val g = slot.group ?: continue
            val start = g.bufferIndex
            val cnt = g.particleCount
            if (cnt == 0) continue

            var sumX = 0f; var sumY = 0f; var sumVy = 0f
            for (i in start until start + cnt) {
                sumX += positions[i].x
                sumY += positions[i].y
                sumVy += velocities[i].y
            }
            val cx = sumX / cnt
            val cy = sumY / cnt
            val avy = sumVy / cnt

            var maxDistSq = 0f
            for (i in start until start + cnt) {
                val dx = positions[i].x - cx
                val dy = positions[i].y - cy
                val distSq = dx * dx + dy * dy
                if (distSq > maxDistSq) maxDistSq = distSq
            }

            val info = _groupInfoPool[infoIdx++]
            info.cohesive = maxDistSq < COHESIVE_SPREAD_SQ && avy > COHESIVE_MIN_VY
            info.bufferStart = start
            info.particleCount = cnt
        }
        _groupInfoCount = infoIdx
    }

    private fun activateSlot(slot: EmitterSlot, now: Long, prewarmIndex: Int? = null) {
        val durationMs = nextGroupLifetimeMs()
        val progress = prewarmIndex?.let { (it + Random.nextFloat()) / MAX_GROUPS.toFloat() }
        val startedAt = progress?.let { now - (durationMs * it).toLong() } ?: now
        val (xPos, yPos) = computeSpawnPosition(slot.laneIndex, progress)
        slot.group = createGroup(xPos, yPos)
        slot.startedAtMs = startedAt
        slot.durationMs = durationMs
        slot.spawnAtMs = 0L
    }

    private fun shouldRecycleGroup(group: ParticleGroup, slot: EmitterSlot, now: Long): Boolean {
        if (group.particleCount <= 0) return true
        if (now - slot.startedAtMs >= slot.durationMs) return true
        return isGroupBelowRecycleLine(group)
    }

    private fun isGroupBelowRecycleLine(group: ParticleGroup): Boolean {
        val positions = world.particlePositionBuffer
        val start = group.bufferIndex
        val end = start + group.particleCount
        if (end <= start) return true
        var minY = Float.MAX_VALUE
        for (i in start until end) {
            if (positions[i].y < minY) minY = positions[i].y
        }
        return minY * proportion > height * GROUP_RECYCLE_BOTTOM_RATIO
    }

    private fun createGroup(xPos: Float, yPos: Float): ParticleGroup {
        val def = ParticleGroupDef()
        val shape = PolygonShape()
        shape.setAsBox(width / (300f * proportion), width / (40f * proportion))
        def.shape = shape
        def.flags = ParticleType.b2_waterParticle

        val baseV = MAX_RAIN_BASE_VELOCITY
        val varV  = baseV * MAX_RAIN_VELOCITY_VARIANCE_RATIO
        def.linearVelocity.set(
            0f,
            baseV + (Random.nextFloat() - 0.5f) * 2f * varV
        )

        def.position.set(xPos, yPos)

        return world.createParticleGroup(def)
    }

    private fun computeSpawnPosition(laneIndex: Int, progress: Float?): Pair<Float, Float> {
        val x = nextSpawnXForLane(laneIndex)
        val y = if (progress != null) {
            height * (-INITIAL_PREWARM_TOP_RATIO + progress * INITIAL_PREWARM_SPAN_RATIO)
        } else {
            val lanePhase = ((laneIndex / spawnLaneCount.toFloat()) + Random.nextFloat() * 0.35f) % 1f
            val baseRatio = RESPAWN_TOP_MIN_RATIO +
                Random.nextFloat() * (RESPAWN_TOP_MAX_RATIO - RESPAWN_TOP_MIN_RATIO)
            -height * (baseRatio + lanePhase * RESPAWN_LANE_PHASE_RATIO)
        }
        return Pair(x, y / proportion)
    }

    private fun nextSpawnXForLane(laneIndex: Int): Float {
        val spawnWidth = width * 0.5f
        val laneWidth = spawnWidth / spawnLaneCount
        val laneCenter = (laneIndex + 0.5f) * laneWidth
        val jitter = (Random.nextFloat() - 0.5f) * laneWidth * SPAWN_LANE_JITTER_RATIO
        return (laneCenter + jitter).coerceIn(0f, spawnWidth) / proportion
    }

    private fun nextRespawnDelayMs(): Long {
        return SLOT_RESPAWN_DELAY_MIN_MS +
            Random.nextLong(SLOT_RESPAWN_DELAY_MAX_MS - SLOT_RESPAWN_DELAY_MIN_MS + 1L)
    }

    private fun nextGroupLifetimeMs(): Long {
        return GROUP_LIFETIME_MIN_MS +
            Random.nextLong(GROUP_LIFETIME_MAX_MS - GROUP_LIFETIME_MIN_MS + 1L)
    }
}
