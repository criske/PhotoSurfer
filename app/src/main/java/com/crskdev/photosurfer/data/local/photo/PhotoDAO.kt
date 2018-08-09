package com.crskdev.photosurfer.data.local.photo

import androidx.paging.DataSource
import androidx.room.*


/**
 * Created by Cristian Pela on 09.08.2018.
 */
@Dao
interface PhotoDAO {

    @Query("SELECT * FROM photos ORDER BY indexInResponse ASC")
    fun getPhotos(): DataSource.Factory<Int, PhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhotos(photos: List<PhotoEntity>)

    @Query("DELETE FROM photos")
    fun clear()

    @Query("SELECT MAX(indexInResponse) + 1 FROM photos")
    fun getNextIndex(): Int
}