package com.crskdev.photosurfer.data.remote.auth


import com.crskdev.photosurfer.data.remote.AUTHORIZING
import com.crskdev.photosurfer.data.remote.LOGIN_FORM_EMAIL
import com.crskdev.photosurfer.data.remote.LOGIN_FORM_PASSWORD
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * Created by Cristian Pela on 13.08.2018.
 */
interface AuthAPI {

    @Headers(AUTHORIZING)
    @GET("login")
    fun authorize(@Query(LOGIN_FORM_EMAIL) email: String, @Query(LOGIN_FORM_PASSWORD) password: String): Call<AuthTokenJSON>

}