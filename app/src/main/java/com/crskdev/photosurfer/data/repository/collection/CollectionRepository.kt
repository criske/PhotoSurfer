package com.crskdev.photosurfer.data.repository.collection

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.DataSource
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.DaoManager
import com.crskdev.photosurfer.data.local.collections.CollectionPhotoDAO
import com.crskdev.photosurfer.data.local.collections.CollectionsDAO
import com.crskdev.photosurfer.data.local.photo.PhotoDAOFacade
import com.crskdev.photosurfer.data.local.track.StaleDataTrackSupervisor
import com.crskdev.photosurfer.data.remote.APICallDispatcher
import com.crskdev.photosurfer.data.remote.PagingData
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.collections.CollectionsAPI
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.entities.*
import com.crskdev.photosurfer.entities.Collection
import com.crskdev.photosurfer.services.ScheduledWorkService
import com.crskdev.photosurfer.services.executors.ExecutorsManager
import com.crskdev.photosurfer.services.executors.ExecutorType
import com.crskdev.photosurfer.util.runOn

/**
 * Created by Cristian Pela on 31.08.2018.
 */
interface CollectionRepository : Repository {

    fun createCollection(collection: Collection, withPhotoId: String? = null)

    fun editCollection(collection: Collection)

    fun deleteCollection(collectionId: Int)

    fun getCollections(): DataSource.Factory<Int, Collection>

    fun getCollectionPhotos(collectionId: Int): DataSource.Factory<Int, Photo>

    fun getCollectionsForPhoto(photoId: String): DataSource.Factory<Int, PairBE<Collection, Boolean>>

    fun fetchAndSaveCollection(page: Int, callback: Repository.Callback<Unit>? = null)

    fun fetchAndSaveCollectionPhotos(collectionId: Int, page: Int, callback: Repository.Callback<Unit>?)

    fun addPhotoToCollection(collection: Collection, photo: Photo)

    fun removePhotoFromCollection(collection: Collection, photo: Photo)

    fun getCollectionLiveData(collectionId: Int): LiveData<Collection>

}

class CollectionRepositoryImpl(
        executorsManager: ExecutorsManager,
        daoManager: DaoManager,
        private val photoDAOFacade: PhotoDAOFacade,
        private val scheduledWorkService: ScheduledWorkService,
        private val apiCallDispatcher: APICallDispatcher,
        private val collectionAPI: CollectionsAPI,
        private val authTokenStorage: AuthTokenStorage,
        private val staleDataTrackSupervisor: StaleDataTrackSupervisor
) : CollectionRepository {

    private val collectionDAO: CollectionsDAO = daoManager.getDao(Contract.TABLE_COLLECTIONS)
    private val collectionPhotoDAO: CollectionPhotoDAO = daoManager.getDao(Contract.TABLE_COLLECTION_PHOTOS)

    private val transactional = daoManager.transactionRunner()
    private val ioExecutor = executorsManager.types[ExecutorType.NETWORK]!!

    private val diskExecutor = executorsManager.types[ExecutorType.DISK]!!
    private val uiExecutor = executorsManager.types[ExecutorType.UI]!!

    override fun createCollection(collection: Collection, withPhotoId: String?) {
        scheduledWorkService.schedule(CreateCollectionWorker
                .createWorkData(
                        collection.title,
                        collection.description ?: "",
                        collection.private,
                        withPhotoId))
    }

    override fun editCollection(collection: Collection) {
        scheduledWorkService.schedule(EditCollectionWorker.createWorkData(
                collection.id,
                collection.title,
                collection.description ?: "",
                collection.private
        ))
        diskExecutor {
            transactional {
                collectionDAO.getCollection(collection.id)?.apply {
                    title = collection.title
                    description = collection.description
                    notPublic = collection.private
                }?.run {
                    collectionDAO.updateCollection(this)
                }
            }
        }
    }

    override fun deleteCollection(collectionId: Int) {
        scheduledWorkService.schedule(DeleteCollectionWorker.createWorkData(collectionId))
        diskExecutor {
            transactional {
                collectionDAO.deleteCollectionById(collectionId)
                val photosByTable = photoDAOFacade
                        .getPhotosBelongToCollectionMappedByTable(collectionId)
                photosByTable.forEach {
                    val photos = it.value
                    val table = it.key
                    photos.forEach { p ->
                        //remove collection for this photo
                        p.collections = p.collections.filter { c -> c.id != collectionId }
                        photoDAOFacade.update(table, p)
                    }
                }
            }
        }
    }

    override fun getCollections(): DataSource.Factory<Int, Collection> =
            collectionDAO.getCollections().mapByPage { page ->
                staleDataTrackSupervisor.runStaleDataCheckForTable(Contract.TABLE_COLLECTIONS)
                page.map { it.toCollection() }
            }

    override fun getCollectionsForPhoto(photoId: String): DataSource.Factory<Int, PairBE<Collection, Boolean>> {
        return collectionDAO.getCollections().mapByPage { page ->
            staleDataTrackSupervisor.runStaleDataCheckForTable(Contract.TABLE_COLLECTIONS)
            page.map { ce ->
                val c = ce.toCollection()
                val photo = photoDAOFacade.getPhotoFromEitherTable(photoId)?.toPhoto()
                val photoCollections = photo?.collections
                c toBE (photoCollections?.firstOrNull { it.id == c.id } != null)
            }
        }
    }

    override fun getCollectionPhotos(collectionId: Int): DataSource.Factory<Int, Photo> {
        return collectionPhotoDAO.getPhotos().mapByPage { col ->
            val sample = col.firstOrNull()
            if (sample?.currentCollectionId == collectionId) {
                staleDataTrackSupervisor.runStaleDataCheckForTable(Contract.TABLE_COLLECTIONS)
            } else {
                collectionPhotoDAO.clear()
            }
            col.map { it.toPhoto() }
        }
    }

    override fun getCollectionLiveData(collectionId: Int): LiveData<Collection> =
            Transformations.map(collectionDAO.getCollectionLiveData(collectionId)) { c ->
                c.toCollection()
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
                            val nextIndex = collectionDAO.getNextIndex()
                            val photos = response.body()?.map {
                                it.toCollectionPhotoDbEntity(pagingData, nextIndex).apply {
                                    currentCollectionId = collectionId
                                }
                            } ?: emptyList()
                            collectionPhotoDAO.insertPhotos(photos)
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
            collectionAPI.addPhotoToCollection(collection.id, collection.id, photo.id).execute()
        }
        diskExecutor {
            transactional {
                //update size and cover
                val collectionDB = collectionDAO.getCollection(collection.id)?.apply {
                    totalPhotos += 1
                    coverPhotoId = photo.id
                    coverPhotoUrls = transformMapUrls(photo.urls)
                }
                collectionDB?.let { collectionDAO.updateCollection(it) }
                photoDAOFacade.addPhotoToCollection(photo.id, collection.asLite())
            }
        }
    }

    override fun removePhotoFromCollection(collection: Collection, photo: Photo) {
        //todo schedule api call
        ioExecutor {
            collectionAPI.removePhotoFromCollection(collection.id, collection.id, photo.id).execute()
        }
        diskExecutor {
            transactional {
                photoDAOFacade.removePhotoFromCollection(photo.id, collection.asLite())

                val collectionDB = collectionDAO.getCollection(collection.id)?.apply {
                    totalPhotos -= 1
                }

                val lastPhoto = collectionPhotoDAO.getLastPhoto()
                //update cover with latest photo in collection only if current collection id is collection id
                if (lastPhoto?.currentCollectionId == collection.id) {
                    lastPhoto.let {
                        collectionDB?.coverPhotoId = it.id
                        collectionDB?.coverPhotoUrls = it.urls
                    }
                } else if (lastPhoto == null) { // there is no photo in this collection
                    collectionDB?.coverPhotoId = null
                    collectionDB?.coverPhotoUrls = null
                }
                collectionDB?.let { collectionDAO.updateCollection(it) }
            }
        }
    }

}