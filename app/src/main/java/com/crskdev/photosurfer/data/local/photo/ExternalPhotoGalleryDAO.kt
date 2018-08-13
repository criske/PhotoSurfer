package com.crskdev.photosurfer.data.local.photo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import com.crskdev.photosurfer.entities.Photo
import okio.Okio
import okio.Source
import java.io.File
import java.io.FileFilter
import java.io.FileNotFoundException

/**
 * Created by Cristian Pela on 07.08.2018.
 */
interface ExternalPhotoGalleryDAO {

    fun save(photo: Photo, source: Source)

    fun isDownloaded(id: String): Boolean

}

class ExternalPhotoGalleryDAOImpl(private val context: Context) : ExternalPhotoGalleryDAO {

    private val photoSurferDir by lazy {
        val picturesDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val photoSurferDir = File(picturesDir, "PhotoSurfer")
        if (!photoSurferDir.exists()) {
            val mkdir = photoSurferDir.mkdir()
            if (!mkdir) {
                throw FileNotFoundException("Could not create directory $photoSurferDir")
            }
        }
        photoSurferDir
    }

    override fun isDownloaded(id: String): Boolean =
            photoSurferDir.listFiles(FileFilter {
                it.nameWithoutExtension == id
            }).isNotEmpty()


    override fun save(photo: Photo, source: Source) {
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