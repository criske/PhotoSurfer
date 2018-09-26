package com.crskdev.photosurfer.data.repository.collection

import com.crskdev.photosurfer.data.repository.scheduled.Tag
import com.crskdev.photosurfer.data.repository.scheduled.WorkData
import com.crskdev.photosurfer.data.repository.scheduled.WorkType
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.services.TypedWorker
import com.crskdev.photosurfer.services.messaging.messages.Message

/**
 * Created by Cristian Pela on 15.09.2018.
 */
class EditCollectionWorker : TypedWorker() {

    override val type: WorkType = WorkType.EDIT_COLLECTION

    companion object {

        private const val ID = "ID"
        private const val TITLE = "collection_title"
        private const val DESCRIPTION = "collection_description"
        private const val PRIVATE = "collection_private"

        fun createWorkData(id: Int, title: String, description: String, private: Boolean): WorkData {
            return WorkData(Tag(WorkType.EDIT_COLLECTION, id.toString()), true,
                    ID to id,
                    TITLE to title,
                    DESCRIPTION to description,
                    PRIVATE to private)
        }
    }


    override fun doWork(): Result {
        try {
            val dependencyGraph = applicationContext.dependencyGraph()
            val collectionsAPI = dependencyGraph.collectionsAPI
            val devicePushMessagingManager = dependencyGraph.devicePushMessagingManager

            val id = inputData.getInt(ID, -1)
            val title = inputData.getString(TITLE)!!
            val description: String = inputData.getString(DESCRIPTION) ?: ""
            val private: Boolean = inputData.getBoolean(PRIVATE, true)
            collectionsAPI.updateCollection(id, title, description, private).execute()
            devicePushMessagingManager.sendMessage(Message.CollectionEdited(id))
            sendPlatformNotification("Collection edited")
        } catch (ex: Exception) {
            ex.printStackTrace()
            return Result.FAILURE
        }
        return Result.SUCCESS
    }


}
