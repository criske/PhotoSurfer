package com.crskdev.photosurfer.data.remote

import com.crskdev.photosurfer.data.remote.auth.APIKeys
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.auth.OAuth2Authorizer
import okhttp3.*

internal class UnsplashInterceptor(private val tokenStorage: AuthTokenStorage,
                                   private val apiKeys: APIKeys) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        return when {
            requestHas(req, HEADER_KEY_BYPASS) -> chain.proceed(req)
            requestHas(req, HEADER_KEY_AUTHORIZING) -> OAuth2Authorizer().authorize(chain, apiKeys)
            requestHas(req, HEADER_KEY_AUTH) -> tokenStorage.token()
                    ?.let {
                        val response = chain.proceed(appendOAuthToken(req, it.access))
                        if (response.code() == 401) {
                            tokenStorage.clearToken()
                        }
                        response
                    }
                    ?: errorResponse(req, 401)
            else -> chain.proceed(appendClientIdKey(req))
        }
    }

    private fun appendClientIdKey(req: Request): Request {
        val httpUrl = req
                .url()
                .newBuilder()
                .addQueryParameter("client_id", apiKeys.accessKey)
                .build()
        return req.newBuilder().url(httpUrl).apply {
            tokenStorage.token()?.let {
                header("Authorization", "Bearer ${it.access}")
            }
        }.build()
    }


    private fun appendOAuthToken(request: Request, token: String): Request =
            request.newBuilder().header("Authorization", "Bearer $token").build()


    private fun requestHas(request: Request, headerCustomKey: String) =
            request.headers(headerCustomKey).isNotEmpty()
}