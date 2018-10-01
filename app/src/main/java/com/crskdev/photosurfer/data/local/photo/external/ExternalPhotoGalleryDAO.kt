package com.crskdev.photosurfer.data.local.photo.external

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.contentValuesOf
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

    fun delete(path: String)

}

class ExternalPhotoGalleryDAOImpl(
        private val context: Context,
        private val directory: ExternalDirectory) : ExternalPhotoGalleryDAO {


    override fun getPhotos(): DataSource.Factory<Int, PhotoEntity> =
            ExternalPhotoGalleryDataSourceFactory(context.contentResolver, directory)

    private val photoSurferDir = directory.get()

    private val contentResolver = context.contentResolver

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
            //write first on a temp file
            val tempFile = File.createTempFile("phs", null, context.cacheDir)
            Okio.buffer(Okio.sink(tempFile)).apply {
                writeAll(it)
            }
            //then copy from temp to real file
            val file = File(photoSurferDir, "${photo.id}.jpg")
            Okio.buffer(Okio.sink(file)).apply {
                writeAll(Okio.source(tempFile))
            }
            //setCustomPhotoAttrs(photo, file)
            tempFile.delete()
            //update system media database
            // contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, convertPhotoToRow(photo))
            val galleryScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    .setData(Uri.fromFile(file))
            context.sendBroadcast(galleryScanIntent)
            Unit
        }
    }

    override fun delete(path: String) {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selectionArgs = arrayOf(path)
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        contentResolver.query(uri, projection, MediaStore.Images.Media.DATA + " = ?",
                selectionArgs, null).use { c ->
            if (c?.moveToFirst() == true) {
                val id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                // manually delete here, because on HUAWEI P10 Lite at least, DELETE event on file observer
                // somehow is not triggered, when rely solely on contentResolver#delete
                directory.delete(path)
                contentResolver.delete(ContentUris.withAppendedId(uri, id), null, null)
            }
        }
    }


    private fun setCustomPhotoAttrs(photo: Photo, file: File) {
        val exifInterface = ExifInterface(file.absolutePath)
        with(exifInterface) {
            setAttribute("phs_id", photo.id)
        }
        exifInterface.saveAttributes()
    }

    private fun convertPhotoToRow(photo: Photo): ContentValues =
            contentValuesOf(
                    MediaStore.Images.Media.PICASA_ID to photo.id
            )
}