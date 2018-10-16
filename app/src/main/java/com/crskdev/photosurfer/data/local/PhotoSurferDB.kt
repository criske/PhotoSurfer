package com.crskdev.photosurfer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crskdev.photosurfer.data.local.collections.CollectionEntity
import com.crskdev.photosurfer.data.local.collections.CollectionPhotoEntity
import com.crskdev.photosurfer.data.local.collections.CollectionsDAO
import com.crskdev.photosurfer.data.local.photo.*
import com.crskdev.photosurfer.data.local.playwave.PlaywaveContentEntity
import com.crskdev.photosurfer.data.local.playwave.PlaywaveEntity
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
            CollectionPhotoEntity::class,
            PlaywaveEntity::class,
            PlaywaveContentEntity::class
        ],
        version = 27,
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
                    .addCallback(callback)
                    .fallbackToDestructiveMigration()
                    .build()
        }

        fun createForTestEnvironment(context: Context): PhotoSurferDB {
            return Room.inMemoryDatabaseBuilder(context, PhotoSurferDB::class.java)
                    .addCallback(callback)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
        }


        private val callback = object : RoomDatabase.Callback() {

            private fun createTrigger(table: String, isInsert: Boolean): String {
                val operation = if (isInsert) "INSERT" else "UPDATE"
                return """
                    CREATE TRIGGER last_updated_local_trigger_$table
                        BEFORE $operation ON $table FOR EACH ROW
                        BEGIN
                            UPDATE $table SET lastUpdatedLocal = datetime('now', 'localtime') WHERE id = NEW.id;
                        END;
                     """.trimIndent()
            }

            override fun onCreate(db: SupportSQLiteDatabase) {
                //TODO figure how trigger get triggered under room/android
//                db.execSQL("PRAGMA recursive_triggers = OFF;")
//                Contract.PHOTO_AND_COLLECTIONS_TABLES.forEach {
//                  db.execSQL(createTrigger(it, false))
//                    //db.execSQL(createTrigger(it, true))
//                }
            }
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

