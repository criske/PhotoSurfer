package com.crskdev.photosurfer.data.remote

/**
 * Created by Cristian Pela on 11.08.2018.
 */
interface ProgressListenerRegistrar {

    var progressListener: ProgressListener?

}

class ProgressListenerRegistrarImpl(private val retrofitClient: RetrofitClient): ProgressListenerRegistrar{

    private val lock = Any()

    override var progressListener: ProgressListener?
        get() = retrofitClient.networkClient.downloadInterceptor.progressListener
        set(value) {
            synchronized(lock){
                if(value!= null)
                    retrofitClient.networkClient.downloadInterceptor.progressListener = value
                else
                    retrofitClient.networkClient.downloadInterceptor.progressListener = null
            }
        }

}