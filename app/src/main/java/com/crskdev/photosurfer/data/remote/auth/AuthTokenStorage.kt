package com.crskdev.photosurfer.data.remote.auth

import android.content.SharedPreferences
import androidx.core.content.edit
import com.crskdev.photosurfer.data.remote.auth.AuthToken.Companion.NONE
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.properties.Delegates

interface AuthTokenStorage {

    interface Listener {
        fun onChange(new: AuthToken?)
    }

    companion object {
        val NONE = object : AuthTokenStorage {
            override fun token(): AuthToken? = null

            override fun saveToken(token: AuthToken) = Unit
        }
    }

    fun token(): AuthToken?

    fun saveToken(token: AuthToken)

    fun hasToken(): Boolean = token() != null

    fun clearToken() {}

    fun addListener(authTokenListener: Listener) {}

    fun removeListener(authTokenListener: Listener) {}

}

abstract class ObservableAuthTokenStorage : AuthTokenStorage {

    private val listeners = CopyOnWriteArrayList<AuthTokenStorage.Listener>()

    override fun addListener(authTokenListener: AuthTokenStorage.Listener) {
        listeners.add(authTokenListener)
        authTokenListener.onChange(token()) // emit on subscribe
    }

    override fun removeListener(authTokenListener: AuthTokenStorage.Listener) {
        listeners.remove(authTokenListener)
    }

    protected fun notifyListeners(new: AuthToken?) {
        listeners.forEach {
            it.onChange(new)
        }
    }
}

class InMemoryAuthTokenStorage : ObservableAuthTokenStorage() {

    private var token: AuthToken by Delegates.observable(NONE) { _, old, new ->
        notifyListeners(new)
    }

    override fun token(): AuthToken? = if (token == NONE) null else token

    override fun saveToken(token: AuthToken) {
        this.token = token
    }

    override fun clearToken() {
        token = NONE
    }
}

class AuthTokenStorageImpl(private val prefs: SharedPreferences) : ObservableAuthTokenStorage() {


    companion object {
        private const val KEY_AUTH_TOKEN = "KEY_AUTH_TOKEN"
        private const val DELIM = "\t"
    }

    init {
        val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == KEY_AUTH_TOKEN) {
                notifyListeners(token())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }


    override fun token(): AuthToken? {
        return prefs.getString(KEY_AUTH_TOKEN, null)?.split(DELIM)
                ?.takeIf { it.size == 6 }
                ?.let {
                    AuthToken(it[0], it[1], it[2], it[3], it[4].toLong(), it[5])
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
                append(DELIM)
                append(token.username)
            })
        }
    }

    override fun hasToken(): Boolean = token() != null

    override fun clearToken() {
        prefs.edit {
            putString(KEY_AUTH_TOKEN, null)
        }
    }
}