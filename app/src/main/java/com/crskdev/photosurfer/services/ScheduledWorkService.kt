package com.crskdev.photosurfer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.crskdev.photosurfer.data.repository.scheduled.Tag
import com.crskdev.photosurfer.data.repository.scheduled.WorkData
import com.crskdev.photosurfer.data.repository.scheduled.WorkType
import com.crskdev.photosurfer.util.systemNotification
import java.util.concurrent.TimeUnit

//TODO rework the Scheduled Work API to allow chaining work manager requests

/**
 * Created by Cristian Pela on 18.08.2018.
 */
interface ScheduledWorkService {

    fun schedule(workData: WorkData)

    fun clearScheduled(workerTag: Tag)

    fun clearAllScheduled()
}

class ScheduledWorkServiceImpl : ScheduledWorkService {

    private val workManager = WorkManager.getInstance()

    override fun schedule(workData: WorkData) {
        val dataBuilder = Data.Builder()
        workData.extras.forEach {
            val any = it.second
            when (any) {
                is String -> dataBuilder.putString(it.first, any)
                is Boolean -> dataBuilder.putBoolean(it.first, any)
                is Int -> dataBuilder.putInt(it.first, any)
                else -> throw Error("Format not supported yet")
            }
        }
        val workerTag = workData.tag.toString()
        val request = OneTimeWorkRequest.Builder(workData.tag.type.workerClass)
                .setInputData(dataBuilder.build())
                .addTag(workerTag)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .build()
        if (workData.isUniqueWork) {
            workManager.beginUniqueWork(workerTag, ExistingWorkPolicy.REPLACE, request).enqueue()
        } else {
            workManager.enqueue(request)
        }
    }

    override fun clearScheduled(workerTag: Tag) {
        workManager.cancelAllWorkByTag(workerTag.toString())
    }

    override fun clearAllScheduled() {
        workManager.cancelAllWork()
    }
}


abstract class TypedWorker : Worker() {

    abstract val type: WorkType

    protected fun sendPlatformNotification(message: String) {
        applicationContext.systemNotification(message)
    }

}

