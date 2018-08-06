package com.crskdev.photosurfer.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.HttpUrl
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class RetrofitClient(val networkClient: NetworkClient) {

    companion object {
        val DEFAULT = RetrofitClient(NetworkClient.DEFAULT)
    }

    val retrofit = Retrofit.Builder()
            .baseUrl(HttpUrl.Builder()
                    .scheme("https")
                    .host(BASE_HOST_API)
                    .build())
            .addConverterFactory(MoshiConverterFactory.create(
                    Moshi.Builder().add(KotlinJsonAdapterFactory()).build())
            )
            .client(networkClient.client)
            .build()
}