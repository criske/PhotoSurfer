package com.crskdev.photosurfer.data.remote.user

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by Cristian Pela on 06.08.2018.
 */
interface UserAPI {

    @GET("users/{userName}")
    fun getPublicProfile(@Path("userName") userName: String): Call<UserJSON>

}