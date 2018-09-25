package com.crskdev.photosurfer.services.messaging.command

import android.content.Context
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.collections.CollectionsDAO
import com.crskdev.photosurfer.data.remote.PagingData
import com.crskdev.photosurfer.dependencies.dependencyGraph
import com.crskdev.photosurfer.entities.toCollectionDB
import com.crskdev.photosurfer.services.messaging.messages.Message
import com.crskdev.photosurfer.services.messaging.remote.FCMMessage

interface Command {

    fun onReceiveMessage(message: FCMMessage)

    fun sendMessage(message: Message)
}

object UnknownCommand : Command {

    override fun onReceiveMessage(message: FCMMessage) {
        throw Error("Unknown Command")
    }

    override fun sendMessage(message: Message) {
        throw Error("Unknown Command")
    }

}


class CollectionCreatedCommand(context: Context) : FCMCommand(context) {

    private val dependencyGraph = context.dependencyGraph()

    private val messagingAPI = dependencyGraph.messagingAPI

    private val collectionsDAO: CollectionsDAO = dependencyGraph.daoManager.getDao(Contract.TABLE_COLLECTIONS)

    private val collectionAPI = dependencyGraph.collectionsAPI

    override fun onReceiveMessage(message: FCMMessage) {
        val collectionId = message.id.toInt()
        val response = collectionAPI.getCollection(collectionId).execute()
        if (response.isSuccessful) {
            response.body()?.let { cjson ->
                val pagingData = collectionsDAO.getLatestCollection()?.let {
                    PagingData(it.total?.plus(1) ?: 1, it.curr ?: 1, it.prev, it.next)
                } ?: PagingData(1, 1, null, null)
                collectionsDAO.createCollection(cjson.toCollectionDB(pagingData))
            }
        }
    }

    override fun sendMessage(message: Message) {
        messagingAPI.sendPushMessage(FCMMessage().apply {
            actionType = message.topic.toString()
            id = (message as Message.CollectionCreate).collectionId.toString()
        }).execute()
    }

}

class CollectionDeletedCommand(context: Context) : FCMCommand(context) {

    private val dependencyGraph = context.dependencyGraph()

    private val messagingAPI = dependencyGraph.messagingAPI

    private val collectionsDAO: CollectionsDAO = dependencyGraph.daoManager.getDao(Contract.TABLE_COLLECTIONS)

    override fun onReceiveMessage(message: FCMMessage) {
        val collectionId = message.id.toInt()
        collectionsDAO.deleteCollectionById(collectionId)
    }

    override fun sendMessage(message: Message) {
        messagingAPI.sendPushMessage(FCMMessage().apply {
            actionType = message.topic.toString()
            id = (message as Message.CollectionDeleted).collectionId.toString()
        }).execute()
    }

}
