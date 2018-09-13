package com.crskdev.photosurfer.data.local.collections

import androidx.room.Entity
import androidx.room.Index
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.data.local.photo.PhotoEntity

/**
 * Created by Cristian Pela on 30.08.2018.
 */
@Entity(
        tableName = Contract.TABLE_COLLECTION_PHOTOS,
        indices = [Index("id")]
)
class CollectionPhotoEntity : PhotoEntity() {
    var currentCollectionId: Int = -1
}