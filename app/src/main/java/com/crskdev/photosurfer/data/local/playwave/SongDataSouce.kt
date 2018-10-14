package com.crskdev.photosurfer.data.local.playwave

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import com.crskdev.photosurfer.data.local.ContentResolverDataSource
import java.util.concurrent.TimeUnit

/**
 * Created by Cristian Pela on 14.10.2018.
 */
class SongDataSource(contentResolver: ContentResolver) : ContentResolverDataSource<Song>(
        contentResolver, Config(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        MediaStore.Audio.Media._ID,
        Config.Where("""
            ${MediaStore.Audio.Media.IS_MUSIC} = ?
             AND
            ${MediaStore.Audio.Media.DURATION} >= ?
        """.trimIndent(), "1", TimeUnit.MINUTES.toMillis(2).toString()),
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
                    .sanitize()
}


data class Song(val id: Int,
                val path: String, val title: String, val artist: String,
                val duration: Int) {

    companion object {
        private const val UNKNOWN_ARTIST = "<unknown>"
        private const val MANGLED_TITLE_DELIM = "-"
        private val urlRegex by lazy {
            "(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]\\.[^\\s]{2,})"
                    .toRegex()
        }
    }

    fun sanitize(): Song {
        val (title: String, artist: String) = if (artist == UNKNOWN_ARTIST) {
            this.title.split(MANGLED_TITLE_DELIM)
                    .takeIf { it.size == 2 }
                    ?.let { it[1] to it[0] }
                    ?: this.title to this.artist
        } else this.title to this.artist
        val title1 = title.trim()
                .replaceFirst(urlRegex, "")
                .split(" ").joinToString(" "){
                    it.trim().capitalize()
                }
        val artist1 = artist.trim()
                .replaceFirst(urlRegex, "")
                .split(" ").joinToString(" ") {
                    it.trim().capitalize()
                }
        return copy(title = title1, artist = artist1)
    }

}