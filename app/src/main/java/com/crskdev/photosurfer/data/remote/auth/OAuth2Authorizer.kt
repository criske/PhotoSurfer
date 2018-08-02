package com.crskdev.photosurfer.data.remote.auth

import com.crskdev.photosurfer.data.remote.AUTHORIZING
import com.crskdev.photosurfer.data.remote.BASE_HOST_AUTHORIZING
import com.crskdev.photosurfer.data.remote.Caller
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Created by Cristian Pela on 30.07.2018.
 */
class OAuth2Authorizer(apiKeys: APIKeys) {

    private val lock = Any()

    private val client = OkHttpClient()

    private var authorizingKey: String? = null

    private var oAuthToken: AuthToken? = null

    private var step: WorkFlow = WorkFlow.AUTHORIZE

    private val redirectUri = "urn:ietf:wg:oauth:2.0:oob"

    private val scope = listOf(
            "public",
            "read_user",
            "read_photos")
            .joinToString("+")

    private val baseAuthHttpUrl = HttpUrl.Builder()
            .host(BASE_HOST_AUTHORIZING)
            .scheme("https")
            .addPathSegment("oauth")
            .addQueryParameter("client_id", apiKeys.accessKey)
            .addQueryParameter("redirect_uri", redirectUri)
            .build()

    //https://unsplash.com/oauth/authorize?client_id=aae696c42cea4a9562f527d4aa2f0a7cc5c63394a723ccfe7b59c1a2b87a898f&redirect_uri=urn%3Aietf%3Awg%3Aoauth%3A2.0%3Aoob
    // &response_type=code&scope=public+read_user+read_photos

    fun authorizeRequest(request: Request, apiKeys: APIKeys): Request {
//        if (step == WorkFlow.AUTHORIZE) {
//            val authorizeUrl = baseAuthHttpUrl
//                    .newBuilder()
//                    .addQueryParameter("response_type", "code")
//                    .addQueryParameter("scope", scope)
//                    .build()
//            val response = client.newCall(Request.Builder()
//                    .url(authorizeUrl)
//                    .build())
//                    .execute()
//            if(response.isSuccessful){
//                val body = response.body()?.string()
//                if()
//            }else {
//
//            }
//
//        }
        TODO()
    }

    fun getTokenAndFlush(): AuthToken {
        val token = oAuthToken?.copy()
        synchronized(lock) {
            authorizingKey = null
            oAuthToken = null
        }
        return token ?: throw IllegalStateException("Token is not created")
    }


    private enum class WorkFlow {
        AUTHORIZE, LOGIN, GRANT_PERMISSION, TOKEN
    }

}