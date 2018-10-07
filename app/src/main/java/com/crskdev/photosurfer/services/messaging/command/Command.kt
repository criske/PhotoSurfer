package com.crskdev.photosurfer.services.messaging.command

import android.content.Context
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.photo.CollectionPhotoDAO
import com.crskdev.photosurfer.data.local.collections.CollectionsDAO
import com.crskdev.photosurfer.data.remote.PagingData
import com.crskdev.photosurfer.entities.*
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

class CollectionEditedCommand(context: Context) : FCMCommand(context) {

    private val collectionsAPI = dependencyGraph.collectionsAPI

    private val daoManager = dependencyGraph.daoManager

    private val collectionsDAO: CollectionsDAO = daoManager.getDao(Contract.TABLE_COLLECTIONS)

    private val transactional = daoManager.transactionRunner()

    override fun onReceiveMessage(message: FCMMessage) {
        val collectionId = message.id.toInt()
        collectionsAPI.getCollection(collectionId).execute()
                .takeIf { it.isSuccessful }
                ?.let { r ->
                    r.body()?.let { cjson ->
                        transactional {
                            collectionsDAO.getCollection(collectionId)?.let { c ->
                                collectionsDAO.updateCollection(c.apply {
                                    this.title = cjson.title
                                    this.description = cjson.description
                                    this.notPublic = cjson.private
                                    this.updatedAt = UNSPLASH_DATE_FORMATTER.formatNow()
                                })
                            }
                        }
                    }
                }
    }

    override fun sendMessage(message: Message) {
        messagingAPI.sendPushMessage(FCMMessage().apply {
            actionType = message.topic.toString()
            id = (message as Message.CollectionEdited).collectionId.toString()
        }).execute()
    }

}

class CollectionAddedPhotoCommand(context: Context) : FCMCommand(context) {

    private val daoManager = dependencyGraph.daoManager

    private val collectionDAO: CollectionsDAO = daoManager.getDao(Contract.TABLE_COLLECTIONS)

    private val transactional = daoManager.transactionRunner()

    private val photoDAOFacade = dependencyGraph.photoDAOFacade

    private val photoAPI = dependencyGraph.photoAPI

    override fun onReceiveMessage(message: FCMMessage) {
        val collectionId = message.id.toInt()
        val photoId = message.extraId
        transactional {
            //update size and cover
            val photoDb = photoDAOFacade.getPhotoFromEitherTable(photoId)
                    ?: photoAPI.getPhoto(photoId).execute().let { r ->
                        if (r.isSuccessful) {
                            val pagingData = photoDAOFacade.getLastPhoto(Contract.TABLE_COLLECTION_PHOTOS)?.let {
                                PagingData(it.total?.plus(1) ?: 1, it.curr ?: 1, it.prev, it.next)
                            } ?: PagingData(1, 1, null, null)
                            r.body()?.toCollectionPhotoDbEntity(pagingData, photoDAOFacade.getNextIndex(Contract.TABLE_COLLECTION_PHOTOS))
                        } else {
                            null
                        }
                    }

            if (photoDb != null) {
                val collectionDB = collectionDAO.getCollection(collectionId)?.apply {
                    totalPhotos += 1
                    coverPhotoId = photoId
                    coverPhotoUrls = photoDb.urls
                    coverPhotoAuthorUsername = photoDb.authorUsername
                    coverPhotoAuthorFullName = photoDb.authorFullName
                    updatedAt = UNSPLASH_DATE_FORMATTER.formatNow()
                }
                collectionDB?.let {
                    collectionDAO.updateCollection(it)
                    photoDAOFacade.addPhotoToCollection(photoId, it.asLite(), photoDb)
                }
            }
        }
    }

    override fun sendMessage(message: Message) {
        val collectionAddedPhotoMsg = message as Message.CollectionAddedPhoto
        messagingAPI.sendPushMessage(FCMMessage().apply {
            actionType = message.topic.toString()
            id = collectionAddedPhotoMsg.collectionId.toString()
            extraId = collectionAddedPhotoMsg.photoId
        }).execute()
    }

}


class CollectionRemovePhotoCommand(context: Context) : FCMCommand(context) {

    private val daoManager = dependencyGraph.daoManager

    private val transactional = daoManager.transactionRunner()

    private val collectionsDAO: CollectionsDAO = daoManager.getDao(Contract.TABLE_COLLECTIONS)

    private val collectionsPhotoDAO: CollectionPhotoDAO = daoManager.getDao(Contract.TABLE_COLLECTION_PHOTOS)

    private val photoDAOFacade = dependencyGraph.photoDAOFacade

    override fun onReceiveMessage(message: FCMMessage) {
        val collectionId = message.id.toInt()
        val photoId = message.extraId
        transactional {
            collectionsDAO.getCollection(collectionId)?.let {
                photoDAOFacade.removePhotoFromCollection(photoId, it.asLite())
            }
            val lastPhoto = collectionsPhotoDAO.getLastPhoto()
            val collectionDB = collectionsDAO.getCollection(collectionId)
            //update cover with latest photo in collection only if current collection id is collection id
            if (lastPhoto?.currentCollectionId == collectionId) {
                lastPhoto.let {
                    collectionDB?.coverPhotoId = it.id
                    collectionDB?.coverPhotoUrls = it.urls
                    collectionDB?.coverPhotoAuthorUsername = it.authorUsername
                    collectionDB?.coverPhotoAuthorFullName = it.authorFullName
                }
            } else if (lastPhoto == null) { // there is no photo in this collection
                collectionDB?.coverPhotoId = null
                collectionDB?.coverPhotoUrls = null
                collectionDB?.coverPhotoAuthorUsername = null
                collectionDB?.coverPhotoAuthorFullName = null
            }
            collectionDB?.let { collectionsDAO.updateCollection(it) }
        }
    }

    override fun sendMessage(message: Message) {
        messagingAPI.sendPushMessage(FCMMessage().apply {
            actionType = message.topic.toString()
            id = (message as Message.CollectionRemovedPhoto).collectionId.toString()
            extraId = message.photoId
        }).execute()
    }

}

class PhotoLikeCommand(context: Context) : FCMCommand(context) {

    private val photoDAOFacade = dependencyGraph.photoDAOFacade

    override fun onReceiveMessage(message: FCMMessage) {
        val photoId = message.id
        photoDAOFacade.like(photoId, true)
        //TODO deal when photo is not found
    }

    override fun sendMessage(message: Message) {
        messagingAPI.sendPushMessage(FCMMessage().apply {
            actionType = message.topic.toString()
            id = (message as Message.PhotoLiked).photoId.toString()
        }).execute()
    }

}

class PhotoUnlikeCommand(context: Context) : FCMCommand(context) {

    private val photoDAOFacade = dependencyGraph.photoDAOFacade

    override fun onReceiveMessage(message: FCMMessage) {
        val photoId = message.id
        photoDAOFacade.like(photoId, false)
    }

    override fun sendMessage(message: Message) {
        messagingAPI.sendPushMessage(FCMMessage().apply {
            actionType = message.topic.toString()
            id = (message as Message.PhotoUnliked).photoId.toString()
        }).execute()
    }

}

