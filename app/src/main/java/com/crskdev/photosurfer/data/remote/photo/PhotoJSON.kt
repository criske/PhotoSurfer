package com.crskdev.photosurfer.data.remote.photo

import com.crskdev.photosurfer.data.remote.collections.CollectionJSON
import com.crskdev.photosurfer.data.remote.collections.CollectionLiteJSON
import com.crskdev.photosurfer.entities.ImageType
import com.squareup.moshi.Json
import java.util.*

class PhotoJSON {
    lateinit var id: String
    @Json(name = "created_at")
    var createdAt: String = ""
    @Json(name = "updated_at")
    var updatedAt: String = ""
    var width: Int = 0
    var height: Int = 0
    @Json(name = "color")
    lateinit var colorString: String
    lateinit var urls: EnumMap<ImageType, String>
    var description: String? = null
    @Json(name = "current_user_collections")
    var collections: List<CollectionLiteJSON> = emptyList()
    lateinit var categories: List<String>
    var likes: Long = 0
    @Json(name = "liked_by_user")
    var likedByMe: Boolean = false
    var views: Long = 0
    @Json(name = "user")
    lateinit var author: AuthorJSON
}

class SearchedPhotosJSON {
    lateinit var results: List<PhotoJSON>
}

