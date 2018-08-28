package com.crskdev.photosurfer.util

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by Cristian Pela on 28.08.2018.
 */
open class Listenable<T> {

    private val listeners = CopyOnWriteArrayList<Listener<T>>()

    interface Listener<T> {
        fun onNotified(data: T)
    }

    open fun addListener(listener: Listener<T>) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener<T>) {
        listeners.remove(listener)
    }

    protected fun notifyListeners(data: T) {
        listeners.forEach {
            it.onNotified(data)
        }
    }

}