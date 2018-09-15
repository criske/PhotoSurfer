package com.crskdev.photosurfer.data.local.photo

import androidx.paging.DataSource
import androidx.room.*
import com.crskdev.photosurfer.data.local.DataAccessor


/**
 * Created by Cristian Pela on 09.08.2018.
 */
@Dao
interface PhotoDAO : DataAccessor {

    @Query("SELECT * FROM photos ORDER BY indexInResponse ASC")
    fun getPhotos(): DataSource.Factory<Int, PhotoEntity>

    @Query("SELECT * FROM photos WHERE collections LIKE :likeCollectionId")
    fun getPhotosBelongToCollection(likeCollectionId: String): List<PhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhotos(photos: List<PhotoEntity>)

    @Query("DELETE FROM photos")
    fun clear()

    @Query("SELECT MAX(indexInResponse) + 1 FROM photos")
    fun getNextIndex(): Int

    @Query("SELECT count(*) == 0 FROM photos")
    fun isEmpty(): Boolean

    @Query("SELECT * FROM photos WHERE id=:id")
    fun getPhoto(id: String): PhotoEntity?

    @Update
    fun like(photo: PhotoEntity)

    @Update
    fun update(photo: PhotoEntity): Int
}