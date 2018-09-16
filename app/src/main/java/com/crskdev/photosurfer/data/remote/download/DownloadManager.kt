package com.crskdev.photosurfer.data.remote.download

import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.photo.PhotoEntity
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.util.safeSet
import com.crskdev.photosurfer.data.local.photo.external.ExternalPhotoGalleryDAO
import com.crskdev.photosurfer.data.remote.APICallDispatcher
import okio.Source
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Cristian Pela on 11.08.2018.
 */
class DownloadManager(
        private val progressListenerRegistrar: ProgressListenerRegistrar,
        private val photoDownloader: PhotoDownloader,
        private val externalPhotoGalleryDAO: ExternalPhotoGalleryDAO) {


    private class MockPhotoDownloader(private val reg: ProgressListenerRegistrar) : PhotoDownloader {

        private val isCanceled = AtomicBoolean(false)
        private var isIndeterminate = true

        override fun data(photo: Photo): Source? {
            //prepare
            var count = 0L
            var lastTime = System.currentTimeMillis()
            val max = 500L
            reg.progressListener?.update(true, count, len(max), false)
            isCanceled.safeSet(false)

            //
            while (count <= max && !isCanceled.get()) {
                if (isCanceled.get()) {
                    break
                }
                val now = System.currentTimeMillis()
                if (now - lastTime >= 100L) {
                    reg.progressListener?.update(false, count, len(max), count >= max)
                    count++
                    lastTime = now
                }
            }
            if (count < max)
                reg.progressListener?.update(false, count, len(max), true)
            isCanceled.safeSet(false)
            return null
        }

        override fun cancel() {
            isCanceled.safeSet(true)
        }

        @Suppress("ConstantConditionIf")
        private fun len(value: Long): Long = if (isIndeterminate) -1 else value

    }

    companion object {
        private val registrar = object : ProgressListenerRegistrar {
            override var progressListener: ProgressListener? = null
        }
        private val externalPhotoGalleryDAO = object : ExternalPhotoGalleryDAO {
            override fun getPhotos(): DataSource.Factory<Int, PhotoEntity> {
                TODO("not implemented")
            }
            override fun isDownloaded(id: String): Boolean = false
            override fun save(photo: Photo, source: Source) = Unit
        }
        val MOCK = DownloadManager(
                registrar,
                MockPhotoDownloader(registrar),
                externalPhotoGalleryDAO
        )
    }

    fun download(photo: Photo, progress: ((Boolean, Long, Long, Boolean) -> Unit)) {
        val progressListener = ProgressListener { isStartingValue, bytesRead, contentLength, done ->
            progress(isStartingValue, bytesRead, contentLength, done)
            if (done) {
                progressListenerRegistrar.progressListener = null
            }
        }
        progressListenerRegistrar.progressListener = progressListener
        photoDownloader.data(photo)?.let { externalPhotoGalleryDAO.save(photo, it) }
    }

    fun isDownloaded(id: String): Boolean = externalPhotoGalleryDAO.isDownloaded(id)

    fun cancel() {
        photoDownloader.cancel()
    }


}

interface PhotoDownloader {

    fun data(photo: Photo): Source?

    fun cancel()

}

class PhotoDownloaderImpl(
        private val apiCallDispatcher: APICallDispatcher,
        private val photoAPI: PhotoAPI) : PhotoDownloader {

    override fun data(photo: Photo): Source? {
        val response = apiCallDispatcher { photoAPI.download(photo.id) }
        if (!response.isSuccessful) {
            throw Exception(response.errorBody()?.string())
        }
        return response.body()?.source()
    }

    override fun cancel() {
        apiCallDispatcher.cancel()
    }

}