package com.crskdev.photosurfer.services.messaging

import android.content.Context
import com.crskdev.photosurfer.data.remote.auth.AuthToken
import com.crskdev.photosurfer.data.remote.auth.ObservableAuthTokenStorage
import com.crskdev.photosurfer.services.messaging.command.CollectionCreatedCommand
import com.crskdev.photosurfer.services.messaging.command.Command
import com.crskdev.photosurfer.services.messaging.command.UnknownCommand
import com.crskdev.photosurfer.services.messaging.messages.Message
import com.crskdev.photosurfer.services.messaging.messages.Topic
import com.crskdev.photosurfer.services.messaging.remote.MessagingAPI
import com.crskdev.photosurfer.util.Listenable
import com.google.firebase.iid.FirebaseInstanceId
import java.lang.Exception


/**
 * Created by Cristian Pela on 18.09.2018.
 */
interface PhotoSurferMessagingManager {

    fun onReceiveMessage(topicStr: String, data: Map<String, String>)

    fun onRegister(token: String)

    fun sendMessage(message: Message)

}

class PhotoSurferMessageManagerImpl(
        context: Context,
        private val messagingAPI: MessagingAPI,
        private val authTokenStorage: ObservableAuthTokenStorage,
        providedCommands: Map<Topic, Command> = emptyMap()) : PhotoSurferMessagingManager {

    private val instanceId = FirebaseInstanceId.getInstance()

    init {
        authTokenStorage.addListener(object : Listenable.Listener<AuthToken> {
            override fun onNotified(oldData: AuthToken?, newData: AuthToken) {
                val name = newData.username
                if (isRegistered()) {
                    if (name.isNotEmpty()) {
                        messagingAPI.registerDevice(name)
                    } else if (oldData != null) {
                        messagingAPI.unregisterDevice(oldData.username)
                    }
                }
            }
        })
    }

    fun isRegistered() = instanceId.token != null


    private val commands = providedCommands.takeIf { it.isNotEmpty() } ?: mapOf(
            Topic.COLLECTION_CREATED to CollectionCreatedCommand(context)
    )


    override fun sendMessage(message: Message) {
        getCommandByTopic(message.topic).sendMessage(message)
    }

    private fun getCommandByTopicString(topicStr: String): Command {
        if (topicStr.isEmpty()) return UnknownCommand
        val topic = topicStr.substring(topicStr.lastIndexOf("/") + 1)
        return try {
            getCommandByTopic(Topic.valueOf(topic))
        } catch (ex: Exception) {
            UnknownCommand
        }
    }

    private fun getCommandByTopic(topic: Topic) = commands[topic] ?: UnknownCommand


    override fun onReceiveMessage(topicStr: String, data: Map<String, String>) {
        getCommandByTopicString(topicStr).onReceiveMessage(data)
    }

    override fun onRegister(token: String) {
        val loggedUsername = authTokenStorage.token()?.username
        if (loggedUsername != null) {
            messagingAPI.registerDevice(loggedUsername)
        }
    }


}

