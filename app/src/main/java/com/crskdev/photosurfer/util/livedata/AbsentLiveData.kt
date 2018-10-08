package com.crskdev.photosurfer.util.livedata

import androidx.lifecycle.LiveData

/**
 * Created by Cristian Pela on 08.10.2018.
 */
class AbsentLiveData<T> private constructor() : LiveData<T>() {
    init {
        postValue(null)
    }

    companion object {
        fun <T> create(): LiveData<T> {
            return AbsentLiveData()
        }
    }
}