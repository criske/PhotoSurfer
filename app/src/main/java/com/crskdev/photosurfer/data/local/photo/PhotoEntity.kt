package com.crskdev.photosurfer.data.local.photo

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.crskdev.photosurfer.data.local.Contract

/**
 * Created by Cristian Pela on 09.08.2018.
 */
@Entity(tableName = Contract.TABLE_PHOTOS)
open class PhotoEntity {
    @PrimaryKey
    lateinit var id: String
    var indexInResponse: Int = -1
    lateinit var createdAt: String
    lateinit var updatedAt: String
    var width: Int = -1
    var height: Int = -1
    lateinit var colorString: String
    /**
     * unwind link map key-values and concat them with ";"
     */
    lateinit var urls: String
    /*
        unwind values and concat them with ";"
     */
    var categories: String? = null
    var likes: Int = 0
    var likedByMe: Boolean = false
    var views: Int = 0
    lateinit var authorId: String
    lateinit var authorUsername: String

    //paging data
    var total: Int? = null
    var curr: Int? = null
    var prev: Int? = null
    var next: Int? = null
}


@Entity(tableName = Contract.TABLE_USER_PHOTOS)
class UserPhotoEntity : PhotoEntity()

@Entity(tableName = Contract.TABLE_LIKE_PHOTOS)
class LikePhotoEntity : PhotoEntity()

@Entity(tableName = Contract.TABLE_SEARCH_PHOTOS)
class SearchPhotoEntity : PhotoEntity()

