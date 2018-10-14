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
 * Created by Cristian Pela on 14.09.2018.
 */
class DeleteCollectionWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {

        private const val ID = "ID"

        fun createWorkData(id: Int): WorkData {
            return WorkData(Tag(WorkType.DELETE_COLLECTION, id.toString()), ID to id)
        }
    }

    override fun doWork(): Result {
        val id = inputData.getInt(ID, -1)
        try {
            val graph = applicationContext.dependencyGraph()
            val collectionsAPI = graph.collectionsAPI
            val res = collectionsAPI.deleteCollection(id).execute()
            if (res.isSuccessful) {
                graph.devicePushMessagingManager.sendMessage(Message.CollectionDeleted(id))
            }
            applicationContext.systemNotification("Collection deleted")
        } catch (ex: Exception) {
            ex.printStackTrace()
            return Result.FAILURE
        }
        return Result.SUCCESS
    }


}