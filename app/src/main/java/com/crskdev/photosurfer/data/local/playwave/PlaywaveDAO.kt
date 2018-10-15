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
    fun insert(playwaveEntity: PlaywaveEntity): Int

    @Delete
    fun delete(playwaveEntity: PlaywaveEntity): Int

    @Update
    fun update(playwaveEntity: PlaywaveEntity): Int

    @Insert
    fun addPhotoToPlaywave(playwaveContentEntity: PlaywaveContentEntity): Int

    @Delete
    fun removePhotoFromPlaywave(playwaveEntity: PlaywaveContentEntity): Int

    @Query(QUERY_ALL)
    fun getPlaywavesLiveData(): LiveData<List<PlaywaveEntity>>

    @Query(QUERY_ALL)
    fun getPlaywaves(): List<PlaywaveEntity>

    @Query(QUERY_ALL)
    fun getPlaywavesWithPhotos(): List<PlaywaveWithPhotos>

    @Query(QUERY_ALL)
    fun getPlaywavesWithPhotosLiveData(): LiveData<List<PlaywaveWithPhotos>>

    @Query(QUERY_BY_ID)
    fun getPlaywave(playwaveId: Int): PlaywaveEntity

    @Query(QUERY_BY_ID)
    fun getPlaywaveLiveData(playwaveId: Int): LiveData<PlaywaveEntity>

    @Query(QUERY_BY_ID)
    fun getPlaywaveWithPhotos(playwaveId: Int): PlaywaveWithPhotos

    @Query(QUERY_BY_ID)
    fun getPlaywaveWithPhotosLiveData(playwaveId: Int): LiveData<PlaywaveWithPhotos>

}