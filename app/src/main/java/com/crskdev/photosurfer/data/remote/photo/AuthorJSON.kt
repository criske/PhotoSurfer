package com.crskdev.photosurfer.data.remote.photo

import com.squareup.moshi.Json

class AuthorJSON {
    lateinit var id: String
    lateinit var username: String
    @Json(name = "name")
    lateinit var fullName: String
}