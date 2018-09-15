package com.crskdev.photosurfer.data.repository.scheduled

import androidx.work.Worker
import com.crskdev.photosurfer.data.repository.collection.CreateCollectionWorker
import com.crskdev.photosurfer.data.repository.collection.DeleteCollectionWorker
import com.crskdev.photosurfer.data.repository.collection.EditCollectionWorker
import com.crskdev.photosurfer.data.repository.photo.LikeWorker

enum class WorkType(val workerClass: Class<out Worker>) {
    LIKE(LikeWorker::class.java),
    CREATE_COLLECTION(CreateCollectionWorker::class.java),
    DELETE_COLLECTION(DeleteCollectionWorker::class.java),
    EDIT_COLLECTION(EditCollectionWorker::class.java)
}