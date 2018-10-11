package com.crskdev.photosurfer.services.schedule.worker

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.Worker
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.services.schedule.AndroidScheduledWork
import com.crskdev.photosurfer.services.schedule.WorkData
import com.crskdev.photosurfer.services.schedule.WorkQueueBookKeeper
import com.crskdev.photosurfer.util.systemNotification
import java.util.concurrent.TimeUnit

/**
 * Created by Cristian Pela on 11.10.2018.
 */
class StaleDataTrackScheduledWork(bookKeeper: WorkQueueBookKeeper) : AndroidScheduledWork(bookKeeper) {

    override fun schedule(workData: WorkData) {
        val tag = "photo-surfer-stale-track-periodic-tag"
        val request = PeriodicWorkRequest
                .Builder(StaleDataTrackWorker::class.java, 12, TimeUnit.HOURS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
                .addTag(tag)
                .build()
        workManager.enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.KEEP, request)
    }

}

class StaleDataTrackWorker() : Worker() {

    override fun doWork(): Result {
        //we make sure user is not interacting with app, otherwise run the stale check
        if (!isAppInForeground()) {
            return Result.RETRY
        }
        val dependencyGraph = applicationContext.dependencyGraph()
        val staleDataTrackSupervisor = dependencyGraph.staleDataTrackSupervisor
        Contract.PHOTO_AND_COLLECTIONS_TABLES.forEach {
            //TODO activate this stale tracker
            staleDataTrackSupervisor.runStaleDataCheckForTable(it)
        }
        applicationContext.systemNotification("Periodic stale data track done...")
        return Result.SUCCESS
    }

    private fun isAppInForeground(): Boolean {
        val appProcessInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(appProcessInfo)
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE)
    }
}
