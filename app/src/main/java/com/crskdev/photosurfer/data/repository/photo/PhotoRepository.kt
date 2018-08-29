package com.crskdev.photosurfer.data.repository.photo

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.photo.PhotoDAOFacade
import com.crskdev.photosurfer.data.local.track.StaleDataTrackSupervisor
import com.crskdev.photosurfer.data.remote.APICallDispatcher
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.download.DownloadManager
import com.crskdev.photosurfer.data.remote.download.DownloadProgress
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.data.remote.photo.PhotoJSON
import com.crskdev.photosurfer.data.remote.PagingData
import com.crskdev.photosurfer.data.remote.photo.SearchedPhotosJSON
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.entities.*
import com.crskdev.photosurfer.services.ScheduledWorkService
import com.crskdev.photosurfer.services.Tag
import com.crskdev.photosurfer.services.WorkData
import com.crskdev.photosurfer.services.WorkType
import com.crskdev.photosurfer.services.executors.ExecutorsManager
import retrofit2.Call
import kotlin.math.roundToInt

/**
 * Created by Cristian Pela on 09.08.2018.
 */
interface PhotoRepository : Repository {

    fun getPhotos(repositoryAction: RepositoryAction): DataSource.Factory<Int, Photo>

    fun insertPhotos(repositoryAction: RepositoryAction, page: Int, callback: Repository.Callback<Unit>? = null)

    fun refresh(username: String? = null)

    fun cancel()

    fun download(photo: Photo, callback: Repository.Callback<DownloadProgress>? = null)

    fun isDownloaded(id: String): Boolean

    fun like(photo: Photo, callback: Repository.Callback<Boolean>)

    fun clearAll()

    fun clear(repositoryAction: RepositoryAction)

}

class RepositoryAction(val type: Type, vararg val extras: Any) {
    companion object {
        val TRENDING = RepositoryAction(Type.TRENDING)
    }

    enum class Type {
        LIKE, TRENDING, USER_PHOTOS, SEARCH
    }
}

class PhotoRepositoryImpl(
        executorsManager: ExecutorsManager,
        private val daoPhotoFacade: PhotoDAOFacade,
        private val authTokenStorage: AuthTokenStorage,
        private val staleDataTrackSupervisor: StaleDataTrackSupervisor,
        private val apiCallDispatcher: APICallDispatcher,
        private val api: PhotoAPI,
        private val downloadManager: DownloadManager,
        private val scheduledWorkService: ScheduledWorkService
) : PhotoRepository {

    private val uiExecutor = executorsManager.types[ExecutorsManager.Type.UI]!!
    private val ioExecutor = executorsManager.types[ExecutorsManager.Type.NETWORK]!!
    private val diskExecutor = executorsManager.types[ExecutorsManager.Type.DISK]!!

    override fun getPhotos(repositoryAction: RepositoryAction): DataSource.Factory<Int, Photo> {
        val table = when (repositoryAction.type) {
            RepositoryAction.Type.LIKE -> Contract.TABLE_LIKE_PHOTOS
            RepositoryAction.Type.TRENDING -> Contract.TABLE_PHOTOS
            RepositoryAction.Type.USER_PHOTOS -> Contract.TABLE_USER_PHOTOS
            RepositoryAction.Type.SEARCH -> Contract.TABLE_SEARCH_PHOTOS
        }
        return daoPhotoFacade.getPhotos(table).mapByPage { page ->
            staleDataTrackSupervisor.runStaleDataCheckForTable(table)
            page.map { it.toPhoto() }
        }
    }

    //this must be called on the io thread
    override fun insertPhotos(repositoryAction: RepositoryAction, page: Int, callback: Repository.Callback<Unit>?) {
        uiExecutor.execute {
            apiCallDispatcher.cancel()
        }
        ioExecutor.execute {
            try {
                val response = if (repositoryAction.type == RepositoryAction.Type.SEARCH) {
                    apiCallDispatcher { api.getSearchedPhotos(repositoryAction.extras[0].toString(), page) }
                } else {
                    apiCallDispatcher {
                        when (repositoryAction.type) {
                            RepositoryAction.Type.LIKE -> api.getLikedPhotos(repositoryAction.extras[0].toString(), page)
                            RepositoryAction.Type.TRENDING -> api.getRandomPhotos(page)
                            RepositoryAction.Type.USER_PHOTOS -> api.getUserPhotos(repositoryAction.extras[0].toString(), page)
                            RepositoryAction.Type.SEARCH -> throw Error()
                        }
                    }
                }
                response.apply {
                    val headers = headers()
                    val pagingData = PagingData.createFromHeaders(headers)
                    if (isSuccessful) {
                        diskExecutor.execute {
                            @Suppress("UNCHECKED_CAST")
                            val body = body()?.let {
                                if (repositoryAction.type == RepositoryAction.Type.SEARCH)
                                    (it as SearchedPhotosJSON).results else it as List<PhotoJSON>
                            }
                            body?.map {
                                when (repositoryAction.type) {
                                    RepositoryAction.Type.LIKE -> it.toLikePhotoDbEntity(pagingData, daoPhotoFacade.getNextIndex(Contract.TABLE_LIKE_PHOTOS))
                                    RepositoryAction.Type.TRENDING -> it.toDbEntity(pagingData, daoPhotoFacade.getNextIndex(Contract.TABLE_PHOTOS))
                                    RepositoryAction.Type.USER_PHOTOS -> it.toUserPhotoDbEntity(pagingData, daoPhotoFacade.getNextIndex(Contract.TABLE_USER_PHOTOS))
                                    RepositoryAction.Type.SEARCH -> it.toSearchPhotoDbEntity(pagingData, daoPhotoFacade.getNextIndex(Contract.TABLE_SEARCH_PHOTOS))
                                }
                            }?.apply {
                                when (repositoryAction.type) {
                                    RepositoryAction.Type.LIKE -> daoPhotoFacade.insertPhotos(Contract.TABLE_LIKE_PHOTOS, this, page == 1)
                                    RepositoryAction.Type.TRENDING -> daoPhotoFacade.insertPhotos(Contract.TABLE_PHOTOS, this, page == 1)
                                    RepositoryAction.Type.USER_PHOTOS -> daoPhotoFacade.insertPhotos(Contract.TABLE_USER_PHOTOS, this, page == 1)
                                    RepositoryAction.Type.SEARCH -> daoPhotoFacade.insertPhotos(Contract.TABLE_SEARCH_PHOTOS, this, page == 1)
                                }
                            }
                            uiExecutor.execute {
                                callback?.onSuccess(Unit)
                            }

                        }
                    } else {
                        uiExecutor.execute {
                            callback?.onError(Error("${code()}:${errorBody()?.string()}"))
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                uiExecutor.execute {
                    callback?.onError(ex)
                }
            }
        }
    }

    override fun download(photo: Photo, callback: Repository.Callback<DownloadProgress>?) {
        uiExecutor.execute {
            apiCallDispatcher.cancel()
        }
        ioExecutor.execute {
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
                                uiExecutor.execute {
                                    callback?.onSuccess(DownloadProgress.INDETERMINATED_START)
                                }

                            } else if (done) {
                                uiExecutor.execute {
                                    callback?.onSuccess(DownloadProgress.INDETERMINATED_END)
                                }

                            }
                        } else {
                            val percent = (bytesRead.toFloat() / contentLength * 100).roundToInt()
                            if (percent % 10 == 0 || percent == 100 || done){// backpressure relief
                                uiExecutor.execute {
                                    callback?.onSuccess(DownloadProgress(percent, start, percent == 100 || done))
                                }
                            }
                        }
                        start = false
                    }

                }
                uiExecutor.execute{
                    callback?.onSuccess(DownloadProgress.NONE)
                }
            } catch (ex: Exception) {
                uiExecutor.execute {
                    callback?.onSuccess(DownloadProgress.INDETERMINATED_END)
                    callback?.onError(ex)
                }
            }
        }
    }

    override fun isDownloaded(id: String): Boolean = downloadManager.isDownloaded(id)

    override fun refresh(username: String?) {
        //todo remove this?
//        val table = if (username == null) Contract.TABLE_PHOTOS else Contract.TABLE_USER_PHOTOS
//        daoPhotoFacade.refresh(table)
    }

    override fun like(photo: Photo, callback: Repository.Callback<Boolean>) {
        if (!authTokenStorage.hasToken()) {
            ioExecutor.execute {
                callback.onError(Error("You need to login"), true)
            }
            return
        }
        diskExecutor.execute {
            daoPhotoFacade.like(photo.id, photo.likedByMe)
            ioExecutor.execute {
                callback.onSuccess(photo.likedByMe)
            }
        }
        scheduledWorkService.schedule(WorkData(Tag(WorkType.LIKE, photo.id), "id" to photo.id,
                "likedByMe" to photo.likedByMe))
    }

    @MainThread
    override fun cancel() {
        uiExecutor.execute {
            apiCallDispatcher.cancel()
            downloadManager.cancel()
        }
    }

    override fun clearAll() {
        diskExecutor.execute {
            daoPhotoFacade.clear()
        }
    }

    override fun clear(repositoryAction: RepositoryAction) {
        diskExecutor.execute {
            val table = when (repositoryAction.type) {
                RepositoryAction.Type.LIKE -> Contract.TABLE_LIKE_PHOTOS
                RepositoryAction.Type.TRENDING -> Contract.TABLE_PHOTOS
                RepositoryAction.Type.USER_PHOTOS -> Contract.TABLE_USER_PHOTOS
                RepositoryAction.Type.SEARCH -> Contract.TABLE_SEARCH_PHOTOS
            }
            daoPhotoFacade.clear(table)
        }
    }


}


