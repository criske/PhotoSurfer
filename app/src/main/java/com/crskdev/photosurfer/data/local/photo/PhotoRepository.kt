package com.crskdev.photosurfer.data.local.photo

import androidx.annotation.AnyThread
import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.TransactionRunner
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.data.remote.photo.PhotoJSON
import com.crskdev.photosurfer.data.remote.photo.PhotoPagingData
import com.crskdev.photosurfer.entities.Photo
import com.crskdev.photosurfer.entities.toDbEntity
import com.crskdev.photosurfer.entities.toPhoto
import retrofit2.Call
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by Cristian Pela on 09.08.2018.
 */
interface PhotoRepository {

    interface Callback {
        fun onSuccess() = Unit
        fun onError(error: Throwable)
    }

    fun getPhotos(): DataSource.Factory<Int, Photo>

    fun insertPhotos(page: Int, callback: Callback? = null)

    fun refresh()

    fun cancel()
}

class PhotoRepositoryImpl(
        private val transactional: TransactionRunner,
        private val api: PhotoAPI,
        private val dao: PhotoDAO
) : PhotoRepository {

    @Volatile
    private var apiCall: Call<List<PhotoJSON>>? = null

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
            apiCall = api.getPhotos(page)
            val response = apiCall?.execute()
            response?.apply {
                val pagingData = PhotoPagingData.createFromHeader(headers())
                if (isSuccessful) {
                    body()?.map {
                        it.toDbEntity(pagingData, dao.getNextIndex())
                    }?.apply {
                        dao.insertPhotos(this)
                    }
                } else {
                    callback?.onError(Error("${code()}:${errorBody()?.string()}"))
                }
            }
        } catch (ex: Exception) {
            callback?.onError(ex)
        }

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
        apiCall?.cancel()
    }

}
