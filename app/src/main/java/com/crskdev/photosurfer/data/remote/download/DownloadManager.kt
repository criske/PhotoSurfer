package com.crskdev.photosurfer.data.remote.download

import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.services.PhotoSaver
import okio.Source
import retrofit2.Call

/**
 * Created by Cristian Pela on 11.08.2018.
 */
class DownloadManager(
        private val progressListenerRegistrar: ProgressListenerRegistrar,
        private val photoDownloader: PhotoDownloader,
        private val photoSaver: PhotoSaver) {


    private class MockPhotoDownloader(private val reg: ProgressListenerRegistrar) : PhotoDownloader {

        override fun data(id: String): Source? {
            var count = 0L
            var lastTime = System.currentTimeMillis()
            val max = 100L
            reg.progressListener?.update(true, count, max, false)
            while (count <= max) {
                val now = System.currentTimeMillis()
                if (now - lastTime >= 100L) {
                    reg.progressListener?.update(false, count, max, count >= max)
                    count++
                    lastTime = now
                }
            }
            reg.progressListener?.update(false, max, max, true)
            return null
        }

        override fun cancel() = Unit

    }

    companion object {
        private val registrar = object : ProgressListenerRegistrar {
            override var progressListener: ProgressListener? = null
        }
        private val photoSaver = object : PhotoSaver {
            override fun save(photo: Photo, source: Source) = Unit

            override fun save(id: String, source: Source) = Unit

        }
        val MOCK = DownloadManager(
                registrar,
                MockPhotoDownloader(registrar),
                photoSaver
        )
    }

    fun download(id: String, progress: ((Boolean, Long, Long, Boolean) -> Unit)) {
        val progressListener = ProgressListener { isStartingValue, bytesRead, contentLength, done ->
            progress(isStartingValue, bytesRead, contentLength, done)
            if (done) {
                progressListenerRegistrar.progressListener = null
            }
        }
        progressListenerRegistrar.progressListener = progressListener

        photoDownloader.data(id)?.let { photoSaver.save(id, it) }
    }

    fun cancel(){
        photoDownloader.cancel()
    }

}

interface PhotoDownloader {

    fun data(id: String): Source?

    fun cancel()

}

class PhotoDownloaderImpl(private val photoAPI: PhotoAPI) : PhotoDownloader {


    @Volatile
    private var call: Call<*>? = null

    override fun data(id: String): Source? {
        val response = photoAPI.download(id).apply { call = this }.execute()
        return response.body()?.source()
    }

    override fun cancel() {
        call?.takeIf { !it.isCanceled || !it.isExecuted }?.cancel()
    }


}