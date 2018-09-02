package com.crskdev.photosurfer.data.remote.collections

import com.crskdev.photosurfer.data.remote.REQUIRE_AUTH
import com.crskdev.photosurfer.data.remote.photo.PhotoJSON
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by Cristian Pela on 30.08.2018.
 */
interface CollectionsAPI {

    @GET("/users/{username}/collections")
    fun getCollections(@Path("username") username: String, @Query("page") page: Int = 1): Call<List<CollectionJSON>>

    @GET("/users/{username}/collections")
    @Headers(REQUIRE_AUTH)
    fun getMyCollections(@Path("username") username: String, @Query("page") page: Int = 1): Call<List<CollectionJSON>>

    @GET("/collections/{id}/photos")
    @Headers(REQUIRE_AUTH)
    fun getMyCollectionPhotos(@Path("id") collectionId: Int, @Query("page") page: Int = 1): Call<List<PhotoJSON>>
}