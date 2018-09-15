package com.crskdev.photosurfer.data.repository.collection

import com.crskdev.photosurfer.data.repository.scheduled.Tag
import com.crskdev.photosurfer.data.repository.scheduled.WorkData
import com.crskdev.photosurfer.data.repository.scheduled.WorkType
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.services.TypedWorker

/**
 * Created by Cristian Pela on 14.09.2018.
 */
class DeleteCollectionWorker : TypedWorker() {

    override val type: WorkType = WorkType.DELETE_COLLECTION

    companion object {

        private const val ID = "ID"

        fun createWorkData(id: Int): WorkData {
            return WorkData(Tag(WorkType.DELETE_COLLECTION, id.toString()), false, ID to id)
        }
    }


    override fun doWork(): Result {
        val id = inputData.getInt(ID, -1)
        try {
            val collectionsAPI = applicationContext.dependencyGraph().collectionsAPI
            collectionsAPI.deleteCollection(id).execute()
        } catch (ex: Exception) {
            ex.printStackTrace()

            return Result.FAILURE
        }
        return Result.SUCCESS
    }


}