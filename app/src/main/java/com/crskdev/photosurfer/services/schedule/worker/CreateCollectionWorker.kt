package com.crskdev.photosurfer.services.schedule.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.collections.CollectionsDAO
import com.crskdev.photosurfer.data.remote.PagingData
import com.crskdev.photosurfer.data.remote.collections.CollectionsAPI
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.entities.toCollectionDB
import com.crskdev.photosurfer.entities.toCollectionLite
import com.crskdev.photosurfer.services.messaging.messages.Message
import com.crskdev.photosurfer.services.schedule.Tag
import com.crskdev.photosurfer.services.schedule.WorkData
import com.crskdev.photosurfer.services.schedule.WorkType
import com.crskdev.photosurfer.util.systemNotification

/**
 * Created by Cristian Pela on 02.09.2018.
 */
class CreateCollectionWorker(context: Context, params: WorkerParameters)
    : Worker(context, params) {

    companion object {
        private const val TITLE = "collection_title"
        private const val DESCRIPTION = "collection_description"
        private const val PRIVATE = "collection_private"

        private const val PHOTO = "photo"

        fun createWorkData(
                title: String,
                description: String,
                private: Boolean,
                withPhoto: String? = null): WorkData {
            return WorkData(Tag(WorkType.CREATE_COLLECTION),
                    PHOTO to (withPhoto
                            ?: ""),
                    TITLE to title,
                    DESCRIPTION to description,
                    PRIVATE to private)
        }

    }

    override fun doWork(): Result {

        val graph = applicationContext.dependencyGraph()

        val title = inputData.getString(TITLE)!!
        val description: String = inputData.getString(DESCRIPTION) ?: ""
        val private: Boolean = inputData.getBoolean(PRIVATE, true)

        val withPhotoId = inputData.getString(PHOTO)?.takeIf { it.isNotEmpty() }

        val collectionsDAO: CollectionsDAO = graph.daoManager.getDao(Contract.TABLE_COLLECTIONS)
        val collectionsAPI: CollectionsAPI = graph.collectionsAPI
        val transactional = graph.daoManager.transactionRunner()

        val devicePushMessagingManager = graph.devicePushMessagingManager

        //TODO this really need to be chained
        try {
            val response = collectionsAPI
                    .createCollection(title, description, private)
                    .execute()
            if (response.isSuccessful) {
                val collectionReturned = response.body()
                collectionReturned?.let { cjson ->
                    transactional {
                        val collectionId = cjson.id
                        val lastCollectionEntity = collectionsDAO.getLastCollection()
                        val pagingData = PagingData.createNextPagingData(lastCollectionEntity?.pagingData)
                        collectionsDAO.createCollection(cjson.toCollectionDB(pagingData, collectionsDAO.getNextIndex()))

                        devicePushMessagingManager.sendMessage(Message.CollectionCreate(collectionId))
                        //add the photo to collection
                        if (withPhotoId != null) {
                            val addToCollectionResponse = collectionsAPI
                                    .addPhotoToCollection(collectionId, collectionId, withPhotoId)
                                    .execute()
                            if (addToCollectionResponse.isSuccessful) {
                                val photoDAOFacade = graph.photoDAOFacade
                                val photoDB = photoDAOFacade.getPhotoFromEitherTable(withPhotoId)
                                if (photoDB != null) {
                                    val collectionDB = collectionsDAO.getCollection(collectionId)?.apply {
                                        totalPhotos += 1
                                        coverPhotoUrls = photoDB.urls
                                        coverPhotoId = photoDB.id
                                    }
                                    if (collectionDB != null) {
                                        collectionsDAO.updateCollection(collectionDB)
                                        photoDAOFacade.addPhotoToCollection(withPhotoId, cjson.toCollectionLite())
                                    }
                                }
                            }
                            //TODO notify device for photo added to collection
                        }
                    }
                    applicationContext.systemNotification("Collection created")
                }
            } else {
                applicationContext.systemNotification(response.errorBody().toString())
                return Result.FAILURE
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            applicationContext.systemNotification(ex.message ?: "Unknown error: ${ex}")
            Result.FAILURE
        }

        return Result.SUCCESS
    }
}