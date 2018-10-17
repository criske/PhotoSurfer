@file:Suppress("NestedLambdaShadowedImplicitParameter")

package com.crskdev.photosurfer.data.repository.playwave

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.crskdev.photosurfer.data.local.playwave.song.Song
import com.crskdev.photosurfer.data.repository.Repository
import com.crskdev.photosurfer.entities.Playwave
import com.crskdev.photosurfer.entities.PlaywavePhoto
import java.util.concurrent.TimeUnit

/**
 * Created by Cristian Pela on 16.10.2018.
 */
class MockPlaywaveRepository : PlaywaveRepository {

    private val songs = """
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
        """.trimIndent().split("\n").map { songFromString(it).sanitize() }

    private var playwaves = listOf(
            Playwave(1, "Play wave test title", 2, Song(
                    1, "Foo", "Some Title Song", "DJ NaN", TimeUnit.MINUTES.toMillis(5), true
            ), emptyList()),
            Playwave(2, "Play wave test title", 0, Song(
                    1, "Foo", "Some Title Song", "DJ NaN", 2342429, false
            ), emptyList()))

    private val playwavesLiveData = MutableLiveData<List<Playwave>>().apply {
        postValue(playwaves)
    }

    private fun songFromString(s: String): Song {
        fun extract(mark: String) =
                s.indexOf(mark).let {
                    val end = s.indexOf(",", it).takeIf { it != -1 } ?: s.indexOf(")", it)
                    s.substring(it + mark.length, end)
                }

        val id = extract("id=").toLong()
        val path = extract("path=")
        val title = extract("title=")
        val author = extract("artist=")
        val duration = extract("duration=").toLong()
        return Song(id, path, title, author, duration, true)
    }


    override fun getAvailableSongs(filterSearch: String?): DataSource.Factory<Int, Song> =
            object : DataSource.Factory<Int, Song>() {
                override fun create(): DataSource<Int, Song> =
                        object : PositionalDataSource<Song>() {

                            private fun filter(filterSearch: String?): List<Song> {
                                return filterSearch?.let {
                                    songs.filter {
                                        it.artist.contains(filterSearch, true) ||
                                                it.title.contains(filterSearch, true)
                                    }
                                } ?: songs
                            }

                            override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Song>) {
                                val out = filter(filterSearch)
                                callback.onResult(out.subList(params.startPosition, params.loadSize))
                            }

                            override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Song>) {
                                val out = filter(filterSearch)
                                val firstLoadPosition = PositionalDataSource.computeInitialLoadPosition(params, out.size)
                                val firstLoadSize = PositionalDataSource.computeInitialLoadSize(params, firstLoadPosition, out.size)
                                callback.onResult(out.subList(firstLoadPosition, firstLoadSize), params.requestedStartPosition, out.size)
                            }
                        }

            }

    override fun getPlaywaves(includePhotos: Boolean): LiveData<List<Playwave>> {
        return playwavesLiveData
    }

    override fun getPlaywave(playwaveId: Int): LiveData<Playwave> = Transformations.map(playwavesLiveData) { l ->
        l.first { it.id == playwaveId }
    }

    override fun createPlaywave(playwave: Playwave, callback: Repository.Callback<Playwave>?) {
        playwaves += playwave
        playwavesLiveData.postValue(playwaves.sortedBy { it.id })
    }

    override fun updatePlaywave(playwave: Playwave, callback: Repository.Callback<Playwave>?) {
        playwaves.firstOrNull { it.id == playwave.id }?.let {
            playwaves -= it
            playwaves += playwave
        }
        playwavesLiveData.postValue(playwaves.sortedBy { it.id })
    }

    override fun deletePlaywave(playwave: Playwave, callback: Repository.Callback<Playwave>?) {
        playwaves.firstOrNull { it.id == playwave.id }?.let {
            playwaves -= it
        }
        playwavesLiveData.postValue(playwaves.sortedBy { it.id })
    }

    override fun addPhotoToPlaywave(playwaveId: Int, photo: PlaywavePhoto, callback: Repository.Callback<Unit>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removePhotoFromPlaywave(playwaveId: Int, photo: PlaywavePhoto, callback: Repository.Callback<Unit>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}