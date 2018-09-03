package com.crskdev.photosurfer.data.repository.collection

import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.DaoManager
import com.crskdev.photosurfer.data.local.collections.CollectionsCollectionPhotoEntity
import com.crskdev.photosurfer.data.local.collections.CollectionsDAO
import com.crskdev.photosurfer.data.local.photo.PhotoDAOFacade
import com.crskdev.photosurfer.data.local.track.StaleDataTrackSupervisor
import com.crskdev.photosurfer.data.remote.APICallDispatcher
import com.crskdev.photosurfer.data.remote.PagingData
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.collections.CollectionJSON
import com.crskdev.photosurfer.data.remote.collections.CollectionsAPI
import com.crskdev.photosurfer.data.remote.photo.PhotoJSON
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.entities.*
import com.crskdev.photosurfer.entities.Collection
import com.crskdev.photosurfer.services.ScheduledWorkService
import com.crskdev.photosurfer.services.executors.ExecutorsManager
import com.crskdev.photosurfer.util.runOn
import com.squareup.moshi.Moshi

/**
 * Created by Cristian Pela on 31.08.2018.
 */
interface CollectionRepository : Repository {

    fun createCollection(collection: Collection, withPhoto: Photo? = null)

    fun getCollections(): DataSource.Factory<Int, Collection>

    fun getCollectionsForPhoto(photo: Photo): DataSource.Factory<Int, Pair<Collection, Boolean>>

    fun fetchAndSaveCollection(page: Int, callback: Repository.Callback<Unit>?)

    fun fetchAndSaveCollectionPhotos(collectionId: Int, page: Int, callback: Repository.Callback<Unit>?)

    fun addPhotoToCollection(collection: Collection, photo: Photo)

    fun removePhotoFromCollection(collection: Collection, photo: Photo)
}

class CollectionRepositoryImpl(
        executorsManager: ExecutorsManager,
        daoManager: DaoManager,
        moshi: Moshi,
        private val photoDAOFacade: PhotoDAOFacade,
        private val scheduledWorkService: ScheduledWorkService,
        private val apiCallDispatcher: APICallDispatcher,
        private val collectionAPI: CollectionsAPI,
        private val authTokenStorage: AuthTokenStorage,
        private val staleDataTrackSupervisor: StaleDataTrackSupervisor
) : CollectionRepository {

    private val collectionDAO: CollectionsDAO = daoManager.getDao(Contract.TABLE_COLLECTIONS)

    private val transactional = daoManager.transactionRunner()
    private val ioExecutor = executorsManager.types[ExecutorsManager.Type.NETWORK]!!

    private val diskExecutor = executorsManager.types[ExecutorsManager.Type.DISK]!!
    private val uiExecutor = executorsManager.types[ExecutorsManager.Type.UI]!!

    private val collectionJsonAdapter by lazy { moshi.adapter(CollectionJSON::class.java) }
    private val photoJsonAdapter by lazy { moshi.adapter(PhotoJSON::class.java) }

    override fun createCollection(collection: Collection, withPhoto: Photo?) {
        scheduledWorkService.schedule(CreateCollectionWorker
                .createWorkData(
                        collectionJsonAdapter.toJson(collection.toJSON()),
                        withPhoto?.let { photoJsonAdapter.toJson(it.toJSON()) }))
    }

    override fun getCollections(): DataSource.Factory<Int, Collection> =
            collectionDAO.getCollections().mapByPage { page ->
                staleDataTrackSupervisor.runStaleDataCheckForTable(Contract.TABLE_COLLECTIONS)
                page.map { it.toCollection() }
            }

    override fun getCollectionsForPhoto(photo: Photo): DataSource.Factory<Int, Pair<Collection, Boolean>> {
        return collectionDAO.getCollections().mapByPage { page ->
            staleDataTrackSupervisor.runStaleDataCheckForTable(Contract.TABLE_COLLECTIONS)
            page.map { ce ->
                val c = ce.toCollection()
                c to (photo.collections.firstOrNull { it.id == c.id } != null)
            }
        }
    }


    override fun fetchAndSaveCollection(page: Int, callback: Repository.Callback<Unit>?) {
        ioExecutor {
            try {
                val me = authTokenStorage.token()?.username
                if (me == null) {
                    callback?.runOn(uiExecutor) { onError(Error("Must be logged in to get your collection"), true) }
                } else {
                    val response = apiCallDispatcher { collectionAPI.getMyCollections(me, page) }
                    with(response) {
                        if (response.isSuccessful) {
                            val pagingData = PagingData.createFromHeaders(headers())
                            diskExecutor {
                                val collections = response.body()?.map {
                                    it.toCollectionDB(pagingData)
                                } ?: emptyList()
                                collectionDAO.insertCollections(collections)
                            }
                            callback?.runOn(uiExecutor) { onSuccess(Unit) }
                            Unit
                        } else {
                            callback?.runOn(uiExecutor) { onError(Error("${code()}:${errorBody()?.string()}")) }
                            Unit
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                callback?.runOn(uiExecutor) { onError(ex) }
            }
        }
    }

    override fun fetchAndSaveCollectionPhotos(collectionId: Int, page: Int, callback: Repository.Callback<Unit>?) {
        ioExecutor {
            val me = authTokenStorage.token()?.username
            if (me == null) {
                callback?.runOn(uiExecutor) { onError(Error("Must be logged in to get your collection"), true) }
            } else {
                val response = apiCallDispatcher { collectionAPI.getMyCollectionPhotos(collectionId, page) }
                with(response) {
                    if (response.isSuccessful) {
                        val pagingData = PagingData.createFromHeaders(headers())
                        diskExecutor {
                            transactional {
                                val nextIndex = collectionDAO.getNextCollectionPhotoIndex()
                                val photos = response.body()?.map {
                                    it.toCollectionPhotoDbEntity(pagingData, nextIndex)
                                } ?: emptyList()
                                val manyToManyIndices = photos.map {
                                    CollectionsCollectionPhotoEntity().apply {
                                        this.collectionId = collectionId
                                        this.photoId = it.id
                                    }
                                }
                                collectionDAO.insertCollectionPhotos(photos)
                                collectionDAO.addPhotosToCollection(manyToManyIndices)
                            }
                            callback?.runOn(uiExecutor) { onSuccess(Unit) }
                            Unit
                        }
                    } else {
                        callback?.runOn(uiExecutor) { onError(Error("${code()}:${errorBody()?.string()}")) }
                        Unit
                    }
                }
            }
        }
    }

    override fun addPhotoToCollection(collection: Collection, photo: Photo) {
        //todo schedule api call
        ioExecutor {
            collectionAPI.addPhotoToCollection(collection.id, photo.id)
        }
        diskExecutor {
            val photosFromAllTables = photoDAOFacade.getPhotoFromAllTables(photo.id).map {
                it.collections = it.collections?.let { cstr -> collectionsLiteStrAdd(cstr, collection) }
                it
            }
            photosFromAllTables.forEach {
                photoDAOFacade.updateForAllTables(it)
            }
        }
    }

    override fun removePhotoFromCollection(collection: Collection, photo: Photo) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}