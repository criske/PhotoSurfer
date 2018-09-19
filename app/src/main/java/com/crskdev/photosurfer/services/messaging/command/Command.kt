package com.crskdev.photosurfer.services.messaging.command

import android.content.Context
import android.util.Log
import com.crskdev.photosurfer.services.messaging.messages.Message

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

    override fun onReceiveMessage(data: Map<String, String>) {
        Log.d(CollectionCreatedCommand::class.java.simpleName, "${data}")
    }

    override fun sendMessage(message: Message) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}