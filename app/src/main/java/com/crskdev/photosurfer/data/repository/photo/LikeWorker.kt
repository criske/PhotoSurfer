package com.crskdev.photosurfer.data.repository.photo

import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.services.TypedWorker
import com.crskdev.photosurfer.data.repository.scheduled.WorkType

class LikeWorker : TypedWorker() {

    override val type: WorkType = WorkType.LIKE

    override fun doWork(): Result {
        val graph = applicationContext.dependencyGraph()
        val api = graph.photoAPI
        val apiCallDispatcher = graph.apiCallDispatcher

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
                    sendPlatformNotification("Scheduled photo like for id: $id successful")
                    Result.SUCCESS
                }
                401 -> {
                    sendPlatformNotification("Scheduled photo like for id: $id failed. Need login")
                    Result.RETRY
                }
                429 -> {
                    sendPlatformNotification("Scheduled photo like for id: $id failed. Request limit reached")
                    Result.RETRY
                }
                else -> Result.RETRY
            }
        } catch (ex: Exception) {
            sendPlatformNotification("Scheduled photo like for id: $id failed. No network. Will retry later")
            Result.RETRY
        }
    }

}