package com.crskdev.photosurfer.data.remote

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Call

/**
 * Created by Cristian Pela on 11.08.2018.
 */
class RequestLimit(private val max: Int, private val remaining: Int) {

    val isLimitReached = remaining == 0

    companion object {
        fun createFromHeaders(headers: Headers): RequestLimit {
            val max = headers["x-ratelimit-limit"]?.toInt() ?: Int.MAX_VALUE
            val remaining = headers["x-ratelimit-remaining"]?.toInt() ?: max
            return RequestLimit(max, remaining)
        }
    }

    override fun toString(): String = "${max - remaining}/$max"
}

class RateLimitInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val requestLimit: RequestLimit = RequestLimit.createFromHeaders(response.headers())
        return if (requestLimit.isLimitReached) {
            errorResponse(request, 429, "Request rate per hour limit reached: $requestLimit")
        } else {
            response
        }
    }

}