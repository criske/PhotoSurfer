package com.crskdev.photosurfer.data.remote.auth

interface AuthTokenStorage {

    fun getToken(): AuthToken?

    fun saveToken(token: AuthToken)

}