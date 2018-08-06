package com.crskdev.photosurfer.data.remote.user

import com.crskdev.photosurfer.data.remote.photo.PhotoJSON
import com.squareup.moshi.Json

/**
 * Created by Cristian Pela on 06.08.2018.
 * {
"id": "pXhwzz1JtQU",
"updated_at": "2016-07-10T11:00:01-05:00",
"username": "jimmyexample",
"first_name": "James",
"last_name": "Example",
"twitter_username": "jimmy",
"portfolio_url": null,
"bio": "The user's bio",
"location": "Montreal, Qc",
"total_likes": 20,
"total_photos": 10,
"total_collections": 5,
"followed_by_user": false,
"downloads": 4321,
"uploads_remaining": 4,
"instagram_username": "james-example",
"location": null,
"email": "jim@example.com",
"links": {
"self": "https://api.unsplash.com/users/jimmyexample",
"html": "https://unsplash.com/jimmyexample",
"photos": "https://api.unsplash.com/users/jimmyexample/photos",
"likes": "https://api.unsplash.com/users/jimmyexample/likes",
"portfolio": "https://api.unsplash.com/users/jimmyexample/portfolio"
}
}
 */

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