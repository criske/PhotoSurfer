package com.crskdev.photosurfer.data.remote.auth

import okhttp3.OkHttpClient

/**
 * Created by Cristian Pela on 01.08.2018.
 */
class Authorizer(private val apiKeys: APIKeys,
                 private val tokenStorage: AuthTokenStorage,
                 private val client: OkHttpClient) {

    fun hasAuthToken(): Boolean = tokenStorage.getToken() != null

    fun authorize(){

    }

    fun login(email: String, password: String){

    }

    fun obtainAuthToken(){

    }

}