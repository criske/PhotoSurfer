package com.crskdev.photosurfer.services

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import com.crskdev.photosurfer.presentation.Photo
import okio.Okio
import okio.Source
import java.io.File
import java.io.FileNotFoundException

/**
 * Created by Cristian Pela on 07.08.2018.
 */
interface PhotoSaver {

    fun save(photo: Photo, source: Source)

    fun save(id: String, source: Source)

}

class GalleryPhotoSaver(private val context: Context) : PhotoSaver {
    override fun save(id: String, source: Source) {
        source.use {
            val picturesDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val photoSurferDir = File(picturesDir, "PhotoSurfer")
            if (!photoSurferDir.exists()) {
                val mkdir = photoSurferDir.mkdir()
                if(!mkdir){
                    throw FileNotFoundException("Could not create directory $photoSurferDir")
                }
            }
            val file = File(photoSurferDir, "${id}.jpg")
            val sink = Okio.buffer(Okio.sink(file))
            sink.writeAll(it)

            val galleryScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    .setData(Uri.fromFile(file))
            context.sendBroadcast(galleryScanIntent)
            Unit
        }

    }

    override fun save(photo: Photo, source: Source) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}