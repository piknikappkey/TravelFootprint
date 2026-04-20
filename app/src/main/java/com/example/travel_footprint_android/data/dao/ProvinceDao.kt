// app/src/main/java/com/example/travel_footprint_android/data/dao/ProvinceDao.kt
package com.example.travel_footprint_android.data.dao

import androidx.room.*
import com.example.travel_footprint_android.data.entity.Province
import kotlinx.coroutines.flow.Flow

@Dao
interface ProvinceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvince(province: Province)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvinces(provinces: List<Province>)

    @Query("SELECT * FROM provinces ORDER BY sortOrder, name")
    fun getAllProvinces(): Flow<List<Province>>

    @Query("SELECT * FROM provinces WHERE adcode = :adcode")
    suspend fun getProvinceByAdcode(adcode: String): Province?

    @Query("SELECT * FROM provinces WHERE name LIKE '%' || :keyword || '%'")
    suspend fun searchProvinces(keyword: String): List<Province>

    @Query("SELECT COUNT(*) FROM provinces")
    suspend fun getProvinceCount(): Int

    @Query("DELETE FROM provinces")
    suspend fun deleteAllProvinces()
}