package com.crskdev.photosurfer.data.local.photo

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.crskdev.photosurfer.data.local.DataAccessor

/**
 * Created by Cristian Pela on 22.08.2018.
 */
@Dao
interface PhotoSearchDAO : DataAccessor {

    @Query("SELECT * FROM search_photos ORDER BY indexInResponse ASC")
    fun getPhotos(): DataSource.Factory<Int, SearchPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhotos(photos: List<SearchPhotoEntity>)

    @Query("SELECT * FROM search_photos WHERE collections LIKE :likeCollectionId")
    fun getPhotosBelongToCollection(likeCollectionId: String): List<SearchPhotoEntity>

    @Query("DELETE FROM search_photos")
    fun clear()

    @Query("SELECT MAX(indexInResponse) + 1 FROM search_photos")
    fun getNextIndex(): Int

    @Query("SELECT count(*) == 0 FROM search_photos")
    fun isEmpty(): Boolean

    @Query("SELECT * FROM search_photos WHERE id=:id")
    fun getPhoto(id: String): SearchPhotoEntity?

    @Query("SELECT * FROM search_photos WHERE id=:id")
    fun getPhotoLiveData(id: String): LiveData<SearchPhotoEntity?>

    @Update
    fun like(photo: SearchPhotoEntity)

    @Update
    fun update(photo: SearchPhotoEntity): Int

    @Query("SELECT * FROM search_photos ORDER BY indexInResponse DESC LIMIT 1")
    fun getLastPhoto(): SearchPhotoEntity?

}