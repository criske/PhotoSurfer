package com.crskdev.photosurfer.data.repository

/**
 * Created by Cristian Pela on 14.08.2018.
 */
interface Repository {
    interface Callback<D> {
        fun onSuccess(data: D, extras: Any? = null) = Unit
        fun onError(error: Throwable)
    }
}