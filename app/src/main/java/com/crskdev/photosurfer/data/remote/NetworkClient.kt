@file:Suppress("unused")

package com.crskdev.photosurfer.data.remote

import android.os.Build
import androidx.annotation.RequiresApi
import com.crskdev.photosurfer.BuildConfig
import com.crskdev.photosurfer.data.remote.auth.APIKeys
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.download.DownloadInterceptor
import com.crskdev.photosurfer.data.remote.download.ProgressListener
import com.squareup.moshi.Moshi
import okhttp3.*
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit

/**
 * Created by Cristian Pela on 29.07.2018.
 */
class NetworkClient(tokenStorage: AuthTokenStorage,
                    apiKeys: APIKeys,
                    cookieJar: CookieJar = JavaNetCookieJar(CookieManager().apply {
                        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
                    })) {


    companion object {
        val DEFAULT = NetworkClient(AuthTokenStorage.NONE, APIKeys(BuildConfig.ACCESS_KEY, BuildConfig.SECRET_KEY,
                BuildConfig.REDIRECT_URI))
    }


    @PublishedApi
    internal val downloadInterceptor = DownloadInterceptor(Moshi.Builder().build())

    internal val client = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(UnsplashInterceptor(tokenStorage, apiKeys))
            .addInterceptor(RateLimitInterceptor())
            .addInterceptor(downloadInterceptor)
            .build()

    internal val caller: Caller = Caller(BASE_HOST_API).apply {
        this.client = this@NetworkClient.client
    }


    inline fun addDownloadProgressListener(crossinline listener: (Boolean, Long, Long, Boolean) -> Unit) {
        downloadInterceptor.progressListener = ProgressListener { isStartingValue, bytesRead, contentLength, done ->
            listener(isStartingValue, bytesRead, contentLength, done)
        }
    }

}

class Caller(private val host: String) {

    lateinit var client: OkHttpClient

    @RequiresApi(Build.VERSION_CODES.N)
    fun call(vararg pathSegments: String): (Map<String, String>) -> Response {
        return {
            val httpUrl = HttpUrl.Builder()
                    .scheme("https")
                    .host(host)
                    .addPathSegments(pathSegments.joinToString("/"))
                    .apply {
                        it.forEach { k, v ->
                            addQueryParameter(k, v)
                        }
                    }
                    .build()
            client.newCall(Request.Builder()
                    .url(httpUrl)
                    .build()).execute()
        }
    }

}


