package com.crskdev.photosurfer.data.remote.collections

import com.crskdev.photosurfer.data.remote.REQUIRE_AUTH
import com.crskdev.photosurfer.data.remote.photo.PhotoJSON
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

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

    @POST("/collections")
    @Headers(REQUIRE_AUTH)
    fun createCollection(@Field("title") title: String, @Field("description") description: String, @Field("private") private: Boolean):Call<CollectionJSON>
}