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

    @Query("SELECT count(*) == 0 FROM collections")
    fun isEmpty(): Boolean

    @Query("SELECT * FROM collections ORDER BY strftime('%s', updatedAt) DESC")
    fun getCollections(): DataSource.Factory<Int, CollectionEntity>

    @Query("SELECT MAX(id) + 1 FROM collections")
    fun getNextIndex(): Int

    @Insert
    fun createCollection(collection: CollectionEntity)

    @Delete
    fun deleteCollection(collection: CollectionEntity)

    @Update
    fun updateCollection(collection: CollectionEntity)

    @Query("SELECT * FROM collections WHERE id=:collectionId")
    fun getCollection(collectionId: Int): CollectionEntity?

    @Query("SELECT * FROM collections ORDER BY  strftime('%s', publishedAt) DESC LIMIT 1")
    fun getLatestCollection(): CollectionEntity?

}