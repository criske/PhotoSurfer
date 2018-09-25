package com.crskdev.photosurfer.data.remote.photo

import com.crskdev.photosurfer.data.remote.REQUIRE_AUTH
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface PhotoAPI {

    @GET("photos/random")
    fun getRandom(): Call<PhotoJSON>

    @GET("photos/{id}")
    fun getPhoto(@Path("id")photoId: String): Call<PhotoJSON>

    @GET("photos")
    fun getRandomPhotos(@Query("page") page: Int = 1): Call<List<PhotoJSON>>

    @GET("users/{username}/likes")
    fun getLikedPhotos(@Path("username") username: String, @Query("page") page: Int = 1): Call<List<PhotoJSON>>

    @GET("/search/photos")
    fun getSearchedPhotos(@Query("query") query: String, @Query("page") page: Int = 1): Call<SearchedPhotosJSON>

    @GET("users/{username}/photos")
    fun getUserPhotos(@Path("username") username: String, @Query("page") page: Int = 1): Call<List<PhotoJSON>>

    @GET("photos/{id}/download")
    @Streaming
    fun download(@Path("id") id: String): Call<ResponseBody>

    @POST("photos/{id}/like")
    @Headers(REQUIRE_AUTH)
    fun like(@Path("id") id: String): Call<ResponseBody>

    @DELETE("photos/{id}/like")
    @Headers(REQUIRE_AUTH)
    fun unlike(@Path("id") id: String): Call<ResponseBody>



}