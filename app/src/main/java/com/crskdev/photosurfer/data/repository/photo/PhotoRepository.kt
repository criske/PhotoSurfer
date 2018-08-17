package com.crskdev.photosurfer.data.repository.photo

import androidx.annotation.AnyThread
import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.TransactionRunner
import com.crskdev.photosurfer.data.local.photo.PhotoDAO
import com.crskdev.photosurfer.data.local.photo.PhotoEntity
import com.crskdev.photosurfer.data.local.photo.UserPhotoEntity
import com.crskdev.photosurfer.data.remote.download.DownloadManager
import com.crskdev.photosurfer.data.remote.download.DownloadProgress
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.data.remote.photo.PhotoPagingData
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.entities.toDbEntity
import com.crskdev.photosurfer.entities.toPhoto
import com.crskdev.photosurfer.entities.toUserPhotoDbEntity
import retrofit2.Call
import kotlin.math.roundToInt

/**
 * Created by Cristian Pela on 09.08.2018.
 */
interface PhotoRepository : Repository {

    fun getPhotos(username: String?): DataSource.Factory<Int, Photo>

    fun insertPhotos(username: String?, page: Int, callback: Repository.Callback<Unit>? = null)

    fun refresh(username: String? = null)

    fun cancel()

    fun download(photo: Photo, callback: Repository.Callback<DownloadProgress>? = null)

    fun isDownloaded(id: String): Boolean

}

class PhotoRepositoryImpl(
        private val transactional: TransactionRunner,
        private val api: PhotoAPI,
        private val dao: PhotoDAO,
        private val downloadManager: DownloadManager
) : PhotoRepository {

    @Volatile
    private var cancelableApiCall: Call<*>? = null

    companion object {

        private val EMPTY_PHOTO_ENTITY = PhotoEntity().apply {
            id = ""
            createdAt = ""
            updatedAt = ""
            colorString = ""
            urls = ""
            authorId = ""
            authorUsername = ""

        }

        private fun emptyUserPhotoEntity(username: String) = UserPhotoEntity().apply {
            this.username = username
            this.photo = EMPTY_PHOTO_ENTITY
        }
    }


    override fun getPhotos(username: String?): DataSource.Factory<Int, Photo> =
            if (username != null)
                dao.getUserPhotos(username)
                        .mapByPage { page -> page.map { it.photo.toPhoto() } }
            else
                dao.getRandomPhotos()
                        .mapByPage { page -> page.map { it.toPhoto() } }


    //this must be called on the io thread
    override fun insertPhotos(username: String?, page: Int, callback: Repository.Callback<Unit>?) {
        try {
            val call = if (username == null) api.getRandomPhotos(page)
                    .apply { cancelableApiCall = this }
            else
                api.getUserPhotos(username, page).apply { cancelableApiCall = this }

            val response = call.execute()
            response?.apply {
                val headers = headers()
                val pagingData = PhotoPagingData.createFromHeaders(headers)
                if (isSuccessful) {
                    body()?.map {
                        @Suppress("IMPLICIT_CAST_TO_ANY")
                        if (username != null) {
                            it.toUserPhotoDbEntity(username, pagingData, dao.getNextIndexUserPhotos(username))
                        } else {
                            it.toDbEntity(pagingData, dao.getNextIndexRandomPhotos())
                        }
                    }?.apply {
                        if (username != null) {
                            dao.insertUserPhotos(map { it as UserPhotoEntity })
                        } else {
                            dao.insertRandomPhotos(map { it as PhotoEntity })
                        }
                        callback?.onSuccess(Unit)
                    }
                } else {
                    callback?.onError(Error("${code()}:${errorBody()?.string()}"))
                }
            }
        } catch (ex: Exception) {
            callback?.onError(ex)
        }
    }

    override fun download(photo: Photo, callback: Repository.Callback<DownloadProgress>?) {
        try {
            val now = System.currentTimeMillis()
            var start = true
            downloadManager.download(photo) { _, bytesRead, contentLength, done ->
                val passed = System.currentTimeMillis() - now
                if (passed < 500 && done) {
                    callback?.onSuccess(DownloadProgress(100, false, true))
                } else {
                    if (contentLength == -1L) { //indeterminated
                        if (start) {
                            callback?.onSuccess(DownloadProgress.INDETERMINATED_START)
                        } else if (done) {
                            callback?.onSuccess(DownloadProgress.INDETERMINATED_END)
                        }
                    } else {
                        val percent = (bytesRead.toFloat() / contentLength * 100).roundToInt()
                        if (percent % 10 == 0 || percent == 100 || done) // backpressure relief
                            callback?.onSuccess(DownloadProgress(percent, start, percent == 100 || done))
                    }
                    start = false
                }

            }
            callback?.onSuccess(DownloadProgress.NONE)
        } catch (ex: Exception) {
            callback?.onSuccess(DownloadProgress.INDETERMINATED_END)
            callback?.onError(ex)
        }
    }

    override fun isDownloaded(id: String): Boolean = downloadManager.isDownloaded(id)

    override fun refresh(username: String?) {
        transactional {
            if (username == null) {
                if (dao.isEmptyRandomPhotos()) {
                    //force trigger the db InvalidationTracker.Observer
                    dao.insertRandomPhotos(listOf(EMPTY_PHOTO_ENTITY))
                    dao.clearRandomPhotos()
                } else {
                    dao.clearRandomPhotos()
                }
            } else {
                if (dao.isEmptyUserPhotos(username)) {
                    //force trigger the db InvalidationTracker.Observer
                    dao.insertUserPhotos(listOf(emptyUserPhotoEntity(username)))
                    dao.clearUserPhotos(username)
                } else {
                    dao.clearUserPhotos(username)
                }
            }

        }
    }

    @AnyThread
    override fun cancel() {
        cancelableApiCall?.cancel()
        downloadManager.cancel()
    }

}


