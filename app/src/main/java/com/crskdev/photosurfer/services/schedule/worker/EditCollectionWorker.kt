package com.crskdev.photosurfer.services.schedule.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.services.messaging.messages.Message
import com.crskdev.photosurfer.services.schedule.Tag
import com.crskdev.photosurfer.services.schedule.WorkData
import com.crskdev.photosurfer.services.schedule.WorkType
import com.crskdev.photosurfer.util.systemNotification

/**
 * Created by Cristian Pela on 15.09.2018.
 */
class EditCollectionWorker (context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {

        const val ID = "ID"
        private const val TITLE = "collection_title"
        private const val DESCRIPTION = "collection_description"
        private const val PRIVATE = "collection_private"

        fun createWorkData(id: Int, title: String, description: String, private: Boolean): WorkData {
            return WorkData(Tag(WorkType.EDIT_COLLECTION, id.toString()),
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
            val bookKeeper = dependencyGraph.workQueueBookKeeper

            val id = inputData.getInt(ID, -1)
            val title = inputData.getString(TITLE)!!
            val description: String = inputData.getString(DESCRIPTION) ?: ""
            val private: Boolean = inputData.getBoolean(PRIVATE, true)
            val execute = collectionsAPI.updateCollection(id, title, description, private).execute()
            if (execute.isSuccessful) {
                bookKeeper.removeFromQueue(Tag(WorkType.EDIT_COLLECTION, id.toString()))
                applicationContext.systemNotification("Collection edited")
            } else if (execute.code() == 429) {
                return Result.RETRY
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
            return Result.FAILURE
        }
        return Result.SUCCESS
    }


}


class EditCollectionPushMessageWorker (context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        try {
            val devicePushMessagingManager = applicationContext.dependencyGraph()
                    .devicePushMessagingManager
            val id = inputData.getInt(EditCollectionWorker.ID, -1)
            devicePushMessagingManager.sendMessage(Message.CollectionEdited(id))
        } catch (ex: Exception) {
            return Result.RETRY
        }
        return Result.SUCCESS
    }

}