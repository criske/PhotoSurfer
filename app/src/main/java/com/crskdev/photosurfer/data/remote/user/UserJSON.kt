package com.crskdev.photosurfer.data.remote.user

import com.crskdev.photosurfer.entities.ImageType
import com.squareup.moshi.Json
import java.util.*

class UserJSON {
    lateinit var id: String
    @Json(name = "updated_at")
    lateinit var lastUpdated: String
    @Json(name = "username")
    lateinit var userName: String
    @Json(name = "first_name")
    var firstName: String? = null
    @Json(name = "last_name")
    var lastName: String? = null
    @Json(name = "profile_image")
    lateinit var profileImageLinks: EnumMap<ImageType, String>
    @Json(name = "twitter_username")
    var twitterUserName: String? = null
    @Json(name = "portfolio_url")
    var portfolioUrl: String? = null
    var bio: String? = null
    var location: String? = null
    @Json(name = "total_likes")
    var totalLikes: Int = 0
    @Json(name = "total_photos")
    var totalPhotos: Int = 0
    @Json(name = "total_collections")
    var totalCollections: Int = 0
    @Json(name = "followed_by_user")
    var isFollowedByMe: Boolean = false
    @Json(name = "followers_count")
    var followers: Int =0
    @Json(name = "following_count")
    var following: Int =0
    @Json(name = "total_likes")
    var likes: Int = 0
    var downloads: Int = 0
}