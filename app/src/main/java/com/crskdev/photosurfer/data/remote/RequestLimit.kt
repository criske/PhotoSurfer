package com.crskdev.photosurfer.data.remote

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Call

/**
 * Created by Cristian Pela on 11.08.2018.
 */
class RequestLimit(private val max: Int, private val remaining: Int) {

    class LimitException(message: String) : Throwable(message)

    val isLimitReached = remaining == 0

    companion object {
        fun createFromHeaders(headers: Headers): RequestLimit {
            val max = headers["x-ratelimit-limit"]?.toInt()
                    ?: throw Error("X-Ratelimit-Limit header not found")
            val remaining = headers["x-ratelimit-remaining"]?.toInt()
                    ?: throw Error("X-Ratelimit-Remaining header not found")
            return RequestLimit(max, remaining)
        }
    }

    override fun toString(): String = "${max - remaining}/$max"
}

class RateLimitInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        return try {
            val requestLimit: RequestLimit = RequestLimit.createFromHeaders(response.headers())
            if (requestLimit.isLimitReached) {
                throw RequestLimit.LimitException("Request rate per hour limit reached: $requestLimit")
            } else {
                response
            }
        } catch (ex: Exception) {
            response
        }
    }

}