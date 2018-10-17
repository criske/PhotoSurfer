package com.crskdev.photosurfer.services.executors

import android.os.Looper

/**
 * Created by Cristian Pela on 28.08.2018.
 */
abstract class ThreadCallChecker {

    companion object {
        val SUPPRESED_CHECK = object : ThreadCallChecker(){
            override fun isOnMainThread(): Boolean = true

        }
    }

    fun assertOnMainThread(failMessage: String = "Call must be executed on main thread") {
        if (!isOnMainThread()) {
            throw IllegalAccessException(failMessage)
        }
    }

    fun assertOnBackgroundThread(failMessage: String = "Call must be executed on background thread") {
        if (isOnMainThread()) {
            throw IllegalAccessException(failMessage)
        }
    }

    abstract fun isOnMainThread(): Boolean

}


class AndroidThreadCallChecker : ThreadCallChecker() {

    override fun isOnMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()
}