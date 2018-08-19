package com.crskdev.photosurfer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.crskdev.photosurfer.data.local.photo.*

/**
 * Created by Cristian Pela on 09.08.2018.
 */
@Database(
        entities = [PhotoEntity::class, UserPhotoEntity::class, LikePhotoEntity::class],
        version = 4,
        exportSchema = false
)
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
    }

    abstract fun photoDAO(): PhotoDAO
    abstract fun photoUserDAO(): PhotoUserDAO
    abstract fun photoLikeDAO(): PhotoLikeDAO
}

