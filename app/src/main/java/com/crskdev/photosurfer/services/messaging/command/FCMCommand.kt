package com.crskdev.photosurfer.services.messaging.command

import android.content.Context
import com.crskdev.photosurfer.dependencies.DependencyGraph
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging

abstract class FCMCommand(protected val applicationContext: Context) : Command {

    protected val fcmInstanceID by lazy { FirebaseInstanceId.getInstance() }

    protected val fcmMessaging by lazy { FirebaseMessaging.getInstance() }

}