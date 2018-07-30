package com.crskdev.photosurfer.data.remote.auth

import com.crskdev.photosurfer.data.remote.BASE_HOST_AUTHORIZING
import com.crskdev.photosurfer.data.remote.Caller
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Created by Cristian Pela on 30.07.2018.
 */
class OAuth2Authorizer() {

    private val lock = Any()

    private val caller = Caller(BASE_HOST_AUTHORIZING).apply {
        client = OkHttpClient()
    }

    private var authorizingKey: String? = null

    private var oAuthToken: AuthToken? = null

    fun authorizeRequest(request: Request, apiKeys: APIKeys): Request {
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

}