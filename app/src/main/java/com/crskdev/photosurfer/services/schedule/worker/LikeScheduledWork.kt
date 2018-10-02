package com.crskdev.photosurfer.services.schedule.worker

import androidx.work.ExistingWorkPolicy
import com.crskdev.photosurfer.services.schedule.AndroidScheduleUtils
import com.crskdev.photosurfer.services.schedule.AndroidScheduledWork
import com.crskdev.photosurfer.services.schedule.WorkData
import com.crskdev.photosurfer.services.schedule.WorkQueueBookKeeper

/**
 * Created by Cristian Pela on 02.10.2018.
 */
class LikeScheduledWork(bookKeeper: WorkQueueBookKeeper): AndroidScheduledWork(bookKeeper) {

    override fun schedule(workData: WorkData) {
        workManager.beginUniqueWork(workData.tag.toString(), ExistingWorkPolicy.REPLACE, AndroidScheduleUtils
                .defaultRequest<LikeWorker>(workData)).enqueue()
        //don't need to track the job for likes
//        workQueueBookKeeper.addToQueue(workData.tag)
    }

}