package com.crskdev.photosurfer.data.remote.auth

import android.content.SharedPreferences
import androidx.core.content.edit
import com.crskdev.photosurfer.data.remote.auth.AuthToken.Companion.NONE
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.properties.Delegates

interface AuthTokenStorage {

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


}

interface ObservableAuthState {

    interface Listener {
        fun onChange(new: AuthToken?)
    }

    fun addListener(authStateListener: Listener) {}

    fun removeListener(authStateListener: Listener) {}

    fun notifyListeners(new: AuthToken?)
}

abstract class ObservableAuthTokenStorage : AuthTokenStorage, ObservableAuthState {

    private val listeners = CopyOnWriteArrayList<ObservableAuthState.Listener>()

    override fun addListener(authStateListener: ObservableAuthState.Listener) {
        listeners.add(authStateListener)
        authStateListener.onChange(token()) // emit on subscribe
    }

    override fun removeListener(authStateListener: ObservableAuthState.Listener) {
        listeners.remove(authStateListener)
    }

    override fun notifyListeners(new: AuthToken?) {
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

//
//    private inner class SharedPreferenceChangeListener : SharedPreferences.OnSharedPreferenceChangeListener {
//        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
//            if (key == KEY_AUTH_TOKEN) {
//                notifyListeners(token())
//            }
//        }
//    }

    companion object {
        private const val KEY_AUTH_TOKEN = "KEY_AUTH_TOKEN"
        private const val DELIM = "\t"
    }

    init {

        //NOTE: not using listener - is not called when remove key. looks lis android decides to remove listener
        //even if using a non-anon class instance

//        val prefsListener = SharedPreferenceChangeListener()
//        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }


    override fun token(): AuthToken? {
        return prefs.getString(KEY_AUTH_TOKEN, "")
                ?.split(DELIM)
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
        notifyListeners(token)
    }

    override fun hasToken(): Boolean = token() != null

    override fun clearToken() {
        prefs.edit {
            remove(KEY_AUTH_TOKEN)
        }
        notifyListeners(null)
    }
}