package com.crskdev.photosurfer.data.local

import android.content.ContentResolver
import android.database.ContentObserver
import android.database.Cursor
import android.database.DataSetObserver
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContentResolverCompat
import androidx.paging.PositionalDataSource

/**
 * Created by Cristian Pela on 14.10.2018.
 */
abstract class ContentResolverDataSource<E>(
        private val contentResolver: ContentResolver,
        private val config: Config) : PositionalDataSource<E>() {

    class Config(val uri: Uri, val orderBy: String, val where: Where?, vararg val projection: String?) {
        class Where(val template: String,
                    vararg val args: String)
    }

    private fun countItems(): Int {
        return ContentResolverCompat.query(contentResolver, config.uri,
                arrayOf("COUNT(*) AS count"),
                config.where?.template, config.where?.args,
                null,
                null)
                .use {
                    it.moveToFirst()
                    it.getInt(0)
                }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<E>) {
        val totalCount = countItems()
        if (totalCount == 0) {
            callback.onResult(emptyList(), 0, 0)
            return
        }
        // bound the size requested, based on known count
        val firstLoadPosition = PositionalDataSource.computeInitialLoadPosition(params, totalCount)
        val firstLoadSize = PositionalDataSource.computeInitialLoadSize(params, firstLoadPosition, totalCount)

        val list = loadRange(firstLoadPosition, firstLoadSize)
        if (list != null && list.size == firstLoadSize) {
            callback.onResult(list, firstLoadPosition, totalCount)
        } else {
            // null list, or size doesn't match request - DB modified between count and load
            invalidate()
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<E>) {
        val list = loadRange(params.startPosition, params.loadSize)
        if (list != null) {
            callback.onResult(list)
        } else {
            invalidate()
        }
    }

    private fun loadRange(startPosition: Int, loadCount: Int): List<E>? {
        return ContentResolverCompat.query(contentResolver, config.uri,
                config.projection,
                config.where?.template, config.where?.args,
                " ${config.orderBy} DESC LIMIT $loadCount OFFSET $startPosition", null
        ).use {
            val list = mutableListOf<E>()
            val readCursor = ReadCursor(it)
            if (it.moveToFirst()) {
                do {
                    list.add(convertRow(readCursor))
                } while (it.moveToNext())
            }
            list
        }
    }

    abstract fun convertRow(cursor: Cursor): E


    private class ReadCursor(cursor: Cursor) : Cursor by cursor {

        private val exception = UnsupportedOperationException("With this cursor, you are only allowed to read data!")

        override fun close() {
            throw exception
        }

        override fun moveToFirst(): Boolean {
            throw exception
        }

        override fun setNotificationUri(cr: ContentResolver?, uri: Uri?) {
            throw exception
        }

        override fun setExtras(extras: Bundle?) {
            throw exception
        }

        override fun moveToPosition(position: Int): Boolean {
            throw exception
        }

        override fun moveToPrevious(): Boolean {
            throw exception
        }

        override fun unregisterContentObserver(observer: ContentObserver?) {
            throw exception
        }

        override fun requery(): Boolean {
            throw exception
        }

        override fun registerDataSetObserver(observer: DataSetObserver?) {
            throw exception
        }

        override fun moveToNext(): Boolean {
            throw exception
        }

        override fun registerContentObserver(observer: ContentObserver?) {
            throw exception
        }

        override fun moveToLast(): Boolean {
            throw exception
        }

        override fun deactivate() {
            throw exception
        }

        override fun move(offset: Int): Boolean {
            throw exception
        }

        override fun respond(extras: Bundle?): Bundle {
            throw exception
        }

        override fun unregisterDataSetObserver(observer: DataSetObserver?) {
            throw exception
        }
    }
}
