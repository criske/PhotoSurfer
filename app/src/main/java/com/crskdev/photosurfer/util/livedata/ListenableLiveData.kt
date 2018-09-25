package com.crskdev.photosurfer.util.livedata

import com.crskdev.photosurfer.util.Listenable

/**
 * Created by Cristian Pela on 28.08.2018.
 */
class ListenableLiveData<T>(private val listenable: Listenable<T>): SingleLiveEvent<T>() {

    private val listener = object : Listenable.Listener<T> {
        override fun onNotified(oldData: T?, newData: T) {
            postValue(newData)
        }
    }

    override fun onActive() {
        listenable.addListener(listener)
    }

    override fun onInactive() {
        listenable.removeListener(listener)
    }

}