package com.crskdev.photosurfer.services.schedule.worker

import androidx.work.Worker
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.services.messaging.messages.Message
import com.crskdev.photosurfer.services.schedule.WorkType
import com.crskdev.photosurfer.util.systemNotification

class LikeWorker : Worker() {

    override fun doWork(): Result {
        val graph = applicationContext.dependencyGraph()
        val api = graph.photoAPI
        val apiCallDispatcher = graph.apiCallDispatcher
        val devicePushMessagingManager = graph.devicePushMessagingManager

        val id = inputData.getString("id")
        val liked: Boolean = inputData.getBoolean("likedByMe", false)

        if (id == null)
            return Result.FAILURE

        return try {
            val response = apiCallDispatcher {
                if (liked)
                    api.like(id)
                else
                    api.unlike(id)
            }
            val code = response.code()
            when (code) {
                201, 200 -> {
                    applicationContext.systemNotification("Scheduled photo like for id: $id successful")
                    devicePushMessagingManager.sendMessage(if (liked) {
                        Message.PhotoLiked(id)
                    } else {
                        Message.PhotoUnliked(id)
                    })
                    Result.SUCCESS
                }
                401 -> {
                    applicationContext.systemNotification("Scheduled photo like for id: $id failed. Need login")
                    Result.RETRY
                }
                429 -> {
                    applicationContext.systemNotification("Scheduled photo like for id: $id failed. Request limit reached")
                    Result.RETRY
                }
                else -> Result.RETRY
            }
        } catch (ex: Exception) {
            applicationContext.systemNotification("Scheduled photo like for id: $id failed. No network. Will retry later")
            Result.RETRY
        }
    }

}