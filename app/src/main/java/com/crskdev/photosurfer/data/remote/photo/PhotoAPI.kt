package com.crskdev.photosurfer.data.remote.photo

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PhotoAPI {

    @GET("photos/random")
    fun getRandom(): Call<PhotoJSON>

    @GET("photos")
    fun getPhotos(@Query("page") page: Int = 1): Call<List<PhotoJSON>>

}