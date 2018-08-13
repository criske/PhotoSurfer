package com.crskdev.photosurfer.data.remote.auth

import android.content.SharedPreferences
import android.os.Parcelable
import androidx.core.content.edit
import kotlinx.android.parcel.Parcelize

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

class AuthTokenStorageImpl(private val prefs: SharedPreferences) : AuthTokenStorage {

    companion object {
        private const val KEY_AUTH_TOKEN = "KEY_AUTH_TOKEN"
        private const val DELIM = "\t"
    }

    override fun getToken(): AuthToken? {
        return prefs.getString(KEY_AUTH_TOKEN, null)?.split(DELIM)
                ?.takeIf { it.size == 5 }
                ?.let {
                    AuthToken(it[0], it[1], it[2], it[3], it[4].toLong())
                }
    }

    override fun saveToken(token: AuthToken) {
        prefs.edit {
            putString(KEY_AUTH_TOKEN, buildString {
                append(token.access)
                append(DELIM)
                append(token.type)
                append(DELIM)
                append(token.refresh)
                append(DELIM)
                append(token.scope)
                append(DELIM)
                append(token.createdAt)
            })
        }
    }

}