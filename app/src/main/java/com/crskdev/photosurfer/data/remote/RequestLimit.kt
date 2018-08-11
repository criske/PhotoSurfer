package com.crskdev.photosurfer.data.remote

import okhttp3.Headers

/**
 * Created by Cristian Pela on 11.08.2018.
 */
class RequestLimit(private val max: Int, private val remaining: Int) {

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