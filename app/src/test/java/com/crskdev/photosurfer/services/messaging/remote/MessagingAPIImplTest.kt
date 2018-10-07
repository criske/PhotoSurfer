package com.crskdev.photosurfer.services.messaging.remote

import com.crskdev.photosurfer.data.remote.auth.AuthToken
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.create
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Created by Cristian Pela on 23.09.2018.
 */
class MessagingAPIImplTest {

    private lateinit var api: MessagingAPI

    private val mockedFCMTokenProvider = MockedFCMTokenProvider()

    private val  username = "foo"

    private val authTokenStorage = object: AuthTokenStorage {

        override fun saveToken(token: AuthToken) = Unit

        override fun token(): AuthToken? =
                AuthToken("","","","",0L,username)

    }

    @Before
    fun before() {
        api = messagingRetrofit(true, mockedFCMTokenProvider, authTokenStorage).create()
        api.clear().execute()
    }

    @After
    fun after() {
        api.clear().execute()
    }

    @Test
    fun shouldRegisterDeviceWithToken() {
        api.registerDevice("foo").execute()
        api.obtainUserDevices().execute().body().apply {
            assertEquals("foo", this?.username)
            assertEquals(listOf("token1"), this?.tokens)
        }
        mockedFCMTokenProvider.generateNextToken()
        api.registerDevice("foo").execute()
        api.obtainUserDevices().execute().body().apply {
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