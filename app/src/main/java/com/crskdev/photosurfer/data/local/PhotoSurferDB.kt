package com.crskdev.photosurfer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.crskdev.photosurfer.data.local.photo.PhotoDAO
import com.crskdev.photosurfer.data.local.photo.PhotoEntity
import com.crskdev.photosurfer.data.local.photo.UserPhotoEntity

/**
 * Created by Cristian Pela on 09.08.2018.
 */
@Database(
        entities = [PhotoEntity::class, UserPhotoEntity::class],
        version = 2,
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
}

