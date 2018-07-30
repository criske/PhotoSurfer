package com.crskdev.photosurfer.data.remote

import com.crskdev.photosurfer.data.remote.auth.APIKeys
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.auth.OAuth2Authorizer
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

internal class UnsplashInterceptor(private val tokenStorage: AuthTokenStorage,
                                   private val authorizer: OAuth2Authorizer,
                                   private val apiKeys: APIKeys) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        return when {
            requestHas(req, HEADER_KEY_BYPASS) -> chain.proceed(req)
            requestHas(req, HEADER_KEY_AUTHORIZING) -> proceedToAuthorize(chain, req)
            requestHas(req, HEADER_KEY_AUTH) -> tokenStorage.getToken()
                    ?.let { chain.proceed(appendOAuthToken(req, it.access)) }
                    ?: proceedToAuthorize(chain, req)
            else -> chain.proceed(appendClientIdKey(req))
                    ?.takeIf { it.code() != 401 }
                    ?: proceedToAuthorize(chain, req)
        }
    }

    private fun appendClientIdKey(req: Request): Request {
        val httpUrl = req
                .url()
                .newBuilder()
                .addQueryParameter("client_id", apiKeys.accessKey)
                .build()
        return req.newBuilder().url(httpUrl).apply {
            tokenStorage.getToken()?.let {
                header("Authorization", "Bearer ${it.access}")
            }
        }.build()
    }

    private fun proceedToAuthorize(chain: Interceptor.Chain, req: Request): Response =
            chain.proceed(authorizer.authorizeRequest(req, apiKeys)).apply {
                if (isSuccessful) {
                    tokenStorage.saveToken(authorizer.getTokenAndFlush())
                }
            }


    private fun appendOAuthToken(request: Request, token: String): Request =
            request.newBuilder().header("Authorization", "Bearer $token").build()


    private fun requestHas(request: Request, headerCustomKey: String) =
            request.headers(headerCustomKey).isNotEmpty()
}