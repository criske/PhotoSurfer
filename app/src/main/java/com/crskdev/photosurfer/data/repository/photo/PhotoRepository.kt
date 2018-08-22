package com.crskdev.photosurfer.data.repository.photo

import androidx.annotation.AnyThread
import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.DaoManager
import com.crskdev.photosurfer.data.local.photo.*
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
        daoManager: DaoManager,
        private val authTokenStorage: AuthTokenStorage,
        private val staleDataTrackSupervisor: StaleDataTrackSupervisor,
        private val api: PhotoAPI,
        private val downloadManager: DownloadManager,
        private val scheduledWorkService: ScheduledWorkService
) : PhotoRepository {

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
            id = ""
            createdAt = ""
            updatedAt = ""
            colorString = ""
            urls = ""
            authorId = ""
            authorUsername = ""
        }

    }

    @Volatile
    private var cancelableApiCall: Call<*>? = null

    private val daoPhotos: PhotoDAO = daoManager.getDao(Contract.TABLE_PHOTOS)
    private val daoLikes: PhotoLikeDAO = daoManager.getDao(Contract.TABLE_LIKE_PHOTOS)
    private val daoUserPhotos: PhotoUserDAO = daoManager.getDao(Contract.TABLE_USER_PHOTOS)
    private val transactional = daoManager.transactionRunner()


    override fun getPhotos(username: String?): DataSource.Factory<Int, Photo> {
        return if (username != null)
            daoUserPhotos.getPhotos(username)
                    .mapByPage { page ->
                        staleDataTrackSupervisor.runStaleDataCheckForTable(Contract.TABLE_USER_PHOTOS)
                        page.map { it.toPhoto() }
                    }
        else
            daoPhotos.getPhotos()
                    .mapByPage { page ->
                        staleDataTrackSupervisor.runStaleDataCheckForTable(Contract.TABLE_PHOTOS)
                        page.map { it.toPhoto() }
                    }
    }

    override fun getLikedPhotos(): DataSource.Factory<Int, Photo> {
        return daoLikes.getPhotos()
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
                            InsertPhotoAction.Type.LIKE -> it.toLikePhotoDbEntity(pagingData, daoLikes.getNextIndex())
                            InsertPhotoAction.Type.RANDOM -> it.toDbEntity(pagingData, daoPhotos.getNextIndex())
                            InsertPhotoAction.Type.USER -> it.toUserPhotoDbEntity(pagingData, daoUserPhotos
                                    .getNextIndex(insertPhotoAction.extra.toString()))
                        }
                    }?.apply {
                        when (insertPhotoAction.type) {
                            InsertPhotoAction.Type.LIKE -> daoLikes.insertPhotos(map { it as LikePhotoEntity })
                            InsertPhotoAction.Type.RANDOM -> daoPhotos.insertPhotos(this)
                            InsertPhotoAction.Type.USER -> daoUserPhotos.insertPhotos(map { it as UserPhotoEntity })
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
                if (daoPhotos.isEmpty()) {
                    //force trigger the db InvalidationTracker.Observer
                    daoPhotos.insertPhotos(listOf(EMPTY_PHOTO_ENTITY))
                    daoPhotos.clear()
                } else {
                    daoPhotos.clear()
                }
            } else {
                if (daoUserPhotos.isEmpty(username)) {
                    //force trigger the db InvalidationTracker.Observer
                    daoUserPhotos.insertPhotos(listOf(emptyUserPhotoEntity(username)))
                    daoUserPhotos.clear(username)
                } else {
                    daoUserPhotos.clear(username)
                }
            }

        }
    }

    override fun like(photo: Photo, callback: Repository.Callback<Boolean>) {
        if(!authTokenStorage.hasToken()){
            callback.onError(Error("You need to login"), true)
            return
        }
        transactional {
            daoPhotos.like(photo.id, photo.likedByMe)
            daoUserPhotos.like(photo.id, photo.likedByMe)
            if (!daoLikes.isEmpty()) { // we doing nothing unless there already fetched the likes from server
                if (photo.likedByMe) {
                    daoLikes.like(photo.toLikePhotoDbEntity(daoLikes.getNextIndex()))
                } else {
                    daoLikes.unlike(photo.toLikePhotoDbEntity(-1))
                }
            }
        }
        callback.onSuccess(photo.likedByMe)
        scheduledWorkService.schedule(WorkData(Tag(WorkType.LIKE, photo.id), "id" to photo.id, "likedByMe" to photo.likedByMe))
    }

    @AnyThread
    override fun cancel() {
        cancelableApiCall?.cancel()
        downloadManager.cancel()
    }

    override fun clearAll() {
        transactional {
            daoPhotos.clear()
            daoUserPhotos.clear()
        }
    }


}


