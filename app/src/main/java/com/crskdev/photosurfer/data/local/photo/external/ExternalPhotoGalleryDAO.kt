package com.crskdev.photosurfer.data.local.photo.external

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.photo.PhotoEntity
import com.crskdev.photosurfer.entities.Photo
import okio.Okio
import okio.Source
import java.io.File
import java.io.FileFilter

/**
 * Created by Cristian Pela on 07.08.2018.
 */
interface ExternalPhotoGalleryDAO {

    fun save(photo: Photo, source: Source)

    fun isDownloaded(id: String): Boolean

    fun getPhotos(): DataSource.Factory<Int, PhotoEntity>

}

class ExternalPhotoGalleryDAOImpl(
        private val context: Context,
        private val directory: ExternalDirectory) : ExternalPhotoGalleryDAO {


    override fun getPhotos(): DataSource.Factory<Int, PhotoEntity> =
            ExternalPhotoGalleryDataSourceFactory(context.contentResolver, directory)

    private val photoSurferDir = directory.get()

    override fun isDownloaded(id: String): Boolean {
        //we call this function here cause we assume the user has given permission to write files
        directory.createIfNotExists()
        return photoSurferDir.listFiles(FileFilter {
            it.nameWithoutExtension == id
        }).isNotEmpty()
    }


    override fun save(photo: Photo, source: Source) {
        //we call this function here cause we assume the user has given permission to write files
        directory.createIfNotExists()
        source.use {
            val file = File(photoSurferDir, "${photo.id}.jpg")
            val sink = Okio.buffer(Okio.sink(file))
            sink.writeAll(it)
            //
            val galleryScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    .setData(Uri.fromFile(file))
            context.sendBroadcast(galleryScanIntent)
            Unit
        }
    }

}