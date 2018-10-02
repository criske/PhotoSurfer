package com.crskdev.photosurfer.services.schedule.worker

import com.crskdev.photosurfer.services.schedule.*

/**
 * Created by Cristian Pela on 02.10.2018.
 */
class CreateCollectionScheduledWork(bookKeeper: WorkQueueBookKeeper) : AndroidScheduledWork(bookKeeper) {

    override fun schedule(workData: WorkData) {
        val request = AndroidScheduleUtils.defaultRequest<CreateCollectionWorker>(workData)
        workManager.enqueue(request)
    }

}