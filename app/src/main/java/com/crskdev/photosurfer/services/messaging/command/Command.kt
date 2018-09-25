package com.crskdev.photosurfer.services.messaging.command

import android.content.Context
import android.util.Log
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.services.messaging.messages.Message
import com.crskdev.photosurfer.services.messaging.remote.FCMMessage

interface Command {

    fun onReceiveMessage(data: Map<String, String>)

    fun sendMessage(message: Message)
}

object UnknownCommand : Command {

    override fun onReceiveMessage(data: Map<String, String>) {
        throw Error("Unknown Command")
    }

    override fun sendMessage(message: Message) {
        throw Error("Unknown Command")
    }

}

class CollectionCreatedCommand(context: Context) : FCMCommand(context) {

    private val messagingAPI = context.dependencyGraph().messagingAPI

    override fun onReceiveMessage(data: Map<String, String>) {
        Log.d(CollectionCreatedCommand::class.java.simpleName, "${data}")
    }

    override fun sendMessage(message: Message) {
        messagingAPI.createCollection(FCMMessage().apply {
            actionType = message.topic.toString()
            id = (message as Message.CollectionCreate).collectionId.toString()
        })
    }

}