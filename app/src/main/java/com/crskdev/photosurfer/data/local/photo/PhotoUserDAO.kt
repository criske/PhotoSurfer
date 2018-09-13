package com.crskdev.photosurfer.data.local.photo

import androidx.paging.DataSource
import androidx.room.*
import com.crskdev.photosurfer.data.local.DataAccessor

/**
 * Created by Cristian Pela on 19.08.2018.
 */
@Dao
interface PhotoUserDAO : DataAccessor {

    @Query("SELECT * FROM user_photos ORDER BY indexInResponse ASC")
    fun getPhotos(): DataSource.Factory<Int, UserPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhotos(photos: List<UserPhotoEntity>)

    @Query("DELETE FROM user_photos")
    fun clear()

    @Query("SELECT MAX(indexInResponse) + 1 FROM user_photos")
    fun getNextIndex(): Int

    @Query("SELECT count(*) == 0 FROM user_photos")
    fun isEmpty(): Boolean

    @Update
    fun like(photo: UserPhotoEntity)

    @Query("SELECT * FROM user_photos WHERE id=:id")
    fun getPhoto(id: String): UserPhotoEntity?

    @Update
    fun update(photo: UserPhotoEntity): Int

}