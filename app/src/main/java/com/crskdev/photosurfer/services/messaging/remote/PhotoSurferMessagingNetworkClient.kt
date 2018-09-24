package com.crskdev.photosurfer.services.messaging.remote

import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage
import com.crskdev.photosurfer.data.remote.errorResponse
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Created by Cristian Pela on 23.09.2018.
 */
object MessagingContract {
    const val REMOTE_URL = "https://us-central1-photosurfer-aa0ff.cloudfunctions.net/"
    const val TEST_URL = "http://localhost:5000/photosurfer-aa0ff/us-central1/"
}

internal fun messagingURL(inTestMode: Boolean = false): String =
        if (inTestMode) {
            MessagingContract.REMOTE_URL
        } else {
            MessagingContract.TEST_URL
        }

fun messagingRetrofit(inTestMode: Boolean, fcmTokenProvider: FCMTokenProvider, authTokenStorage: AuthTokenStorage): Retrofit =
        Retrofit.Builder()
                .baseUrl(messagingURL(inTestMode))
                .addConverterFactory(MoshiConverterFactory.create(
                        Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .build())
                        .asLenient())
                .client(OkHttpClient.Builder()
                        .addInterceptor(FCMTokenInterceptor(fcmTokenProvider, authTokenStorage))
                        .build())
                .build()


class FCMTokenInterceptor(private val fcmTokenProvider: FCMTokenProvider,
                          private val authTokenStorage: AuthTokenStorage) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
            (fcmTokenProvider.token() to authTokenStorage.token())
                    .takeIf {
                        it.first != null && it.second != null
                    }?.let {
                        val originalRequest = chain.request()
                        val (fcmToken, authToken) = it
                        val requestWithToken = originalRequest.newBuilder()
                                .url(originalRequest.url().newBuilder()
                                        .addQueryParameter("username", authToken!!.username)
                                        .addQueryParameter("token", fcmToken!!)
                                        .build())
                                .build()
                        chain.proceed(requestWithToken)
                    }
                    ?: errorResponse(chain.request(), 500, "Device is not registered with a token to Firebase Messaging")
}

interface FCMTokenProvider {
    fun token(): String?
}

class FCMTokeProviderImpl(private val firebaseInstanceId: FirebaseInstanceId) : FCMTokenProvider {
    override fun token(): String? = firebaseInstanceId.token
}


