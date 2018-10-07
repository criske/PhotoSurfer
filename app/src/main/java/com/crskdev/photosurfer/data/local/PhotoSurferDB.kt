package com.crskdev.photosurfer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crskdev.photosurfer.data.local.collections.CollectionEntity
import com.crskdev.photosurfer.data.local.photo.CollectionPhotoDAO
import com.crskdev.photosurfer.data.local.collections.CollectionPhotoEntity
import com.crskdev.photosurfer.data.local.collections.CollectionsDAO
import com.crskdev.photosurfer.data.local.photo.*
import com.crskdev.photosurfer.data.local.track.StaleDataTackDAO
import com.crskdev.photosurfer.data.local.track.StaleDataTrackEntity
import com.crskdev.photosurfer.data.local.user.UserDAO
import com.crskdev.photosurfer.data.local.user.UserEntity

/**
 * Created by Cristian Pela on 09.08.2018.
 */
@Database(
        entities = [
            PhotoEntity::class,
            UserPhotoEntity::class,
            LikePhotoEntity::class,
            SearchPhotoEntity::class,
            UserEntity::class,
            StaleDataTrackEntity::class,
            CollectionEntity::class,
            CollectionPhotoEntity::class
        ],
        version = 15,
        exportSchema = false
)
@TypeConverters(DataTypeConverters::class)
abstract class PhotoSurferDB : RoomDatabase() {
    companion object {
        fun create(context: Context, useInMemory: Boolean): PhotoSurferDB {
            val databaseBuilder = if (useInMemory) {
                Room.inMemoryDatabaseBuilder(context, PhotoSurferDB::class.java)
            } else {
                Room.databaseBuilder(context, PhotoSurferDB::class.java, "photo-surfer.db")
            }
            return databaseBuilder
                    .fallbackToDestructiveMigration()
                    .build()
        }

        fun createForTestEnvironment(context: Context): PhotoSurferDB {
            return Room.inMemoryDatabaseBuilder(context, PhotoSurferDB::class.java)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
        }
    }

    abstract fun photoDAO(): PhotoDAO
    abstract fun photoUserDAO(): PhotoUserDAO
    abstract fun photoLikeDAO(): PhotoLikeDAO
    abstract fun photoSearchDAO(): PhotoSearchDAO
    abstract fun collectionPhotoDAO(): CollectionPhotoDAO

    abstract fun collectionsDAO(): CollectionsDAO

    abstract fun userDAO(): UserDAO
    abstract fun staleDataTrackDAO(): StaleDataTackDAO
}

