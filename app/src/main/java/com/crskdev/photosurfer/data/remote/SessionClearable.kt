package com.crskdev.photosurfer.data.remote

import com.franmontiel.persistentcookiejar.ClearableCookieJar
import java.net.CookieStore

/**
 * Created by Cristian Pela on 26.09.2018.
 */
interface SessionClearable {
    fun clear()
}

//adapters for various cookie storage implementations

class PersistentSessionClearable(private val clearable: ClearableCookieJar) : SessionClearable {
    override fun clear() {
        clearable.clear()
    }
}

class JavaNetSessionClearable(private val clearable: CookieStore) : SessionClearable {
    override fun clear() {
        clearable.removeAll()
    }
}