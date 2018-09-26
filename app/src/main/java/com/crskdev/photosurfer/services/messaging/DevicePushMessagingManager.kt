package com.crskdev.photosurfer.services.messaging

import android.content.Context
import com.crskdev.photosurfer.data.remote.auth.AuthToken
import com.crskdev.photosurfer.data.remote.auth.ObservableAuthTokenStorage
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.services.messaging.command.*
import com.crskdev.photosurfer.services.messaging.messages.Message
import com.crskdev.photosurfer.services.messaging.messages.Topic
import com.crskdev.photosurfer.services.messaging.remote.FCMMessage
import com.crskdev.photosurfer.services.messaging.remote.MessagingAPI
import com.crskdev.photosurfer.util.Listenable
import com.google.firebase.iid.FirebaseInstanceId
import java.lang.Error
import java.lang.Exception


/**
 * Created by Cristian Pela on 18.09.2018.
 */
interface DevicePushMessagingManager {

    fun onReceiveMessage(data: Map<String, String>)

    fun onRegister(token: String)

    fun sendMessage(message: Message)

}

class DevicePushMessageManagerImpl(
        context: Context,
        private val messagingAPI: MessagingAPI,
        private val authTokenStorage: ObservableAuthTokenStorage,
        providedCommands: Map<Topic, Command> = emptyMap()) : DevicePushMessagingManager {

    private val dependencyGraph = context.dependencyGraph()

    private val instanceId = FirebaseInstanceId.getInstance()

    private val ioExecutor = dependencyGraph.ioThreadExecutor

    private val diskExecutor = dependencyGraph.diskThreadExecutor

    init {
        authTokenStorage.addListener(object : Listenable.Listener<AuthToken> {
            override fun onNotified(oldData: AuthToken?, newData: AuthToken) {
                val name = newData.username
                if (isRegistered()) {
                    ioExecutor {
                        if (name.isNotEmpty()) {
                            messagingAPI.registerDevice(name).execute()
                        } else if (oldData != null) {
                            messagingAPI.unregisterDevice(oldData.username).execute()
                        }
                    }
                }
            }
        })
    }

    fun isRegistered() = instanceId.token != null


    private val commands by lazy {
        providedCommands.takeIf { it.isNotEmpty() } ?: mapOf(
                Topic.COLLECTION_CREATED to CollectionCreatedCommand(context),
                Topic.COLLECTION_DELETED to CollectionDeletedCommand(context),
                Topic.COLLECTION_EDITED to CollectionEditedCommand(context),
                Topic.COLLECTION_ADDED_PHOTO to CollectionAddedPhotoCommand(context),
                Topic.COLLECTION_REMOVED_PHOTO to CollectionRemovePhotoCommand(context),
                Topic.LIKED to PhotoLikeCommand(context),
                Topic.UNLIKED to PhotoUnlikeCommand(context)
        )
    }

    override fun sendMessage(message: Message) {
        getCommandByTopic(message.topic).sendMessage(message)
    }


    private fun getCommandByTopic(topic: Topic) = commands[topic] ?: UnknownCommand

    override fun onReceiveMessage(data: Map<String, String>) {
        val actionType = data["actionType"]
                ?: throw Error("FCMMessage parse: Field 'actionType' not found")
        val command = try {
            getCommandByTopic(Topic.valueOf(actionType))
        } catch (ex: Exception) {
            UnknownCommand
        }
        diskExecutor {
            val fcmMessage = FCMMessage().apply {
                this.actionType = actionType
                this.id = data["id"] ?: throw Error("FCMMessage parse: Field 'id' not found")
                this.extraId = data["extraId"] ?: ""
            }
            command.onReceiveMessage(fcmMessage)
        }
    }

    override fun onRegister(token: String) {
        val loggedUsername = authTokenStorage.token()?.username
        if (loggedUsername != null) {
            ioExecutor {
                messagingAPI.registerDevice(loggedUsername)
            }
        }
    }

}

