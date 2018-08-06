package com.crskdev.photosurfer.data.remote.user

import com.crskdev.photosurfer.data.remote.NetworkClient
import com.crskdev.photosurfer.data.remote.RetrofitClient
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit

/**
 * Created by Cristian Pela on 06.08.2018.
 */
class UserJSONTest {

    @Test
    fun testUserApi() {
        val retrofit = RetrofitClient(NetworkClient.DEFAULT)
        val api = retrofit.retrofit.create(UserAPI::class.java)
        api.getPublicProfile("tentides").execute().body()?.let {
            println(it)
        }
    }
}