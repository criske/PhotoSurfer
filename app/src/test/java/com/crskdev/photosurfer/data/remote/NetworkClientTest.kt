package com.crskdev.photosurfer.data.remote

import com.crskdev.photosurfer.BuildConfig
import com.crskdev.photosurfer.data.remote.auth.*
import com.crskdev.photosurfer.data.remote.photo.PhotoAPI
import com.crskdev.photosurfer.data.remote.user.UserAPI
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by Cristian Pela on 29.07.2018.
 */
class NetworkClientTest {

    private val testAuthTokenStorage: AuthTokenStorage = object : AuthTokenStorage {

        var authToken: AuthToken? = null

        override fun token(): AuthToken? = authToken

        override fun saveToken(token: AuthToken) {
            authToken = token
        }

    }

    private val networkClient = NetworkClient(
            testAuthTokenStorage,
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

        val authResponse = authAPI.authorize("cristypela05@gmail.com", "aa26041985").execute()
        assertTrue(authResponse.isSuccessful)

        val token = authResponse.body()?.toAuthToken()!!
        //testAuthTokenStorage.saveToken(token)

        val userAPI = retrofit.create(UserAPI::class.java)

        val meResponse = userAPI.getMe().execute()

        assertTrue(meResponse.isSuccessful)
        val user = meResponse.body()!!


        val b = true


    }
}