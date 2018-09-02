package com.crskdev.photosurfer.data.repository.collection

import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.collections.CollectionsCollectionPhotoEntity
import com.crskdev.photosurfer.data.local.collections.CollectionsDAO
import com.crskdev.photosurfer.data.remote.PagingData
import com.crskdev.photosurfer.data.remote.collections.CollectionJSON
import com.crskdev.photosurfer.data.remote.collections.CollectionsAPI
import com.crskdev.photosurfer.data.remote.photo.PhotoJSON
import com.crskdev.photosurfer.data.repository.scheduled.Tag
import com.crskdev.photosurfer.data.repository.scheduled.WorkData
import com.crskdev.photosurfer.data.repository.scheduled.WorkType
import com.crskdev.photosurfer.dependencyGraph
import com.crskdev.photosurfer.entities.toCollectionDB
import com.crskdev.photosurfer.entities.toCollectionPhotoDbEntity
import com.crskdev.photosurfer.services.TypedWorker

/**
 * Created by Cristian Pela on 02.09.2018.
 */
class CreateCollectionWorker : TypedWorker() {

    companion object {
        private const val COLLECTION = "collection"

        private const val PHOTO = "photo"

        fun createWorkData(
                collectionJson: String,
                withPhoto: String? = null): WorkData {
            return WorkData(Tag(WorkType.CREATE_COLLECTION), false, PHOTO to (withPhoto ?: ""),
                    COLLECTION to collectionJson)
        }

    }

    override val type: WorkType = WorkType.CREATE_COLLECTION


    override fun doWork(): Result {

        val graph = applicationContext.dependencyGraph()

        val jsonAdapter = graph.moshi.adapter(CollectionJSON::class.java)
        val collection = jsonAdapter.fromJson(inputData.getString(COLLECTION)!!)!!
        val photoJSON = inputData.getString(PHOTO)?.takeIf { it.isNotEmpty() }?.let {
            graph.moshi.adapter(PhotoJSON::class.java).fromJson(it)
        }

        val collectionsDAO: CollectionsDAO = graph.daoManager.getDao(Contract.TABLE_COLLECTIONS)
        val collectionsAPI: CollectionsAPI = graph.collectionsAPI
        val transactional = graph.daoManager.transactionRunner()

        try {
            val response = collectionsAPI
                    .createCollection(collection.title, collection.description, collection.private)
                    .execute()
            if (response.isSuccessful) {
                val collectionReturned = response.body()
                collectionReturned?.let { cjson ->
                    transactional {
                        val lastCollectionEntity = collectionsDAO.getLatestCollection()
                        val pagingData = lastCollectionEntity?.let {
                            PagingData(it.total ?: 0, it.curr ?: 1, it.prev, it.next)
                        } ?: PagingData(0, 1, null, null)
                        collectionsDAO.createCollection(cjson.toCollectionDB(pagingData))
                        if (photoJSON != null) {
                            val photoPagingData = collectionsDAO
                                    .getLatestCollectionPhoto(cjson.id)?.let {
                                        PagingData(it.total ?: 1+1, it.curr ?: 1, it.prev, it.next)
                                    } ?: PagingData(1, 1, null, null)
                            val photoDB = photoJSON.toCollectionPhotoDbEntity(
                                    photoPagingData,
                                    collectionsDAO.getNextCollectionPhotoIndex())
                            collectionsDAO.insertCollectionPhoto(photoDB)
                            collectionsDAO.addPhotoToCollection(CollectionsCollectionPhotoEntity().apply {
                                collectionId = cjson.id
                                photoId = photoJSON.id
                            })
                        }
                    }
                    sendPlatformNotification("Collection created")
                }
            } else {
                sendPlatformNotification(response.errorBody().toString())
                return Result.FAILURE
            }
        } catch (ex: Exception) {
            sendPlatformNotification(ex.message ?: "Unknown error: ${ex}")
            Result.FAILURE
        }

        return Result.SUCCESS
    }
}