package com.crskdev.photosurfer.data.local.photo

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crskdev.photosurfer.data.local.DataAccessor

/**
 * Created by Cristian Pela on 19.08.2018.
 */
@Dao
interface PhotoUserDAO : DataAccessor {

    @Query("SELECT * FROM user_photos WHERE authorUsername=:userName ORDER BY indexInResponse ASC")
    fun getPhotos(userName: String): DataSource.Factory<Int, UserPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhotos(photos: List<UserPhotoEntity>)

    @Query("DELETE FROM user_photos WHERE authorUsername=:userName")
    fun clear(userName: String)

    @Query("DELETE FROM user_photos")
    fun clear()

    @Query("SELECT MAX(indexInResponse) + 1 FROM user_photos WHERE  authorUsername=:userName")
    fun getNextIndex(userName: String): Int

    @Query("SELECT count(*) == 0 FROM user_photos WHERE authorUsername=:userName")
    fun isEmpty(userName: String): Boolean

    @Query("UPDATE user_photos SET likedByMe=:like WHERE id=:id")
    fun like(id: String, like: Boolean)

    @Query("SELECT * FROM user_photos WHERE id=:id")
    fun getPhoto(id: String): UserPhotoEntity

}