package com.crskdev.photosurfer.data.local.playwave

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import com.crskdev.photosurfer.data.local.ContentResolverDataSource

/**
 * Created by Cristian Pela on 14.10.2018.
 */
class SongDataSource(contentResolver: ContentResolver) : ContentResolverDataSource<Song>(
        contentResolver, Config(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        MediaStore.Audio.Media._ID,
        null,
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION)) {

    override fun convertRow(cursor: Cursor): Song =
            Song(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)))
}


data class Song(val id: Int,
                val path: String, val title: String, val artist: String,
                val duration: Int)