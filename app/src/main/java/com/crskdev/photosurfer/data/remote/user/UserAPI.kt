package com.crskdev.photosurfer.data.remote.user

import com.crskdev.photosurfer.data.remote.REQUIRE_AUTH
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by Cristian Pela on 06.08.2018.
 */
interface UserAPI {

    @GET("users/{userName}")
    fun getPublicProfile(@Path("userName") userName: String): Call<UserJSON>

    @GET("me")
    @Headers(REQUIRE_AUTH)
    fun getMe(): Call<UserJSON>

    @GET("users/{id}")
    fun getUser(@Path("id") id: String): Call<UserJSON>

    @GET("/search/users")
    fun search(@Query("query") query: String, @Query("page") page: Int = 1): Call<SearchUsersJSON>

}