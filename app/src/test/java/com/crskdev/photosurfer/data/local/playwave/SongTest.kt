@file:Suppress("NestedLambdaShadowedImplicitParameter")

package com.crskdev.photosurfer.data.local.playwave

import com.crskdev.photosurfer.data.local.playwave.song.Song
import org.junit.Test

/**
 * Created by Cristian Pela on 14.10.2018.
 */
class SongTest {

    @Test
    fun sanitize() {
        """
            Song(id=20878, path=/storage/emulated/0/Download/Kick Bong - Landscape (Cydelix remix) _ Cosmicleaf.com-vbVMJy5Uewo.mp3, title=Kick Bong - Landscape (Cydelix remix) _ Cosmicleaf.com-vbVMJy5Uewo, artist=<unknown>, duration=289104)
            Song(id=20876, path=/storage/emulated/0/Download/Electribe 101 - Talking With Myself 98 (Canny remix).mp3, title=Electribe 101 - Talking With Myself 98 (Canny remix), artist=<unknown>, duration=428591)
            Song(id=20872, path=/storage/emulated/0/Download/Hamatsuki - You As Ronnie.mp3, title=Hamatsuki - You As Ronnie, artist=<unknown>, duration=405984)
            Song(id=20870, path=/storage/emulated/0/Download/Quadrophenia - Paradise.mp3, title=Quadrophenia - Paradise, artist=<unknown>, duration=278184)
            Song(id=20575, path=/storage/emulated/0/Download/The Starseeds - Heavensairportcoffeeshoprestaurant.mp3, title=The Starseeds - Heavensairportcoffeeshoprestaurant, artist=<unknown>, duration=406872)
            Song(id=3427, path=/storage/emulated/0/Music/music/Étienne De Crécy - When Jack Met Jill.mp3, title=Étienne De Crécy - When Jack Met Jill, artist=<unknown>, duration=335595)
            Song(id=3426, path=/storage/emulated/0/Music/music/[www.fisierulmeu.ro] alexandra ungureanu - lumea viseaza.mp3, title=[www.fisierulmeu.ro] alexandra ungureanu - lumea viseaza, artist=<unknown>, duration=236121)
            Song(id=3425, path=/storage/emulated/0/Music/music/[CHILLHOUSE] Caia-Heavy Weather [Full].mp3, title=[CHILLHOUSE] Caia-Heavy Weather [Full], artist=<unknown>, duration=308349)
            Song(id=3424, path=/storage/emulated/0/Music/music/zzzz_momentscpad.mp3, title=zzzz_momentscpad, artist=<unknown>, duration=384287,)
            Song(id=3423, path=/storage/emulated/0/Music/music/zLusine - Push.mp3, title=zLusine - Push, artist=<unknown>, duration=328882,)
        """.trimIndent().split("\n").forEach { println(songFromString(it).sanitize()) }
    }


    private fun songFromString(s: String): Song {
        fun extract(mark: String) =
                s.indexOf(mark).let {
                    val end = s.indexOf(",", it).takeIf { it != -1 } ?: s.indexOf(")", it)
                    s.substring(it + mark.length, end)
                }
        val id = extract("id=").toInt()
        val path = extract("path=")
        val title = extract("title=")
        val author = extract("artist=")
        val duration = extract("duration=").toInt()
        return Song(id, path, title, author, duration)
    }

}