package com.crskdev.photosurfer.data.repository.photo

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.photo.PhotoDAOFacade
import com.crskdev.photosurfer.data.local.photo.external.ExternalPhotoGalleryDAO
import com.crskdev.photosurfer.data.local.track.StaleDataTrackSupervisor
import com.crskdev.photosurfer.data.remote.APICallDispatcher
import com.crskdev.photosurfer.data.remote.PagingData
import com.crskdev.photosurfer.data.remote.download.DownloadManager
import com.crskdev.photosurfer.data.remote.download.DownloadProgress
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.data.remote.photo.PhotoJSON
import com.crskdev.photosurfer.data.remote.photo.SearchedPhotosJSON
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.entities.*
import com.crskdev.photosurfer.services.executors.ExecutorType
import com.crskdev.photosurfer.services.executors.ExecutorsManager
import com.crskdev.photosurfer.services.schedule.ScheduledWorkManager
import com.crskdev.photosurfer.services.schedule.Tag
import com.crskdev.photosurfer.services.schedule.WorkData
import com.crskdev.photosurfer.services.schedule.WorkType
import com.crskdev.photosurfer.util.runOn
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

/**
 * Created by Cristian Pela on 09.08.2018.
 */
interface PhotoRepository : Repository {

    fun getPhotos(repositoryAction: RepositoryAction): DataSource.Factory<Int, Photo>

    fun getPhotoLiveData(id: String): LiveData<Photo>

    fun getSavedPhotos(): DataSource.Factory<Int, Photo>

    fun fetchAndSavePhotos(repositoryAction: RepositoryAction, callback: Repository.Callback<Unit>? = null)

    fun refresh(username: String? = null)

    fun cancel()

    fun download(photo: Photo, callback: Repository.Callback<DownloadProgress>? = null)

    fun isDownloaded(id: String): Boolean

    fun like(photo: Photo, callback: Repository.Callback<Boolean>? = null)

    fun clearAll()

    fun clear(repositoryAction: RepositoryAction)

    fun delete(photo: Photo)

}

class RepositoryAction(val type: Type, vararg val extras: Any) {
    companion object {
        val TRENDING = RepositoryAction(Type.TRENDING)
        val NONE = RepositoryAction(Type.NONE)
    }

    enum class Type {
        LIKE, TRENDING, USER_PHOTOS, SEARCH, NONE
    }
}

class PhotoRepositoryImpl(
        executorsManager: ExecutorsManager,
        private val daoPhotoFacade: PhotoDAOFacade,
        private val daoExternalPhotoGalleryDAO: ExternalPhotoGalleryDAO,
        private val apiCallDispatcher: APICallDispatcher,
        private val api: PhotoAPI,
        private val downloadManager: DownloadManager,
        private val scheduledWorkService: ScheduledWorkManager
) : PhotoRepository {

    private val uiExecutor = executorsManager.types[ExecutorType.UI]!!
    private val ioExecutor = executorsManager.types[ExecutorType.NETWORK]!!
    private val diskExecutor = executorsManager.types[ExecutorType.DISK]!!

    override fun getPhotos(repositoryAction: RepositoryAction): DataSource.Factory<Int, Photo> {
        val table = when (repositoryAction.type) {
            RepositoryAction.Type.LIKE -> Contract.TABLE_LIKE_PHOTOS
            RepositoryAction.Type.TRENDING -> Contract.TABLE_PHOTOS
            RepositoryAction.Type.USER_PHOTOS -> Contract.TABLE_USER_PHOTOS
            RepositoryAction.Type.SEARCH -> Contract.TABLE_SEARCH_PHOTOS
            else -> throw Exception("Unsupported Action")
        }
        return daoPhotoFacade.getPhotos(table).mapByPage { page ->
            page.map {
                Log.d(this.javaClass.simpleName, "${it.authorFullName} Liked: ${it.likedByMe}  Updated: ${DISPLAY_DATE_FORMATTER.format(it.lastUpdatedLocal)} ")
                it.toPhoto()
            }
        }
    }

    override fun getSavedPhotos(): DataSource.Factory<Int, Photo> =
            daoExternalPhotoGalleryDAO.getPhotos().mapByPage { page ->
                page.map { it.toPhoto() }
            }


    //this must be called on the io thread
    override fun fetchAndSavePhotos(repositoryAction: RepositoryAction, callback: Repository.Callback<Unit>?) {
        //TODO do a smarter way to cancel your requests maybe group them by repository action
//        uiExecutor {
//            apiCallDispatcher.cancel()
//        }
        ioExecutor.execute {
            try {
                val lastPhoto = when (repositoryAction.type) {
                    RepositoryAction.Type.LIKE -> daoPhotoFacade.getLastPhoto(Contract.TABLE_LIKE_PHOTOS)
                    RepositoryAction.Type.TRENDING -> daoPhotoFacade.getLastPhoto(Contract.TABLE_PHOTOS)
                    RepositoryAction.Type.USER_PHOTOS -> daoPhotoFacade.getLastPhoto(Contract.TABLE_USER_PHOTOS)
                    RepositoryAction.Type.SEARCH -> daoPhotoFacade.getLastPhoto(Contract.TABLE_SEARCH_PHOTOS)
                    else -> throw Exception("Unsupported Action")
                }
                if (lastPhoto != null && lastPhoto.pagingData?.next == null) {
                    //bail out if no more pages
                    return@execute
                }
                val page = lastPhoto?.pagingData?.next ?: 1
                val response = if (repositoryAction.type == RepositoryAction.Type.SEARCH) {
                    //repositoryAction.extras[0] - USERNAME
                    apiCallDispatcher { api.getSearchedPhotos(repositoryAction.extras[0].toString(), page) }
                } else {
                    apiCallDispatcher {
                        when (repositoryAction.type) {
                            //repositoryAction.extras[0] - USERNAME
                            RepositoryAction.Type.LIKE -> api.getLikedPhotos(repositoryAction.extras[0].toString(), page)
                            RepositoryAction.Type.TRENDING -> api.getRandomPhotos(page)
                            //repositoryAction.extras[0] - USERNAME
                            RepositoryAction.Type.USER_PHOTOS -> api.getUserPhotos(repositoryAction.extras[0].toString(), page)
                            else -> throw Exception("Unsupported Action")
                        }
                    }
                }
                response.apply {
                    val pagingData = PagingData.createFromHeaders(headers())
                    if (isSuccessful) {
                        diskExecutor {
                            @Suppress("UNCHECKED_CAST")
                            val body = body()?.let {
                                if (repositoryAction.type == RepositoryAction.Type.SEARCH)
                                    (it as SearchedPhotosJSON).results else it as List<PhotoJSON>
                            }
                            var nextIndex =
                                    when (repositoryAction.type) {
                                        RepositoryAction.Type.LIKE -> daoPhotoFacade.getNextIndex(Contract.TABLE_LIKE_PHOTOS)
                                        RepositoryAction.Type.TRENDING -> daoPhotoFacade.getNextIndex(Contract.TABLE_PHOTOS)
                                        RepositoryAction.Type.USER_PHOTOS -> daoPhotoFacade.getNextIndex(Contract.TABLE_USER_PHOTOS)
                                        RepositoryAction.Type.SEARCH -> daoPhotoFacade.getNextIndex(Contract.TABLE_SEARCH_PHOTOS)
                                        else -> throw Exception("Unsupported Action")
                                    }
                            body?.map {
                                val mapped = when (repositoryAction.type) {
                                    RepositoryAction.Type.LIKE -> it.toLikePhotoDbEntity(pagingData, nextIndex)
                                    RepositoryAction.Type.TRENDING -> it.toDbEntity(pagingData, nextIndex)
                                    RepositoryAction.Type.USER_PHOTOS -> it.toUserPhotoDbEntity(pagingData, nextIndex)
                                    RepositoryAction.Type.SEARCH -> it.toSearchPhotoDbEntity(pagingData, nextIndex)
                                    else -> throw Exception("Unsupported Action")
                                }
                                nextIndex += 1
                                mapped
                            }?.apply {
                                when (repositoryAction.type) {
                                    RepositoryAction.Type.LIKE -> daoPhotoFacade.insertPhotos(Contract.TABLE_LIKE_PHOTOS, this)
                                    RepositoryAction.Type.TRENDING -> daoPhotoFacade.insertPhotos(Contract.TABLE_PHOTOS, this)
                                    RepositoryAction.Type.USER_PHOTOS -> daoPhotoFacade.insertPhotos(Contract.TABLE_USER_PHOTOS, this)
                                    RepositoryAction.Type.SEARCH -> daoPhotoFacade.insertPhotos(Contract.TABLE_SEARCH_PHOTOS, this)
                                    else -> throw Exception("Unsupported Action")
                                }
                            }
                            uiExecutor {
                                callback?.onSuccess(Unit)
                            }

                        }
                    } else {
                        uiExecutor {
                            callback?.onError(Error("${code()}:${errorBody()?.string()}"))
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                uiExecutor {
                    callback?.onError(ex)
                }
            }
        }
    }

    override fun download(photo: Photo, callback: Repository.Callback<DownloadProgress>?) {
        uiExecutor {
            apiCallDispatcher.cancel()
        }
        ioExecutor {
            try {
                val start = AtomicBoolean(true)
                downloadManager.download(photo) { _, bytesRead, contentLength, done ->
                    if (contentLength == -1L) { //indeterminated
                        if (start.get()) {
                            callback?.runOn(uiExecutor) {
                                onSuccess(DownloadProgress.INDETERMINATED_START)
                                start.compareAndSet(true, false)
                            }
                        } else if (done) {
                            callback?.runOn(uiExecutor) {
                                onSuccess(DownloadProgress.INDETERMINATED_END)
                            }
                        }
                    } else {
                        val percent = (bytesRead.toFloat() / contentLength * 100).roundToInt()
                        if (percent % 10 == 0 || percent == 100 || done) {// backpressure relief
                            callback?.runOn(uiExecutor) {
                                onSuccess(DownloadProgress(percent, start.get(), percent == 100 || done))
                                start.compareAndSet(true, false)
                            }
                        }
                    }
                }
                callback?.runOn(uiExecutor) {
                    onSuccess(DownloadProgress.NONE)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                callback?.runOn(uiExecutor) {
                    onSuccess(DownloadProgress.INDETERMINATED_END)
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

    override fun like(photo: Photo, callback: Repository.Callback<Boolean>?) {
        //NOTE: do the toggle here: if like -> unlike and vice-versa
        val likedPhoto = photo.copy(likedByMe = !photo.likedByMe)
        diskExecutor {
            daoPhotoFacade.like(likedPhoto.id, likedPhoto.likedByMe)
            callback.runOn(uiExecutor) { callback?.onSuccess(likedPhoto.likedByMe) }
        }
        scheduledWorkService.schedule(WorkData(Tag(WorkType.LIKE, likedPhoto.id), "id" to likedPhoto.id,
                "likedByMe" to likedPhoto.likedByMe))
    }

    @MainThread
    override fun cancel() {
        downloadManager.cancel()
    }

    override fun clearAll() {
        diskExecutor {
            daoPhotoFacade.clear()
        }
    }

    override fun clear(repositoryAction: RepositoryAction) {
        diskExecutor {
            val table = when (repositoryAction.type) {
                RepositoryAction.Type.LIKE -> Contract.TABLE_LIKE_PHOTOS
                RepositoryAction.Type.TRENDING -> Contract.TABLE_PHOTOS
                RepositoryAction.Type.USER_PHOTOS -> Contract.TABLE_USER_PHOTOS
                RepositoryAction.Type.SEARCH -> Contract.TABLE_SEARCH_PHOTOS
                else -> throw Exception("Unsupported Action")
            }
            daoPhotoFacade.clear(table)
        }
    }

    override fun delete(photo: Photo) {
        diskExecutor {
            //for delete photo.id is the file path on disk
            daoExternalPhotoGalleryDAO.delete(photo.id)
        }
    }

    override fun getPhotoLiveData(id: String): LiveData<Photo> {
        return Transformations.map(daoPhotoFacade.getPhotoFromEitherTableLiveData(id)) {
            it.toPhoto()
        }
    }

}


