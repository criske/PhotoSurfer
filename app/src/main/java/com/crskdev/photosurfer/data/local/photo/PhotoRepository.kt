package com.crskdev.photosurfer.data.local.photo

import androidx.annotation.AnyThread
import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.TransactionRunner
import com.crskdev.photosurfer.data.remote.RequestLimit
import com.crskdev.photosurfer.data.remote.download.DownloadManager
import com.crskdev.photosurfer.data.remote.download.DownloadProgress
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.data.remote.photo.PhotoPagingData
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.entities.toDbEntity
import com.crskdev.photosurfer.entities.toPhoto
import retrofit2.Call
import kotlin.math.roundToInt

/**
 * Created by Cristian Pela on 09.08.2018.
 */
interface PhotoRepository {

    interface Callback {
        fun onSuccess(data: Any? = null) = Unit
        fun onError(error: Throwable)
    }

    fun getPhotos(): DataSource.Factory<Int, Photo>

    fun insertPhotos(page: Int, callback: Callback? = null)

    fun refresh()

    fun cancel()

    fun download(id: String, callback: Callback? = null)
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

        private val EMPTY_ENTITY = PhotoEntity().apply {
            id = ""
            createdAt = ""
            updatedAt = ""
            colorString = ""
            urls = ""
            authorId = ""
            authorUsername = ""

        }
    }

    override fun getPhotos(): DataSource.Factory<Int, Photo> = dao.getPhotos()
            .mapByPage { page -> page.map { it.toPhoto() } }

    //this must be called on the io thread
    override fun insertPhotos(page: Int, callback: PhotoRepository.Callback?) {
        try {
            val response = api.getPhotos(page).apply { cancelableApiCall = this }.execute()
            response?.apply {
                val headers = headers()
                val pagingData = PhotoPagingData.createFromHeaders(headers)
                val requestLimit = RequestLimit.createFromHeaders(headers)
                if (requestLimit.isLimitReached) {
                    callback?.onError(Error("Request limit reached: $requestLimit"))
                } else if (isSuccessful) {
                    body()?.map {
                        it.toDbEntity(pagingData, dao.getNextIndex())
                    }?.apply {
                        dao.insertPhotos(this)
                        callback?.onSuccess()
                    }
                } else {
                    callback?.onError(Error("${code()}:${errorBody()?.string()}"))
                }
            }
        } catch (ex: Exception) {
            callback?.onError(ex)
        }

    }


    override fun download(id: String, callback: PhotoRepository.Callback?) {
        val now = System.currentTimeMillis()
        var start = true
        downloadManager.download(id) { isStartingValue, bytesRead, contentLength, done ->
            val passed = System.currentTimeMillis() - now
            if (passed < 500 && done) {
                callback?.onSuccess(DownloadProgress(100, false, true))
            } else {
                if (contentLength == -1L) { //indeterminated
                    if(start){
                        callback?.onSuccess(DownloadProgress.INDETERMINATED_START)
                    }else if(done){
                        callback?.onSuccess(DownloadProgress.INDETERMINATED_END)
                    }
                }else {
                    val percent = (bytesRead.toFloat() / contentLength * 100).roundToInt()
                    if (percent % 20 == 0 || percent == 100) // backpressure relief
                        callback?.onSuccess(DownloadProgress(percent, start , percent == 100))
                }
                start = false
            }

        }
        callback?.onSuccess(DownloadProgress.NONE)
    }

    override fun refresh() {
        transactional {
            if (dao.isEmpty()) {
                //force trigger the db InvalidationTracker.Observer
                dao.insertPhotos(listOf(EMPTY_ENTITY))
                dao.clear()
            } else {
                dao.clear()
            }

        }
    }

    @AnyThread
    override fun cancel() {
        cancelableApiCall?.cancel()
        downloadManager.cancel()
    }

}


