package com.crskdev.photosurfer.services.messaging

import android.content.Context
import android.telephony.TelephonyManager
import androidx.core.content.getSystemService
import com.crskdev.photosurfer.data.remote.auth.AuthToken
import com.crskdev.photosurfer.data.remote.auth.ObservableAuthTokenStorage
import com.crskdev.photosurfer.services.messaging.command.CollectionCreatedCommand
import com.crskdev.photosurfer.services.messaging.command.Command
import com.crskdev.photosurfer.services.messaging.command.UnknownCommand
import com.crskdev.photosurfer.services.messaging.messages.Message
import com.crskdev.photosurfer.services.messaging.messages.Topic
import com.crskdev.photosurfer.util.Listenable
import com.crskdev.photosurfer.util.safeSet
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import java.lang.Exception
import java.util.concurrent.atomic.AtomicReference


/**
 * Created by Cristian Pela on 18.09.2018.
 */
interface PhotoSurferMessagingManager {

    fun onReceiveMessage(topicStr: String, data: Map<String, String>)

    fun onRegister(token: String)

    fun sendMessage(message: Message)

}

interface DeviceIdProvider {
    fun getId(): String
}

class DeviceIdProviderImpl(private val context: Context) : DeviceIdProvider {

    //TODO Find a way to implement this using app permissions
    private val id = ""

    override fun getId(): String = id
}

class PhotoSurferMessageManagerImpl(
        deviceIdProvider: DeviceIdProvider,
        private val context: Context,
        private val authTokenStorage: ObservableAuthTokenStorage,
        providedCommands: Map<Topic, Command> = emptyMap()) : PhotoSurferMessagingManager {

    private val lastUserName = AtomicReference<String>("")

    private val messaging = FirebaseMessaging.getInstance()

    private val instanceId = FirebaseInstanceId.getInstance()

    private val deviceId = deviceIdProvider.getId()

    private val TAG = PhotoSurferMessageManagerImpl::class.java.simpleName

    companion object {
        const val SERVER_KEY = "AAAArUVT6Js:APA91bF5vuPATIEy-2HnjS4_NB2fnEulbVS9esA_k3e8Yr4nYGMLPTEbA6hrjtFtC0Ow39Yc76PDv4vW5dwhkGojuoceqw0DTSzLdzmxQX95B5ErUbbX8tqGz0PwLS43Z6RaWtOEeXkW55_3r5RK_JQ4myqRUnuv_A"
        const val SENDER_ID = "744192469147"
        const val FIREBASE_API_KEY = "AIzaSyCFqSU9q44CLwQSRd_g3DlTrNpAbqBbP30"
    }

    init {
        authTokenStorage.addListener(object : Listenable.Listener<AuthToken> {
            override fun onNotified(data: AuthToken) {
                val name = data.username
                if (isRegistered()) {
                    val token = instanceId.token!!
                    if (name.isNotEmpty()) {
                        addDeviceToUserDeviceGroup(token, name)
                    } else if (lastUserName.get().isNotEmpty()) {
                        removeDeviceFromUserDeviceGroup(token, name)
                    }
                }
                lastUserName.safeSet(name)
            }
        })
    }

    fun isRegistered() = instanceId.token != null


    private val commands = providedCommands.takeIf { it.isNotEmpty() } ?: mapOf(
            Topic.COLLECTION_CREATED to CollectionCreatedCommand(context)
    )


    override fun sendMessage(message: Message) {

    }

    private fun getCommand(topicStr: String): Command {
        if (topicStr.isEmpty()) return UnknownCommand
        val topic = topicStr.substring(topicStr.lastIndexOf("/") + 1)
        return try {
            commands[Topic.valueOf(topic)] ?: UnknownCommand
        } catch (ex: Exception) {
            UnknownCommand
        }
    }


    override fun onReceiveMessage(topicStr: String, data: Map<String, String>) {
        getCommand(topicStr).onReceiveMessage(data)
    }

    override fun onRegister(token: String) {
        val loggedUsername = authTokenStorage.token()?.username
        if (loggedUsername != null) {
            addDeviceToUserDeviceGroup(token, loggedUsername)
        }
        Topic.values().forEach { topic ->
            messaging.subscribeToTopic(topic.toString()).result
        }
    }

    private fun addDeviceToUserDeviceGroup(deviceId: String, username: String) {

    }

    private fun removeDeviceFromUserDeviceGroup(deviceId: String, username: String) {

    }


}

