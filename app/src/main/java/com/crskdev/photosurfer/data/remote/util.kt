package com.crskdev.photosurfer.data.remote

import okhttp3.*

/**
 * Created by Cristian Pela on 16.08.2018.
 */
fun errorResponse(request: Request, code: Int,
                  content: String = "",
                  message: String = "",
                  mediaType: MediaType = MediaType.get("text/plain")): Response {
    return Response.Builder().code(code)
            .body(ResponseBody.create(mediaType, content))
            .request(request)
            .message(message)
            .protocol(Protocol.HTTP_1_1)
            .build()
}