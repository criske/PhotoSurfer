@file:Suppress("MemberVisibilityCanBePrivate")

package com.crskdev.photosurfer.data.local.photo.external

import android.os.Environment
import android.os.FileObserver
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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

    fun delete(path: String): Boolean =
            File(path).delete().takeIf { true }  // delete from path
                    ?: File(parent, path).delete() // delete from file name

    fun exists(path: String): Boolean =
            File(path).exists()

    private class WeakFileObserver(path: String, mask: Int =
            FileObserver.CREATE.or(FileObserver.DELETE)) : FileObserver(path, mask) {

        private val TAG = WeakFileObserver::class.java.simpleName

        @Volatile
        private var weakListener: SoftReference<Listener>? = null

        private val lock = ReentrantLock()

        fun addWeakListener(listener: Listener) {
            lock.withLock {
                val ref = weakListener?.get()
                Log.d(TAG, "Try to add listener. $listener. Old ref: $ref.")
                if (listener != ref) {
                    Log.d(TAG, "Listener added")
                    weakListener = SoftReference(listener)
                } else {
                    Log.d(TAG, "Listener rejected. Listener is the same")
                }
            }
        }

        override fun onEvent(event: Int, path: String?) {
            val type = when (event) {
                FileObserver.CREATE -> "CREATED"
                FileObserver.DELETE -> "DELETE"
                else -> "UNKNOWN"
            }
            Log.d(TAG, "File Observer photo directory event: $type. Path: $path. Weak ref: ${weakListener?.get()}")
            lock.withLock {
                weakListener?.get()?.onEvent(event, path)
            }
        }

    }

    interface Listener {
        fun onEvent(event: Int, path: String?)
    }

}
