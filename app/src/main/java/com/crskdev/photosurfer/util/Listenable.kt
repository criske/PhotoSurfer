package com.crskdev.photosurfer.util

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by Cristian Pela on 28.08.2018.
 */
open class Listenable<T> {

    private val listeners = CopyOnWriteArrayList<Listener<T>>()

    interface Listener<T> {
        fun onNotified(oldData: T?, newData: T)
    }

    open fun addListener(listener: Listener<T>) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener<T>) {
        listeners.remove(listener)
    }

    protected fun notifyListeners(oldData: T?, newData: T) {
        listeners.forEach {
            it.onNotified(oldData, newData)
        }
    }

}