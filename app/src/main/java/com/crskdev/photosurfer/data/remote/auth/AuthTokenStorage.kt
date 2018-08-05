package com.crskdev.photosurfer.data.remote.auth

interface AuthTokenStorage {

    companion object {
        val NONE = object : AuthTokenStorage {
            override fun getToken(): AuthToken? = null

            override fun saveToken(token: AuthToken) = Unit
        }
    }

    fun getToken(): AuthToken?

    fun saveToken(token: AuthToken)

}