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
            MessagingContract.TEST_URL
        } else {
            MessagingContract.REMOTE_URL
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
            fcmTokenProvider.token()?.let {
                val originalRequest = chain.request()
                var urlBuilder = originalRequest.url().newBuilder()
                        .addQueryParameter("token", it)
                val queryUsername = originalRequest.url().queryParameter("username")
                if (queryUsername == null) {
                    val authUsername = authTokenStorage.token()?.username
                    if (authUsername != null) {
                        urlBuilder = urlBuilder.addQueryParameter("username", authUsername)
                    } else {
                        return@let errorResponse(chain.request(),
                                500,
                                "Can't send registration request. Logged username is not present")
                    }
                }
                val requestWithToken = originalRequest.newBuilder()
                        .url(urlBuilder.build())
                        .build()
                chain.proceed(requestWithToken)
            } ?: errorResponse(chain.request(),
                    500,
                    "Can't send registration request. Firebase Messaging token id is not present")

}

interface FCMTokenProvider {
    fun token(): String?
}

class FCMTokeProviderImpl(private val firebaseInstanceId: FirebaseInstanceId) : FCMTokenProvider {
    override fun token(): String? = firebaseInstanceId.token
}


