package com.crskdev.photosurfer.data.remote

import com.crskdev.photosurfer.BuildConfig
import com.crskdev.photosurfer.data.remote.auth.APIKeys
import com.crskdev.photosurfer.data.remote.auth.AuthAPI
import com.crskdev.photosurfer.data.remote.auth.AuthToken
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by Cristian Pela on 29.07.2018.
 */
class NetworkClientTest {

    private val networkClient = NetworkClient(
            object : AuthTokenStorage {
                override fun getToken(): AuthToken? = AuthToken("", "", "", "",0L)
                override fun saveToken(token: AuthToken) = Unit
            },
            APIKeys(BuildConfig.ACCESS_KEY, BuildConfig.SECRET_KEY, BuildConfig.REDIRECT_URI)
    )

    private val retrofit = RetrofitClient(networkClient).retrofit

    @Test
    fun testSimplePhotoFetch() {
        networkClient.caller.call("photos", "random")(emptyMap())
                .body()
                ?.string()
                ?.let { println(it) }


        val photoAPI = retrofit.create(PhotoAPI::class.java)

        val photo = photoAPI.getRandom().execute().body()

        println(photo)
    }

    @Test
    fun testAuthentication() {
        val authAPI = retrofit.create(AuthAPI::class.java)

        val execute = authAPI.authorize("cristypela05@gmail.com", "26041985").execute()
        println(execute.errorBody()?.string())
        val auth = execute.body()
        println(auth)
        val b = true

        assertTrue(execute.isSuccessful)
    }
}