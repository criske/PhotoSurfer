package com.crskdev.photosurfer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.crskdev.photosurfer.dependencyGraph
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
                else -> throw Error("Format not supported yed")
            }
        }
        val workerTag = workData.tag.toString()
        workManager.cancelAllWorkByTag(workerTag)
        val request = OneTimeWorkRequest.Builder(workData.tag.type.workerClass)
                .setInputData(dataBuilder.build())
                .addTag(workerTag)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .build()
        workManager.enqueue(request)
    }

    override fun clearScheduled(workerTag: Tag) {
        workManager.cancelAllWorkByTag(workerTag.toString())
    }

    override fun clearAllScheduled() {
        workManager.cancelAllWork()
    }
}

class WorkData(val tag: Tag, vararg val extras: Pair<String, Any>)


enum class WorkType(val workerClass: Class<out Worker>) {
    LIKE(LikeWorker::class.java)
}

class Tag(val type: WorkType, val uniqueId: String) {
    override fun toString(): String {
        return "$type#$uniqueId"
    }
}

abstract class TypedWorker : Worker() {

    abstract val type: WorkType

    protected fun sendNotification(message: String) {
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

class LikeWorker : TypedWorker() {

    override val type: WorkType = WorkType.LIKE

    override fun doWork(): Result {
        val api = applicationContext.dependencyGraph().photoAPI

        val id = inputData.getString("id")
        val liked: Boolean = inputData.getBoolean("likedByMe", false)

        if (id == null)
            return Result.FAILURE

        val call = if (liked) api.like(id) else api.unlike(id)
        return try {
            val response = call.execute()
            val code = response.code()
            when (code) {
                201, 200 -> {
                    sendNotification("Scheduled photo like for id: $id successful")
                    Result.SUCCESS
                }
                401 -> {
                    sendNotification("Scheduled photo like for id: $id failed. Need login")
                    Result.RETRY
                }
                429 -> {
                    sendNotification("Scheduled photo like for id: $id failed. Request limit reached")
                    Result.RETRY
                }
                else -> Result.RETRY
            }
        } catch (ex: Exception) {
            sendNotification("Scheduled photo like for id: $id failed. No network. Will retry later")
            Result.RETRY
        }
    }

}