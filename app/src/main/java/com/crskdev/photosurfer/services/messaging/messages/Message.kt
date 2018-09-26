package com.crskdev.photosurfer.services.messaging.messages

sealed class Message(val topic: Topic) {
    class CollectionCreate(val collectionId: Int) : Message(Topic.COLLECTION_CREATED)
    class CollectionDeleted(val collectionId: Int) : Message(Topic.COLLECTION_DELETED)
    class CollectionEdited(val collectionId: Int) : Message(Topic.COLLECTION_EDITED)
    class CollectionAddedPhoto(val collectionId: Int, val photoId: String) : Message(Topic.COLLECTION_ADDED_PHOTO)
    class CollectionRemovedPhoto(val collectionId: Int, val photoId: String) : Message(Topic.COLLECTION_REMOVED_PHOTO)
    class PhotoLiked(val photoId: String) : Message(Topic.LIKED)
    class PhotoUnliked(val photoId: String) : Message(Topic.UNLIKED)
}