package com.crskdev.photosurfer.data.local.playwave.song

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import com.crskdev.photosurfer.data.local.ContentResolverDataSource
import com.crskdev.photosurfer.presentation.playwave.prettySongDuration
import java.util.concurrent.TimeUnit

/**
 * Created by Cristian Pela on 14.10.2018.
 */
class SongDataSource(contentResolver: ContentResolver, filterSearch: String? = null) : ContentResolverDataSource<Song>(
        contentResolver, createConfig(filterSearch)) {

    companion object {
        private fun createConfig(filterSearch: String?): Config {
            return filterSearch?.takeIf { it.isNotEmpty() }?.let {
                val like = "%$it%"
                Config(
                        SONGS_URI,
                        SONG_ID,
                        Config.Where("""
                        ${MediaStore.Audio.Media.IS_MUSIC} = ?
                            AND
                        ${MediaStore.Audio.Media.DURATION} >= ?
                            AND
                        (${MediaStore.Audio.Media.TITLE} LIKE ? COLLATE NOCASE
                                OR ${MediaStore.Audio.Media.ARTIST} LIKE ? COLLATE NOCASE)
                    """.trimIndent(),
                                "1",
                                TimeUnit.MINUTES.toMillis(2).toString(),
                                like,
                                like),
                        *SONGS_PROJECTION)
            } ?: Config(
                    SONGS_URI,
                    SONG_ID,
                    Config.Where("""
                        ${MediaStore.Audio.Media.IS_MUSIC} = ?
                            AND
                        ${MediaStore.Audio.Media.DURATION} >= ?
                    """.trimIndent(), "1", TimeUnit.MINUTES.toMillis(2).toString()),
                    *SONGS_PROJECTION)
        }
    }

    override fun convertRow(cursor: Cursor): Song = toSong(cursor)
}


data class Song(val id: Long,
                val albumId: Long,
                val path: String, val title: String, val artist: String,
                val duration: Long,
                val exists: Boolean) {

    companion object {
        private const val UNKNOWN_ARTIST = "<unknown>"
        private const val MANGLED_TITLE_DELIM = "-"
        private val urlRegex by lazy {
            "(https?://(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?://(?:www\\.|(?!www))[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]\\.[^\\s]{2,})"
                    .toRegex()
        }
        val NO_SONG = Song(-1, -1,"", "", "", -1L, false)
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
                .split(" ").joinToString(" ") {
                    it.trim().capitalize()
                }
        val artist1 = artist.trim()
                .replaceFirst(urlRegex, "")
                .split(" ").joinToString(" ") {
                    it.trim().capitalize()
                }
        return copy(title = title1, artist = artist1)
    }

    override fun toString(): String = "$artist - $title (${prettySongDuration(duration)})"
}