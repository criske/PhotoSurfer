package com.crskdev.photosurfer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.crskdev.photosurfer.dependencyGraph
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by Cristian Pela on 18.08.2018.
 */
interface JobService {
    fun schedule(workData: WorkData)
}


class JobServiceImpl(private val workerFactory: EnumMap<WorkType, Class<out Worker>>) : JobService {

    companion object {
        fun createDefault(): JobService {
            val workers = EnumMap<WorkType, Class<out Worker>>(WorkType::class.java).apply {
                put(WorkType.LIKE, LikeWorker::class.java)
            }
            return JobServiceImpl(workers)
        }
    }

    override fun schedule(workData: WorkData) {

        if (!workerFactory.containsKey(workData.type)) {
            throw Error("Worker not found for ${workData.type}")
        }

        val dataBuilder = Data.Builder()
        workData.extras.forEach {
            val any = it.second
            when (any) {
                is String -> dataBuilder.putString(it.first, any)
                is Boolean -> dataBuilder.putBoolean(it.first, any)
                else -> throw Error("Format not supported yed")
            }
        }
        val request = OneTimeWorkRequest.Builder(workerFactory[workData.type]!!)
                .setInputData(dataBuilder.build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .build()
        WorkManager.getInstance().enqueue(request)
    }


}

class WorkData(val type: WorkType, vararg val extras: Pair<String, Any>)


enum class WorkType {
    LIKE
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