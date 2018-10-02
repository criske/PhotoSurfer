package com.crskdev.photosurfer.services.schedule.worker

import com.crskdev.photosurfer.services.schedule.*

/**
 * Created by Cristian Pela on 02.10.2018.
 */
class DeleteCollectionScheduledWork(bookKeeper: WorkQueueBookKeeper) : AndroidScheduledWork(bookKeeper) {

    override fun schedule(workData: WorkData) {
        val request = AndroidScheduleUtils.defaultRequest<DeleteCollectionWorker>(workData)
        val tag = workData.tag
        //cancel if add/remove photo to this collection is scheduled
        workQueueBookKeeper.getAllWithTagLike(tag.morph(WorkType.PHOTO_TO_COLLECTION).toString()).forEach {
            workManager.cancelAllWorkByTag(it)
        }
        //cancel any push edit for this collection
        workQueueBookKeeper.getAllWithTagLike(tag.morph(WorkType.EDIT_PUSH_COLLECTION).toString()).forEach {
            workManager.cancelAllWorkByTag(it)
        }
        //cancel if this collection is scheduled for edit
        workManager.cancelAllWorkByTag(workData.tag.morph(WorkType.EDIT_COLLECTION).toString())
        workManager.enqueue(request)
    }

}