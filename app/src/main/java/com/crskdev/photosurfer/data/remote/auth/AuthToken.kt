package com.crskdev.photosurfer.data.remote.auth

import com.squareup.moshi.Json

data class AuthToken(val access: String,
                     val type: String,
                     val refresh: String,
                     val scope: String,
                     val createdAt: Long)


class AuthTokenJSON{
    @Json(name = "access_token")
    lateinit var accessToken: String
    @Json(name = "token_type")
    lateinit var tokenType: String
    @Json(name = "refresh_token")
    lateinit var refreshToken: String
    lateinit var scope: String
    @Json(name = "created_at")
    var createdAt: Long = -1L
}
