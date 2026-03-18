// app/src/main/java/com/example/travel_footprint_android/data/dao/MediaDao.kt
package com.example.travel_footprint_android.data.dao

import androidx.room.*
import com.example.travel_footprint_android.data.entity.MediaAttachment
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    @Insert
    suspend fun insertMedia(media: MediaAttachment): Long

    @Insert
    suspend fun insertAllMedia(mediaList: List<MediaAttachment>)

    @Delete
    suspend fun deleteMedia(media: MediaAttachment)

    @Query("DELETE FROM media_attachments WHERE footprintId = :footprintId")
    suspend fun deleteMediaByFootprint(footprintId: Long)

    @Query("SELECT * FROM media_attachments WHERE footprintId = :footprintId ORDER BY createTime")
    suspend fun getMediaByFootprint(footprintId: Long): List<MediaAttachment>

    @Query("SELECT * FROM media_attachments WHERE type = 'photo' ORDER BY createTime DESC")
    fun getAllPhotos(): Flow<List<MediaAttachment>>

    @Query("SELECT * FROM media_attachments WHERE footprintId IN (:footprintIds)")
    suspend fun getMediaByFootprints(footprintIds: List<Long>): List<MediaAttachment>


}