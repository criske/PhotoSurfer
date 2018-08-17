package com.crskdev.photosurfer.presentation

import androidx.lifecycle.LiveData
import com.crskdev.photosurfer.data.remote.auth.AuthToken
import com.crskdev.photosurfer.data.remote.auth.AuthTokenStorage

/**
 * Created by Cristian Pela on 17.08.2018.
 */
class AuthStateLiveData(private val authTokenStorage: AuthTokenStorage) : LiveData<Boolean>() {

    private val listener = object : AuthTokenStorage.Listener {
        override fun onChange(new: AuthToken?) {
            postValue(new != null)
        }
    }

    override fun onActive() {
        authTokenStorage.addListener(listener)
    }

    override fun onInactive() {
        authTokenStorage.removeListener(listener)
    }
}