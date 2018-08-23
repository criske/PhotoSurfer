package com.crskdev.photosurfer.data.repository.photo

import androidx.annotation.AnyThread
import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.photo.PhotoDAOFacade
import com.crskdev.photosurfer.data.local.track.StaleDataTrackSupervisor
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.download.DownloadManager
import com.crskdev.photosurfer.data.remote.download.DownloadProgress
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.data.remote.photo.PhotoPagingData
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.entities.*
import com.crskdev.photosurfer.services.ScheduledWorkService
import com.crskdev.photosurfer.services.Tag
import com.crskdev.photosurfer.services.WorkData
import com.crskdev.photosurfer.services.WorkType
import retrofit2.Call
import kotlin.math.roundToInt

/**
 * Created by Cristian Pela on 09.08.2018.
 */
interface PhotoRepository : Repository {

    fun getPhotos(username: String?): DataSource.Factory<Int, Photo>

    fun getLikedPhotos(): DataSource.Factory<Int, Photo>

    fun insertPhotos(insertPhotoAction: InsertPhotoAction, page: Int, callback: Repository.Callback<Unit>? = null)

    fun refresh(username: String? = null)

    fun cancel()

    fun download(photo: Photo, callback: Repository.Callback<DownloadProgress>? = null)

    fun isDownloaded(id: String): Boolean

    fun like(photo: Photo, callback: Repository.Callback<Boolean>)

    fun clearAll()

}

data class InsertPhotoAction(val type: Type, val extra: Any? = null) {
    enum class Type {
        LIKE, RANDOM, USER
    }
}

class PhotoRepositoryImpl(
        private val daoPhotoFacade: PhotoDAOFacade,
        private val authTokenStorage: AuthTokenStorage,
        private val staleDataTrackSupervisor: StaleDataTrackSupervisor,
        private val api: PhotoAPI,
        private val downloadManager: DownloadManager,
        private val scheduledWorkService: ScheduledWorkService
) : PhotoRepository {


    @Volatile
    private var cancelableApiCall: Call<*>? = null

    override fun getPhotos(username: String?): DataSource.Factory<Int, Photo> {
        val table = if (username == null) Contract.TABLE_PHOTOS else Contract.TABLE_USER_PHOTOS
        return daoPhotoFacade.getPhotos(table).mapByPage { page ->
            staleDataTrackSupervisor.runStaleDataCheckForTable(table)
            page.map { it.toPhoto() }
        }
    }

    override fun getLikedPhotos(): DataSource.Factory<Int, Photo> {
        return daoPhotoFacade.getPhotos(Contract.TABLE_LIKE_PHOTOS)
                .mapByPage { page ->
                    staleDataTrackSupervisor.runStaleDataCheckForTable(Contract.TABLE_LIKE_PHOTOS)
                    page.map { it.toPhoto() }
                }
    }


    //this must be called on the io thread
    override fun insertPhotos(insertPhotoAction: InsertPhotoAction, page: Int, callback: Repository.Callback<Unit>?) {
        try {
            val call = when (insertPhotoAction.type) {
                InsertPhotoAction.Type.LIKE -> api.getLikedPhotos(insertPhotoAction.extra.toString(), page)
                InsertPhotoAction.Type.RANDOM -> api.getRandomPhotos(page)
                InsertPhotoAction.Type.USER -> api.getUserPhotos(insertPhotoAction.extra.toString(), page)
            }.apply { cancelableApiCall = this }

            val response = call.execute()
            response?.apply {
                val headers = headers()
                val pagingData = PhotoPagingData.createFromHeaders(headers)
                if (isSuccessful) {
                    body()?.map {
                        when (insertPhotoAction.type) {
                            InsertPhotoAction.Type.LIKE -> it.toLikePhotoDbEntity(pagingData, daoPhotoFacade.getNextIndex(Contract.TABLE_LIKE_PHOTOS))
                            InsertPhotoAction.Type.RANDOM -> it.toDbEntity(pagingData, daoPhotoFacade.getNextIndex(Contract.TABLE_PHOTOS))
                            InsertPhotoAction.Type.USER -> it.toUserPhotoDbEntity(pagingData, daoPhotoFacade.getNextIndex(Contract.TABLE_USER_PHOTOS))
                        }
                    }?.apply {
                        when (insertPhotoAction.type) {
                            InsertPhotoAction.Type.LIKE -> daoPhotoFacade.insertPhotos(Contract.TABLE_LIKE_PHOTOS, this, page == 1)
                            InsertPhotoAction.Type.RANDOM -> daoPhotoFacade.insertPhotos(Contract.TABLE_PHOTOS, this, page == 1)
                            InsertPhotoAction.Type.USER -> daoPhotoFacade.insertPhotos(Contract.TABLE_USER_PHOTOS, this, page == 1)
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
        val table = if (username == null) Contract.TABLE_PHOTOS else Contract.TABLE_USER_PHOTOS
        daoPhotoFacade.refresh(table)
    }

    override fun like(photo: Photo, callback: Repository.Callback<Boolean>) {
        if (!authTokenStorage.hasToken()) {
            callback.onError(Error("You need to login"), true)
            return
        }
        daoPhotoFacade.like(photo.id, photo.likedByMe)
        callback.onSuccess(photo.likedByMe)
        scheduledWorkService.schedule(WorkData(Tag(WorkType.LIKE, photo.id), "id" to photo.id, "likedByMe" to photo.likedByMe))
    }

    @AnyThread
    override fun cancel() {
        cancelableApiCall?.cancel()
        downloadManager.cancel()
    }

    override fun clearAll() {
        daoPhotoFacade.clear()
    }


}


