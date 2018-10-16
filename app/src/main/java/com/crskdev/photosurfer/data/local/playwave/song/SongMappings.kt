package com.crskdev.photosurfer.data.local.playwave.song

import android.database.Cursor
import android.provider.MediaStore

/**
 * Created by Cristian Pela on 15.10.2018.
 */
internal fun toSong(cursor: Cursor, sanitize: Boolean = true): Song =
        Song(cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                true)
                .let {
                    if (sanitize) {
                        it.sanitize()
                    } else {
                        it
                    }
                }

internal val SONG_ID = MediaStore.Audio.Media._ID

internal val SONGS_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

internal val SONGS_PROJECTION = arrayOf(
        SONG_ID,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION
)
