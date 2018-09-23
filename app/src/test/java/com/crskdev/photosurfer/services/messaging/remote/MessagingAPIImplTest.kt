package com.crskdev.photosurfer.services.messaging.remote

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Created by Cristian Pela on 23.09.2018.
 */
class MessagingAPIImplTest {

    lateinit var api: MessagingAPI

    private val mockedFCMTokenProvider = MockedFCMTokenProvider()

    @Before
    fun before() {
        api = messagingRetrofit(true, mockedFCMTokenProvider).create(MessagingAPI::class.java)
        api.clear().execute()
    }

    @After
    fun after() {
        api.clear().execute()
    }

    @Test
    fun shouldRegisterDeviceWithToken() {
        val username = "foo"
        api.registerDevice(username).execute()
        api.obtainUserDevices(username).execute().body().apply {
            assertEquals("foo", this?.username)
            assertEquals(listOf("token1"), this?.tokens)
        }
        mockedFCMTokenProvider.generateNextToken()
        api.registerDevice(username).execute()
        api.obtainUserDevices(username).execute().body().apply {
            assertEquals("foo", this?.username)
            assertEquals(listOf("token1", "token2"), this?.tokens)
        }
    }

}

class MockedFCMTokenProvider : FCMTokenProvider {

    private var tokenCaret = 0

    private var currentToken = createToken()

    override fun token(): String? = currentToken

    fun generateNextToken() {
        currentToken = createToken()
    }

    private fun createToken() = "token${++tokenCaret}"

}