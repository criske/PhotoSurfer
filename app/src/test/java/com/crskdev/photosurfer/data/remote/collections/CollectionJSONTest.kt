package com.crskdev.photosurfer.data.remote.collections

import com.crskdev.photosurfer.data.remote.RetrofitClient
import org.junit.Assert.*
import org.junit.Test

/**
 * Created by Cristian Pela on 30.08.2018.
 */
class CollectionJSONTest {

    @Test
    fun getCollections() {
        val retrofit = RetrofitClient.DEFAULT.retrofit
        val collectionsAPI = retrofit.create(CollectionsAPI::class.java)
        val response = collectionsAPI.getCollections("criskey").execute()
        assertTrue(response.isSuccessful)
        val collections = response.body()
        assertTrue(collections?.isNotEmpty() == true)

    }
}