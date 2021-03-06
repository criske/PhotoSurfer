package com.crskdev.photosurfer.data.local.playwave.song

import android.content.ContentResolver
import androidx.paging.DataSource

/**
 * Created by Cristian Pela on 15.10.2018.
 */
interface SongDAO {

    fun getSongs(filterSearch: String?): DataSource.Factory<Int, Song>

    fun getSongById(id: Long): Song?

    fun exists(id: Long): Boolean

}

class SongDAOImpl(private val contentResolver: ContentResolver) : SongDAO {

    override fun getSongs(filterSearch: String?): DataSource.Factory<Int, Song> =
            object : DataSource.Factory<Int, Song>() {
                override fun create(): DataSource<Int, Song> = SongDataSource(contentResolver, filterSearch)
            }

    override fun getSongById(id: Long): Song? {
        return contentResolver
                .query(SONGS_URI, SONGS_PROJECTION, "$SONG_ID=?", arrayOf(id.toString()), null, null)
                ?.use {
                    if (it.moveToFirst()) {
                        toSong(it)
                    } else {
                        null
                    }
                }
    }

    override fun exists(id: Long): Boolean {
        return contentResolver
                .query(SONGS_URI, arrayOf(SONG_ID), "$SONG_ID=?", arrayOf(id.toString()), null, null)
                ?.use {
                    it.moveToLast()
                } ?: false
    }

}