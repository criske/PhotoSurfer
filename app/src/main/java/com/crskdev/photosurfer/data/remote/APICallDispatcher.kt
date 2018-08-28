package com.crskdev.photosurfer.data.remote

import com.crskdev.photosurfer.util.Listenable
import com.crskdev.photosurfer.util.ThreadCallChecker
import retrofit2.Call
import retrofit2.Response

/**
 * Created by Cristian Pela on 28.08.2018.
 */
class APICallDispatcher(@PublishedApi internal val threadCallChecker: ThreadCallChecker)
    : Listenable<APICallDispatcher.State>() {

    enum class State {
        EXECUTING, EXECUTED, CANCELED,
    }

    @Volatile
    @PublishedApi
    internal var currentCall: Call<*>? = null

    inline fun <T> execute(apiCall: () -> Call<T>): Response<T> {
        threadCallChecker.assertOnBackgroundThread()
        publishedAPInotifyListeners(State.EXECUTING)
        synchronized(this) {
            currentCall?.cancel()
            currentCall = apiCall()
        }
        @Suppress("UNCHECKED_CAST")
        val response = currentCall!!.execute() as Response<T>
        publishedAPInotifyListeners(State.EXECUTED)
        return response
    }

    operator fun <T> invoke(apiCall: () -> Call<T>): Response<T> = execute(apiCall)


    @Synchronized
    fun cancel() {
        threadCallChecker.assertOnMainThread()
        currentCall
                ?.takeIf { !it.isExecuted }
                ?.let {
                    it.cancel()
                    notifyListeners(State.CANCELED)
                }
    }

    @PublishedApi
    internal fun publishedAPInotifyListeners(state: State) = notifyListeners(state)

}