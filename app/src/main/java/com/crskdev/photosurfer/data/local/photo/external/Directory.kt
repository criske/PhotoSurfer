@file:Suppress("MemberVisibilityCanBePrivate")

package com.crskdev.photosurfer.data.local.photo.external

import android.os.Environment
import android.os.FileObserver
import java.io.File
import java.io.FileNotFoundException
import java.lang.ref.WeakReference

/**
 * Created by Cristian Pela on 16.09.2018.
 */
class ExternalDirectory(val parent: File, val name: String = "PhotoSurfer") {

    private val watcher = WeakFileObserver(getPathString())

    private val photoSurferDir = File(parent, name)

    init {
        watcher.startWatching()
    }

    fun getPathString(): String = File(parent, name).absolutePath

    fun get(): File = photoSurferDir

    /**
     * creates the directory if not exists. NOTE: Make sure the permission for write files is given by user
     * or else will crash
     */
    fun createIfNotExists() {
        if (!photoSurferDir.exists()) {
            val mkdir = photoSurferDir.mkdir()
            if (!mkdir) {
                throw FileNotFoundException("Could not create directory $photoSurferDir")
            }
        }
    }

    fun addWeakListener(listener: (Int, String?) -> Unit) {
        watcher.addWeakListener(object : Listener {
            override fun onEvent(event: Int, path: String?) {
                listener(event, path)
            }
        })
    }

    private class WeakFileObserver(path: String) : FileObserver(path) {

        private var weakListener: WeakReference<Listener>? = null

        fun addWeakListener(listener: Listener) {
            weakListener = WeakReference(listener)
        }

        override fun onEvent(event: Int, path: String?) {
            weakListener?.get()?.onEvent(event, path)
        }

    }

    interface Listener {
        fun onEvent(event: Int, path: String?)
    }

}
