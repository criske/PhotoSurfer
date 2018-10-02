package com.crskdev.photosurfer.services.schedule.worker

import androidx.work.ExistingWorkPolicy
import com.crskdev.photosurfer.services.schedule.*

/**
 * Created by Cristian Pela on 02.10.2018.
 */
class EditCollectionScheduledWork(bookKeeper: WorkQueueBookKeeper) : AndroidScheduledWork(bookKeeper) {

    override fun schedule(workData: WorkData) {
        workManager.beginUniqueWork(workData.tag.toString(), ExistingWorkPolicy.REPLACE,
                AndroidScheduleUtils
                        .defaultRequest<EditCollectionWorker>(workData))
                .then(AndroidScheduleUtils
                        .defaultRequest<EditCollectionPushMessageWorker>(workData.morph(WorkType.EDIT_PUSH_COLLECTION)))
                .enqueue()
        workQueueBookKeeper.addToQueue(workData.tag)
        workQueueBookKeeper.addToQueue(workData.morph(WorkType.EDIT_PUSH_COLLECTION).tag)
    }

}