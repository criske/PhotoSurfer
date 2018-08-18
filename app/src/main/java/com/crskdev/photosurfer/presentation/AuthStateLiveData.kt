package com.crskdev.photosurfer.presentation

import com.crskdev.photosurfer.data.remote.auth.AuthToken
import com.crskdev.photosurfer.data.remote.auth.ObservableAuthState
import com.crskdev.photosurfer.util.livedata.SingleLiveEvent

/**
 * Created by Cristian Pela on 17.08.2018.
 */
class AuthStateLiveData(private val observableAuthState: ObservableAuthState) : SingleLiveEvent<String>() {

    private val listener = object : ObservableAuthState.Listener {
        override fun onChange(new: AuthToken?) {
            postValue(new?.username ?: "")
        }
    }

    override fun onActive() {
        observableAuthState.addListener(listener)
    }

    override fun onInactive() {
        observableAuthState.removeListener(listener)
    }
}