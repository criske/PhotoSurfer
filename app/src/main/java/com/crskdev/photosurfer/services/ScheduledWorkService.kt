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
import java.util.concurrent.TimeUnit


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
        val context = applicationContext
        val channelID = "PhotoSurfer-Job-Service-Notification"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = channelID
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, name, importance)
            channel.description = channelID
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat
                .Builder(context, channelID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Photo Surfer")
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1337, notification)
    }

}

