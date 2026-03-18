// app/src/main/java/com/example/travel_footprint_android/utils/Constants.kt
package com.example.travel_footprint_android.utils

import android.Manifest

object Constants {

    // 数据库
    const val DATABASE_NAME = "travel_journal.db"
    const val DATABASE_VERSION = 1

    // 日期格式
    const val DATE_FORMAT_FULL = "yyyy-MM-dd HH:mm:ss"
    const val DATE_FORMAT_DATE = "yyyy-MM-dd"
    const val DATE_FORMAT_TIME = "HH:mm"
    const val DATE_FORMAT_FILENAME = "yyyyMMdd_HHmmss"

    // 文件目录
    const val DIR_PHOTOS = "photos"
    const val DIR_THUMBNAILS = "thumbnails"
    const val DIR_EXPORTS = "exports"
    const val DIR_BACKUPS = "backups"

    // 图片尺寸
    const val MAX_PHOTO_WIDTH = 1920
    const val MAX_PHOTO_HEIGHT = 1080
    const val THUMBNAIL_WIDTH = 300
    const val THUMBNAIL_HEIGHT = 300
    const val THUMBNAIL_QUALITY = 80

    // 手绘风格
    const val STYLE_WATERCOLOR = "watercolor"
    const val STYLE_PENCIL = "pencil_sketch"
    const val STYLE_VINTAGE = "vintage_paper"
    const val STYLE_INK = "ink_wash"
    const val STYLE_CRAYON = "crayon"

    // 权限请求码
    const val REQUEST_CODE_LOCATION = 1001
    const val REQUEST_CODE_STORAGE = 1002
    const val REQUEST_CODE_CAMERA = 1003

    // 权限列表
    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val STORAGE_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    // 地图默认参数
    const val DEFAULT_LATITUDE = 39.9042  // 北京
    const val DEFAULT_LONGITUDE = 116.4074
    const val DEFAULT_ZOOM = 12f
}