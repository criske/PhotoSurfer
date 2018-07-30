package com.crskdev.photosurfer.data.remote.photo

import com.squareup.moshi.Json

class PhotoJSON {
    lateinit var id: String
    @Json(name = "created_at")
    lateinit var createdAt: String
    @Json(name = "updated_at")
    lateinit var updatedAt: String
    var width: Int = 0
    var height: Int = 0
    @Json(name = "color")
    lateinit var colorString: String
    lateinit var urls: Map<String, String>
    var description: String? = null
    lateinit var categories: List<String>
    var likes: Int = 0
    @Json(name = "liked_by_user")
    var likedByMe: Boolean = false
    var views: Int = 0
    @Json(name = "user")
    lateinit var author: AuthorJSON
}