package com.crskdev.photosurfer.data.local.photo

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.crskdev.photosurfer.data.local.DataAccessor

/**
 * Created by Cristian Pela on 19.08.2018.
 */
@Dao
interface PhotoLikeDAO : DataAccessor {

    //TODO fix to lastUpdatedLocal when figure how to make triggers work
    @Query("SELECT * FROM like_photos ORDER BY indexInResponse ASC")
    fun getPhotos(): DataSource.Factory<Int, LikePhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhotos(photos: List<LikePhotoEntity>)

    @Query("SELECT * FROM like_photos WHERE collections LIKE :likeCollectionId")
    fun getPhotosBelongToCollection(likeCollectionId: String): List<LikePhotoEntity>

    @Query("DELETE FROM like_photos")
    fun clear()

    @Query("SELECT MAX(indexInResponse) + 1 FROM like_photos")
    fun getNextIndex(): Int

    @Query("SELECT count(*) == 0 FROM like_photos")
    fun isEmpty(): Boolean

    @Query("SELECT * FROM like_photos WHERE id=:id")
    fun getPhoto(id: String): LikePhotoEntity?

    @Query("SELECT * FROM like_photos WHERE id=:id")
    fun getPhotoLiveData(id: String): LiveData<LikePhotoEntity?>

    @Query("SELECT * FROM like_photos ORDER BY indexInResponse DESC LIMIT 1")
    fun getLastPhoto(): LikePhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun like(photo: LikePhotoEntity)

    @Delete
    fun unlike(photo: LikePhotoEntity)

    @Update
    fun update(photo: LikePhotoEntity): Int
}