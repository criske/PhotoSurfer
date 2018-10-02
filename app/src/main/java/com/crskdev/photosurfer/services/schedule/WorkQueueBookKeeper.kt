package com.crskdev.photosurfer.services.schedule

import android.content.Context
import androidx.core.content.edit

/**
 * Created by Cristian Pela on 02.10.2018.
 */
class WorkQueueBookKeeper(context: Context) {

    private val store = context.getSharedPreferences("work_queue", Context.MODE_PRIVATE)

    companion object {
        const val KEY_QUEUE = "KEY_QUEUE"
    }

    fun addToQueue(tag: Tag) {
        store.edit {
            val queue = store.getStringSet(KEY_QUEUE, emptySet())!!
            queue.add(tag.toString())
            putStringSet(KEY_QUEUE, queue)
        }
    }

    fun removeFromQueue(tag: Tag) {
        val queue = store.getStringSet(KEY_QUEUE, emptySet())!!.asSequence().filter {
            it != tag.toString()
        }.toSet()
        store.edit {
            putStringSet(KEY_QUEUE, queue)
        }
    }

    fun getAllWithTagLike(pattern: String): List<String> =
            store.getStringSet(KEY_QUEUE, emptySet())!!.asSequence().filter {
                it.contains(pattern)
            }.toList()

    fun getAllWithTagLike(pattern: Regex): List<String> =
            store.getStringSet(KEY_QUEUE, emptySet())!!.asSequence().filter {
                it.contains(pattern)
            }.toList()

}