package com.example.travel_footprint_android.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.travel_footprint_android.MainActivity
import com.example.travel_footprint_android.data.entity.Footprint
import com.example.travel_footprint_android.data.entity.Location
import com.example.travel_footprint_android.data.repository.FootprintRepository
import com.example.travel_footprint_android.domain.service.LocationTracker
import com.example.travel_footprint_android.domain.service.RecordingStateHolder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * RecordingForegroundService - 足迹录制前台服务
 *
 * 在后台持续运行 GPS 定位和计时，即使用户切换页面或退出应用也能保持录制。
 * 通过通知栏显示录制状态，用户可点击通知返回应用。
 */
@AndroidEntryPoint
class RecordingForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "recording_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.example.travel_footprint_android.ACTION_START"
        const val ACTION_PAUSE = "com.example.travel_footprint_android.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.travel_footprint_android.ACTION_RESUME"
        const val ACTION_STOP = "com.example.travel_footprint_android.ACTION_STOP"

        const val EXTRA_FOOTPRINT_ID = "footprint_id"
        const val EXTRA_FOOTPRINT_TITLE = "footprint_title"
        const val EXTRA_FOOTPRINT_JOURNEY_ID = "footprint_journey_id"
        const val EXTRA_FOOTPRINT_DURATION = "footprint_duration"
        const val EXTRA_FOOTPRINT_DISTANCE = "footprint_distance"
        const val EXTRA_FOOTPRINT_SPEED = "footprint_speed"
        const val EXTRA_FOOTPRINT_CALORIES = "footprint_calories"
        const val EXTRA_FOOTPRINT_START_TIME = "footprint_start_time"
        const val EXTRA_LOCATION_INDEX = "location_index"

        /** 便捷方法：创建启动 Service 的 Intent */
        fun startIntent(
            context: Context,
            footprint: Footprint,
            locationIndex: Int,
        ): Intent {
            return Intent(context, RecordingForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_FOOTPRINT_ID, footprint.id)
                putExtra(EXTRA_FOOTPRINT_TITLE, footprint.title)
                putExtra(EXTRA_FOOTPRINT_JOURNEY_ID, footprint.journeyId)
                putExtra(EXTRA_FOOTPRINT_DURATION, footprint.duration)
                putExtra(EXTRA_FOOTPRINT_DISTANCE, footprint.distance)
                putExtra(EXTRA_FOOTPRINT_SPEED, footprint.speed)
                putExtra(EXTRA_FOOTPRINT_CALORIES, footprint.calories)
                putExtra(EXTRA_FOOTPRINT_START_TIME, footprint.startTime.time)
                putExtra(EXTRA_LOCATION_INDEX, locationIndex)
            }
        }

        fun pauseIntent(context: Context): Intent {
            return Intent(context, RecordingForegroundService::class.java).apply {
                action = ACTION_PAUSE
            }
        }

        fun resumeIntent(context: Context): Intent {
            return Intent(context, RecordingForegroundService::class.java).apply {
                action = ACTION_RESUME
            }
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, RecordingForegroundService::class.java).apply {
                action = ACTION_STOP
            }
        }
    }

    @Inject lateinit var locationTracker: LocationTracker
    @Inject lateinit var footprintRepository: FootprintRepository
    @Inject lateinit var stateHolder: RecordingStateHolder

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // 录制状态
    private var footprintId: Long = 0L
    private var footprintTitle: String = ""
    private var journeyId: Long = 0L
    private var locationIndex: Int = 1
    private var startTime: Long = 0L
    private var historicalDuration: Long = 0L
    private var displacementDistance: Double = 0.0
    private var speed: Double = 0.0
    private var calories: Double = 0.0
    private var pausedDuration: Long = 0L
    private var pauseStartTime: Long? = null
    private var lastLatitude: Double? = null
    private var lastLongitude: Double? = null
    private var isRecording = false
    private var isPaused = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                footprintId = intent.getLongExtra(EXTRA_FOOTPRINT_ID, 0L)
                footprintTitle = intent.getStringExtra(EXTRA_FOOTPRINT_TITLE) ?: ""
                journeyId = intent.getLongExtra(EXTRA_FOOTPRINT_JOURNEY_ID, 0L)
                historicalDuration = intent.getLongExtra(EXTRA_FOOTPRINT_DURATION, 0L)
                displacementDistance = intent.getDoubleExtra(EXTRA_FOOTPRINT_DISTANCE, 0.0)
                speed = intent.getDoubleExtra(EXTRA_FOOTPRINT_SPEED, 0.0)
                calories = intent.getDoubleExtra(EXTRA_FOOTPRINT_CALORIES, 0.0)
                val savedStartTime = intent.getLongExtra(EXTRA_FOOTPRINT_START_TIME, 0L)
                locationIndex = intent.getIntExtra(EXTRA_LOCATION_INDEX, 1)

                startTime = if (savedStartTime > 0L) savedStartTime else System.currentTimeMillis()
                pausedDuration = if (historicalDuration > 0L) historicalDuration else 0L
                isRecording = true
                isPaused = false

                // 同步状态到 RecordingStateHolder
                stateHolder.setRecording(true)
                stateHolder.setPaused(false)
                stateHolder.setRecordingFootprintId(footprintId)
                stateHolder.setRecordingFootprintTitle(footprintTitle)
                stateHolder.setRecordingJourneyId(journeyId)

                startForeground(NOTIFICATION_ID, buildNotification("正在记录: $footprintTitle"))
                startGpsTracking()
                startTimerLoop()
            }
            ACTION_PAUSE -> {
                isPaused = true
                pauseStartTime = System.currentTimeMillis()
                locationTracker.stop()
                stateHolder.setPaused(true)
                updateNotification("已暂停: $footprintTitle")
            }
            ACTION_RESUME -> {
                isPaused = false
                pauseStartTime?.let { pauseStart ->
                    pausedDuration += System.currentTimeMillis() - pauseStart
                    pauseStartTime = null
                }
                stateHolder.setPaused(false)
                startGpsTracking()
                updateNotification("正在记录: $footprintTitle")
            }
            ACTION_STOP -> {
                stopRecording()
            }
        }
        return START_STICKY
    }

    private fun startGpsTracking() {
        locationTracker.start(applicationContext) { latitude, longitude ->
            lastLatitude?.let { prevLat ->
                lastLongitude?.let { prevLon ->
                    val distance = calculateDistance(prevLat, prevLon, latitude, longitude)
                    if (distance < 100) {
                        displacementDistance += distance
                        calories = displacementDistance * 60
                        // 将定位点存入数据库
                        serviceScope.launch {
                            try {
                                footprintRepository.addAddress(
                                    Location(
                                        footprintId = footprintId,
                                        latitude = latitude,
                                        longitude = longitude,
                                        index = locationIndex,
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e("RecordingService", "保存定位点失败", e)
                            }
                        }
                    }
                }
            }
            lastLatitude = latitude
            lastLongitude = longitude
        }
    }

    private fun startTimerLoop() {
        serviceScope.launch {
            while (isRecording) {
                if (!isPaused) {
                    val currentTime = System.currentTimeMillis()
                    val durationTime = currentTime - startTime - pausedDuration + historicalDuration
                    if (durationTime > 0) {
                        speed = displacementDistance / (durationTime / 1000.0)
                    }
                    // 持久化到数据库
                    try {
                        footprintRepository.updateFootprintRecordingData(
                            footprintId = footprintId,
                            startTime = if (startTime > 0) Date(startTime) else null,
                            duration = durationTime,
                            distance = displacementDistance,
                            speed = speed,
                            calories = calories,
                        )
                    } catch (e: Exception) {
                        Log.e("RecordingService", "更新录制数据失败", e)
                    }
                    // 同步到 StateHolder
                    stateHolder.updateRecordingData(durationTime, displacementDistance, speed, calories)
                    // 更新通知
                    updateNotification("正在记录: $footprintTitle - ${formatDuration(durationTime)}")
                }
                delay(1000)
            }
        }
    }

    private fun stopRecording() {
        isRecording = false
        locationTracker.stop()
        stateHolder.clearAll()
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRecording = false
        locationTracker.stop()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "足迹录制",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示足迹录制状态"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("足迹记录")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("足迹记录")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        }
    }

    private fun updateNotification(contentText: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(contentText))
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)
        val a = sin(deltaLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun formatDuration(durationMs: Long): String {
        val seconds = durationMs / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, secs)
            else -> String.format("%02d:%02d", minutes, secs)
        }
    }
}
