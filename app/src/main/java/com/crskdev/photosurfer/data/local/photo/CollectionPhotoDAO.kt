package com.crskdev.photosurfer.data.local.photo

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.crskdev.photosurfer.data.local.DataAccessor
import com.crskdev.photosurfer.data.local.collections.CollectionPhotoEntity

/**
 * Created by Cristian Pela on 05.09.2018.
 */
@Dao
interface CollectionPhotoDAO : DataAccessor {

    @Query("SELECT * FROM collection_photos ORDER BY indexInResponse ASC")
    fun getPhotos(): DataSource.Factory<Int, CollectionPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhotos(photos: List<CollectionPhotoEntity>)

    @Query("SELECT * FROM collection_photos WHERE collections LIKE :likeCollectionId")
    fun getPhotosBelongToCollection(likeCollectionId: String): List<CollectionPhotoEntity>

    @Query("DELETE FROM collection_photos")
    fun clear()

    @Query("SELECT MAX(indexInResponse) + 1 FROM collection_photos")
    fun getNextIndex(): Int

    @Query("SELECT count(*) == 0 FROM collection_photos")
    fun isEmpty(): Boolean

    @Query("SELECT * FROM collection_photos WHERE id=:id")
    fun getPhoto(id: String): CollectionPhotoEntity?

    @Query("SELECT * FROM collection_photos WHERE id=:id")
    fun getPhotoLiveData(id: String): LiveData<CollectionPhotoEntity?>

    @Update
    fun like(photo: CollectionPhotoEntity)

    @Update
    fun update(photo: CollectionPhotoEntity): Int

    @Query("SELECT * FROM collection_photos ORDER BY indexInResponse DESC LIMIT 1")
    fun getLastPhoto(): CollectionPhotoEntity?

    @Query("DELETE FROM collection_photos WHERE id=:id")
    fun delete(id: String)

}