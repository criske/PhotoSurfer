package com.crskdev.photosurfer.data.local.photo

import androidx.paging.DataSource
import androidx.room.*


/**
 * Created by Cristian Pela on 09.08.2018.
 */
@Dao
interface PhotoDAO {

    @Query("SELECT * FROM photos ORDER BY indexInResponse ASC")
    fun getRandomPhotos(): DataSource.Factory<Int, PhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRandomPhotos(photos: List<PhotoEntity>)

    @Query("DELETE FROM photos")
    fun clearRandomPhotos()

    @Query("SELECT MAX(indexInResponse) + 1 FROM photos")
    fun getNextIndexRandomPhotos(): Int

    @Query("SELECT count(*) == 0 FROM photos")
    fun isEmptyRandomPhotos(): Boolean

    @Query("SELECT * FROM user_photos WHERE username=:userName ORDER BY indexInResponse ASC")
    fun getUserPhotos(userName: String): DataSource.Factory<Int, UserPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserPhotos(photos: List<UserPhotoEntity>)

    @Query("DELETE FROM user_photos WHERE username=:userName")
    fun clearUserPhotos(userName: String)

    @Query("SELECT MAX(indexInResponse) + 1 FROM user_photos WHERE  username=:userName")
    fun getNextIndexUserPhotos(userName: String): Int

    @Query("SELECT count(*) == 0 FROM user_photos WHERE username=:userName")
    fun isEmptyUserPhotos(userName: String): Boolean

    @Query("UPDATE photos SET likedByMe=:like WHERE id=:id")
    fun likeRandom(id: String, like: Boolean)

    @Query("UPDATE user_photos SET likedByMe=:like WHERE id=:id")
    fun likeUserPhoto(id: String, like: Boolean)

    @Query("SELECT * FROM photos WHERE id=:id")
    fun getPhoto(id: String): PhotoEntity

    @Query("SELECT * FROM user_photos WHERE id=:id")
    fun getUserPhoto(id: String): UserPhotoEntity

}