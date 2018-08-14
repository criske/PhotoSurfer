package com.crskdev.photosurfer.data.remote.user

import com.crskdev.photosurfer.data.remote.photo.PhotoJSON
import com.squareup.moshi.Json

class UserJSON {
    lateinit var id: String
    @Json(name = "updated_at")
    lateinit var lastUpdated: String
    @Json(name = "username")
    lateinit var userName: String
    @Json(name = "first_name")
    lateinit var firstName: String
    @Json(name = "last_name")
    lateinit var lastName: String
    @Json(name = "profile_image")
    lateinit var profileImageLinks: Map<String, String>
    @Json(name = "twitter_username")
    var twitterUserName: String? = null
    @Json(name = "portfolio_url")
    var portfolioUrl: String? = null
    var bio: String? = null
    var location: String? = null
    @Json(name="total_likes")
    var totalLikes: Int = 0
    @Json(name="total_photos")
    var totalPhotos: Int = 0
    @Json(name="total_collections")
    var totalCollections: Int = 0
    @Json(name="followed_by_user")
    var isFollowedByMe: Boolean = false
    var downloads: Int = 0
    lateinit var photos: List<PhotoJSON>
}