package com.crskdev.photosurfer.data.local.photo

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.crskdev.photosurfer.data.local.BaseDBEntity
import com.crskdev.photosurfer.data.local.Contract
import com.crskdev.photosurfer.entities.CollectionLite
import com.crskdev.photosurfer.entities.User

/**
 * Created by Cristian Pela on 09.08.2018.
 */
@Entity(tableName = Contract.TABLE_PHOTOS)
open class PhotoEntity : BaseDBEntity() {
    @PrimaryKey
    lateinit var id: String

    lateinit var createdAt: String
    lateinit var updatedAt: String
    var width: Int = -1
    var height: Int = -1
    lateinit var colorString: String
    /**
     * unwind link map key-values and concat them with ";"
     */
    //TODO use type converters
    lateinit var urls: String
    var description: String? = null
    /*
    unwind values and concat them with ";"
     */
    //TODO use type converters
    var categories: String? = null
    //TODO use type converters
    lateinit var collections: List<CollectionLite>
    var likes: Int = 0
    var likedByMe: Boolean = false
    var views: Int = 0
    lateinit var authorId: String
    lateinit var authorUsername: String
}


@Entity(tableName = Contract.TABLE_USER_PHOTOS)
class UserPhotoEntity : PhotoEntity()

@Entity(tableName = Contract.TABLE_LIKE_PHOTOS)
class LikePhotoEntity : PhotoEntity()

@Entity(tableName = Contract.TABLE_SEARCH_PHOTOS)
class SearchPhotoEntity : PhotoEntity()


fun PhotoEntity.toUserPhotoEntity(): UserPhotoEntity {
    val dis = this@toUserPhotoEntity
    return UserPhotoEntity().apply {
        id = dis.id
        indexInResponse = dis.indexInResponse

    }
}

