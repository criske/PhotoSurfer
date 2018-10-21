package com.crskdev.photosurfer.services.schedule

import android.content.Context
import androidx.core.content.edit

interface IWorkQueueBookKeeper {
    fun addToQueue(tag: Tag)
    fun removeFromQueue(tag: Tag)
    fun getAllWithTagLike(pattern: String): List<String>
    fun getAllWithTagLike(pattern: Regex): List<String>
}

/**
 * Created by Cristian Pela on 02.10.2018.
 */
class WorkQueueBookKeeper(context: Context) : IWorkQueueBookKeeper {

    private val store = context.getSharedPreferences("work_queue", Context.MODE_PRIVATE)

    companion object {
        const val KEY_QUEUE = "KEY_QUEUE"
    }

    override fun addToQueue(tag: Tag) {
        store.edit {
            val queue = store.getStringSet(KEY_QUEUE, emptySet())!!
            queue.add(tag.toString())
            putStringSet(KEY_QUEUE, queue)
        }
    }

    override fun removeFromQueue(tag: Tag) {
        val queue = store.getStringSet(KEY_QUEUE, emptySet())!!.asSequence().filter {
            it != tag.toString()
        }.toSet()
        store.edit {
            putStringSet(KEY_QUEUE, queue)
        }
    }

    override fun getAllWithTagLike(pattern: String): List<String> =
            store.getStringSet(KEY_QUEUE, emptySet())!!.asSequence().filter {
                it.contains(pattern)
            }.toList()

    override fun getAllWithTagLike(pattern: Regex): List<String> =
            store.getStringSet(KEY_QUEUE, emptySet())!!.asSequence().filter {
                it.contains(pattern)
            }.toList()

}