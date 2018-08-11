package com.crskdev.photosurfer.data.remote.download

import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Created by Cristian Pela on 06.08.2018.
 */
class DownloadInterceptor(private val moshi: Moshi) : Interceptor {

    var progressListener: ProgressListener? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val encodedPathSegments = request.url().encodedPathSegments()
        val isDownloadRequest = encodedPathSegments.let {
            it.size == 3 && it.contains("photos") && it.contains("download")
        }
        val response = chain.proceed(request)
        return if (isDownloadRequest) {
            return if (response.isSuccessful) {
                val downloadUrl = moshi.adapter<DownloadUrl>(DownloadUrl::class.java)
                        .fromJson(response.body()!!.string())!!
                chain.proceed(Request.Builder().url(downloadUrl.url).build())
                        .let {
                            it.newBuilder().body(ProgressResponseBody(it.body(), progressListener)).build()
                        }
            } else {
                response
            }
        } else {
            response
        }
    }

    private class DownloadUrl {
        lateinit var url: String
    }
}