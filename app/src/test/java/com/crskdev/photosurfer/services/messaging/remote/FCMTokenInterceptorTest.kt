package com.crskdev.photosurfer.services.messaging.remote

import okhttp3.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Created by Cristian Pela on 23.09.2018.
 */
class FCMTokenInterceptorTest {

    @Test
    fun shouldInterceptWhenThereIsMessageToken() {
        val tokenProvider = object : FCMTokenProvider {
            override fun token(): String? = "mocked-token"

        }
        val testingRequest = Request.Builder()
                .url(HttpUrl.parse(MessagingContract.TEST_URL)!!
                        .newBuilder()
                        .addQueryParameter("username", "foo")
                        .build())
                .build()
        val interceptor = FCMTokenInterceptor(tokenProvider)
        val response = interceptor.intercept(MockedChain(testingRequest))
        assertEquals("http://localhost:5000/photosurfer-aa0ff/us-central1/?username=foo&token=mocked-token",
                response.request().url().toString())
    }

    @Test
    fun shouldReturnErrorResponseWhenTokenNotProvided() {
        val tokenProvider = object : FCMTokenProvider {
            override fun token(): String? = null

        }
        val testingRequest = Request.Builder()
                .url(HttpUrl.parse(MessagingContract.TEST_URL)!!
                        .newBuilder()
                        .addQueryParameter("username", "foo")
                        .build())
                .build()
        val interceptor = FCMTokenInterceptor(tokenProvider)
        val response = interceptor.intercept(MockedChain(testingRequest))
        assertEquals(500, response.code())
    }
}

class MockedChain(private val testingRequest: Request) : Interceptor.Chain {

    override fun writeTimeoutMillis(): Int = 0

    override fun call(): Call = throw UnsupportedOperationException()

    override fun proceed(request: Request): Response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_0)
            .code(200)
            .message("OK")
            .build()

    override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this

    override fun connectTimeoutMillis(): Int = 0

    override fun connection(): Connection? = null

    override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this

    override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this

    override fun request(): Request = testingRequest

    override fun readTimeoutMillis(): Int = 0

}