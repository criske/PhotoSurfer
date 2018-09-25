package com.crskdev.photosurfer.services.messaging


import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class PhotoSurferMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        applicationContext.dependencyGraph()
                .devicePushMessagingManager.onReceiveMessage(message.data)
    }

    override fun onNewToken(token: String) {
        applicationContext.dependencyGraph()
                .devicePushMessagingManager.onRegister(token)
    }
}
