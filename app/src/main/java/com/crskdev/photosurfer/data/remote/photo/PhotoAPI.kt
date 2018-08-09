package com.crskdev.photosurfer.data.remote.photo

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface PhotoAPI {

    @GET("photos/random")
    fun getRandom(): Call<PhotoJSON>

    @GET("photos")
    fun getPhotos(@Query("page") page: Int = 1, @Query("per_page") perPage: Int = 30): Call<List<PhotoJSON>>

    @GET("/photos/{id}/download")
    @Streaming
    fun download(@Path("id") id: String): Call<ResponseBody>

}