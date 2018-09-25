package com.crskdev.photosurfer.services.messaging.remote

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 *
 *functions:
 *+ createCollection: http://localhost:5000/photosurfer-aa0ff/us-central1/createCollection
 *+  functions: registerDevice: http://localhost:5000/photosurfer-aa0ff/us-central1/registerDevice
 *+  functions: obtainUserDevices: http://localhost:5000/photosurfer-aa0ff/us-central1/obtainUserDevices
 *+  functions: unregisterDevice: http://localhost:5000/photosurfer-aa0ff/us-central1/unregisterDevice
 *+  functions: clear: http://localhost:5000/photosurfer-aa0ff/us-central1/clear
 *
 *
 * Created by Cristian Pela on 23.09.2018.
 */
interface MessagingAPI {

    @GET("/registerDevice")
    fun registerDevice(@Query("username") username: String): Call<ResponseBody>

    @GET("/unregisterDevice")
    fun unregisterDevice(@Query("username") username: String): Call<ResponseBody>

    @GET("/obtainUserDevices")
    fun obtainUserDevices(): Call<UserDevices>

    @POST("/sendPushMessage")
    fun sendPushMessage(@Body message: FCMMessage): Call<ResponseBody>

    @GET("/clear")
    fun clear(): Call<ResponseBody>

}

class UserDevices {
    var username: String? = null
    var tokens: List<String> = emptyList()
}

class FCMMessage {
    lateinit var actionType: String
    lateinit var id: String
    var extraId: String = ""
}