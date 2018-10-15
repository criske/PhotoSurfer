package com.crskdev.photosurfer.data.local.playwave.song

import android.content.ContentResolver
import androidx.paging.DataSource

/**
 * Created by Cristian Pela on 15.10.2018.
 */
interface SongDAO {

    fun getSongs(filterSearch: String?): DataSource.Factory<Int, Song>

    fun getSongById(id: Int): Song?

}

class SongDAOImpl(private val contentResolver: ContentResolver) : SongDAO {

    override fun getSongs(filterSearch: String?): DataSource.Factory<Int, Song> =
            object : DataSource.Factory<Int, Song>() {
                override fun create(): DataSource<Int, Song> = SongDataSource(contentResolver)
            }

    override fun getSongById(id: Int): Song? {
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

}