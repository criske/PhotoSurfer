package com.crskdev.photosurfer.data.local.collections

import androidx.paging.DataSource
import androidx.room.*
import com.crskdev.photosurfer.data.local.DataAccessor

/**
 * Created by Cristian Pela on 30.08.2018.
 */
@Dao
interface CollectionsDAO : DataAccessor {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCollections(collections: List<CollectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCollectionPhotos(photos: List<CollectionPhotoEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addPhotosToCollection(collectionPhotos: List<CollectionsCollectionPhotoEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addPhotoToCollection(collectionPhoto: CollectionsCollectionPhotoEntity)

    @Query("SELECT MAX(indexInResponse) + 1 FROM collection_photos")
    fun getNextCollectionPhotoIndex(): Int

    @Query("SELECT count(*) == 0 FROM collection_photos")
    fun isEmptyCollectionPhotos(): Boolean

    @Query("SELECT count(*) == 0 FROM collections")
    fun isEmptyCollections(): Boolean

    @Query("SELECT * FROM collections ORDER BY strftime('%s', updatedAt) DESC")
    fun getCollections(): DataSource.Factory<Int, CollectionEntity>

    @Query("SELECT MAX(id) + 1 FROM collections")
    fun getNextCollectionIndex(): Int

    @Query("""
        SELECT cp.* FROM collection_photos AS cp, collections AS c, collections_collection_photos AS ccp
        WHERE c.id==ccp.collection_id AND cp.id==ccp.photo_id AND ccp.collection_id == :collectionId
        ORDER BY cp.indexInResponse
    """)
    fun getCollectionPhotos(collectionId: Long): DataSource.Factory<Int, CollectionPhotoEntity>

    @Query("SELECT * FROM collection_photos WHERE id=:id")
    fun getPhoto(id: String): CollectionPhotoEntity?

    @Update
    fun like(photo: CollectionPhotoEntity)

    @Insert
    fun createCollection(collection: CollectionEntity)

    @Delete
    fun deleteCollection(collection: CollectionEntity)

    @Update
    fun updateCollection(collection: CollectionEntity)

    @Query("SELECT * FROM collections WHERE id=:collectionId")
    fun getCollection(collectionId: Long): CollectionEntity?

    @Query("SELECT * FROM collections ORDER BY  strftime('%s', publishedAt) DESC LIMIT 1")
    fun getLatestCollection(): CollectionEntity?

}