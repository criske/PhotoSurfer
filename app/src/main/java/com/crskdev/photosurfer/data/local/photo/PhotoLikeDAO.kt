package com.crskdev.photosurfer.data.local.photo

import androidx.paging.DataSource
import androidx.room.*
import com.crskdev.photosurfer.data.local.DataAccessor

/**
 * Created by Cristian Pela on 19.08.2018.
 */
@Dao
interface PhotoLikeDAO:DataAccessor {

    @Query("SELECT * FROM like_photos ORDER BY indexInResponse DESC")
    fun getPhotos(): DataSource.Factory<Int, LikePhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhotos(photos: List<LikePhotoEntity>)

    @Query("DELETE FROM like_photos")
    fun clear()

    @Query("SELECT MAX(indexInResponse) + 1 FROM like_photos")
    fun getNextIndex(): Int

    @Query("SELECT count(*) == 0 FROM like_photos")
    fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun like(photo: LikePhotoEntity)

    @Delete
    fun unlike(photo: LikePhotoEntity)
}