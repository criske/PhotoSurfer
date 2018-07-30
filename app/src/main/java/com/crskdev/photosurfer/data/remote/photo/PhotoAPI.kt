package com.crskdev.photosurfer.data.remote.photo

import retrofit2.Call
import retrofit2.http.GET

interface PhotoAPI {

    @GET("photos/random")
    fun getRandom(): Call<PhotoJSON>
}