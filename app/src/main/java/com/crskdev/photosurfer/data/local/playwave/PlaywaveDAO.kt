package com.crskdev.photosurfer.data.local.playwave

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Created by Cristian Pela on 14.10.2018.
 */
@Dao
interface PlaywaveDAO {

    companion object {
        private const val QUERY_ALL = "SELECT * FROM playwaves ORDER BY id ASC"
        private const val QUERY_BY_ID = "SELECT * FROM playwaves WHERE id=:playwaveId ORDER BY id ASC"
    }

    @Insert
    fun insert(playwaveEntity: PlaywaveEntity): Long

    @Delete
    fun delete(playwaveEntity: PlaywaveEntity)

    @Query("DELETE FROM playwaves WHERE id=:id")
    fun delete(id: Int)

    @Update
    fun update(playwaveEntity: PlaywaveEntity)

    @Insert
    fun addPhotoToPlaywave(playwaveContentEntity: PlaywaveContentEntity)

    @Delete
    fun removePhotoFromPlaywave(playwaveEntity: PlaywaveContentEntity)

    @Query("DELETE FROM playwave_contents  WHERE  playwaveId=:playwaveId AND photoId=:photoId")
    fun removePhotoFromPlaywave(playwaveId: Int, photoId: String)

    @Query(QUERY_ALL)
    fun getPlaywavesLiveData(): LiveData<List<PlaywaveEntity>>

    @Query(QUERY_ALL)
    fun getPlaywaves(): List<PlaywaveEntity>

    @Query(QUERY_ALL)
    @Transaction
    fun getPlaywavesWithPhotos(): List<PlaywaveWithPhotosEntity>

    @Query(QUERY_ALL)
    @Transaction
    fun getPlaywavesWithPhotosLiveData(): LiveData<List<PlaywaveWithPhotosEntity>>

    @Query(QUERY_BY_ID)
    fun getPlaywave(playwaveId: Int): PlaywaveEntity

    @Query(QUERY_BY_ID)
    fun getPlaywaveLiveData(playwaveId: Int): LiveData<PlaywaveEntity>

    @Query(QUERY_BY_ID)
    @Transaction
    fun getPlaywaveWithPhotos(playwaveId: Int): PlaywaveWithPhotosEntity

    @Query(QUERY_BY_ID)
    @Transaction
    fun getPlaywaveWithPhotosLiveData(playwaveId: Int): LiveData<PlaywaveWithPhotosEntity>

    @Query("SELECT COUNT(*) FROM playwave_contents WHERE playwaveId =:playwaveId")
    fun getPlaywaveSize(playwaveId: Int): Int

    @Query("UPDATE playwaves SET size =:size WHERE id =:playwaveId")
    fun setPlaywaveSize(playwaveId: Int, size: Int)

    @Query("""
        SELECT pw.id, pw.title, pw.size, pwc.photoId FROM playwaves as pw
            LEFT JOIN playwave_contents as pwc ON pw.id == pwc.playwaveId AND pwc.photoId =:photoId
    """)
    fun getPlaywavesForPhoto(photoId: String): LiveData<List<PlaywaveForPhotoEntity>>

}