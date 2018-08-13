package com.crskdev.photosurfer.data.remote.auth

import fi.iki.elonen.NanoHTTPD

/**
 * Created by Cristian Pela on 14.08.2018.
 */
internal class OAuth2RedirectURIServer(hostName: String, port: Int) : NanoHTTPD(hostName, port) {

    override fun serve(session: IHTTPSession): Response {
        val code = session.parms["code"]
        return newFixedLengthResponse("<!DOCTYPE html><html><head><title>Code</title></head><body><code>" +
                "$code</code></body></html>")
    }
}