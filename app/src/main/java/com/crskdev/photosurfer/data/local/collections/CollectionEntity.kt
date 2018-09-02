package com.crskdev.photosurfer.data.local.collections

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.crskdev.photosurfer.data.local.BaseDBEntity
import com.crskdev.photosurfer.data.local.Contract

/**
 * Created by Cristian Pela on 30.08.2018.
 */
@Entity(
        tableName = Contract.TABLE_COLLECTIONS,
        indices = [Index("id")]
)
class CollectionEntity : BaseDBEntity() {
    @PrimaryKey
    var id: Int = -1
    lateinit var title: String
    lateinit var description: String
    lateinit var publishedAt: String
    lateinit var updatedAt: String
    var curated: Boolean = false
    var totalPhotos: Int = 0
    var private: Boolean = false
    lateinit var sharedKey: String
    var coverPhotoId: String? = null
    var coverPhotoUrls: String? = null
    lateinit var ownerId: String
    lateinit var ownerUsername: String
    lateinit var links: String
}