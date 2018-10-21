package com.crskdev.photosurfer.presentation.playwave

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.crskdev.photosurfer.data.repository.playwave.PlaywaveRepository
import com.crskdev.photosurfer.services.executors.KExecutor
import com.crskdev.photosurfer.services.playwave.PlaywaveSoundPlayer
import com.crskdev.photosurfer.util.livedata.defaultPageListConfig
import kotlin.math.roundToInt

/**
 * Created by Cristian Pela on 17.10.2018.
 */
class UpsertPlaywaveViewModel(
        private val executor: KExecutor,
        private val playwaveRepository: PlaywaveRepository,
        private val playwaveSoundPlayer: PlaywaveSoundPlayer) : ViewModel() {

    private val searchQueryLiveData: MutableLiveData<String?> = MutableLiveData<String?>().apply {
        value = null
    }

    val foundSongsLiveData: LiveData<PagedList<SongUI>> = Transformations.switchMap(searchQueryLiveData) { query ->
        defaultPageListConfig()
                .let { c ->
                    LivePagedListBuilder<Int, SongUI>(playwaveRepository
                            .getAvailableSongs(query)
                            .mapByPage { p ->
                                p.asSequence().map {
                                    SongUI(it.id, it.path, it.title, it.artist, prettySongDuration(it.duration), it.toString(),
                                            it.duration, it.exists)
                                }.toList()
                            }, c)
                            .setFetchExecutor(executor)
                            .build()
                }
    }

    val playingSongStateLiveData: LiveData<PlayingSongState> = MutableLiveData()

    var selectedSongLiveData: LiveData<SongUI?> = MutableLiveData<SongUI>()

    init {
        playwaveSoundPlayer.setTrackListener(object : PlaywaveSoundPlayer.TrackListener {
            override fun onReady() {
                selectedSong().value?.let {
                    playState().postValue(PlayingSongState.Ready(it))
                }

            }

            override fun onTrack(position: Long) {
                selectedSong().value?.let {
                    val percent = positionPercent(position, it.durationLong)
                    println("$position, ${it.durationLong} $percent")
                    playState().postValue(PlayingSongState.Playing(it, percent, positionDisplay(percent, it.durationLong)))
                }
            }

            override fun complete() {
                selectedSong().value?.let {
                    playState().postValue(PlayingSongState.Completed(it))
                }
            }
        })
    }

    fun search(query: String?) {
        searchQueryLiveData.value = query
    }

    fun clear() {
        removeSelectedSong()
    }

    fun stopSelectedSong() {
        selectedSong().value?.let {
            playState().value = PlayingSongState.Stopped(it)
        }
    }

    fun pauseSelectedSong() {
        selectedSong().value?.let {
            val playState = playState()
            if (playState.value is PlayingSongState.Playing) {
                playState.value = PlayingSongState.Paused(it)
                playwaveSoundPlayer.pause()
            }
        }
    }

    fun playSelectedSong() {
        selectedSong().value?.let {
            playwaveSoundPlayer.play()
        }

    }

    fun skipTo(percent: Int, confirmedToPlayAt: Boolean = false) {
        selectedSong().value?.let {
            playState().value = PlayingSongState.Playing(it, percent, positionDisplay(percent, it.durationLong))
            if (confirmedToPlayAt) {
                playwaveSoundPlayer.seekTo(realPosition(percent, it.durationLong))
            }
        }
    }

    fun selectSong(song: SongUI) {
        selectedSong().value = song
        playState().value = PlayingSongState.Prepare(song)
        playwaveSoundPlayer.load(song.path, song.durationLong)
    }

    fun removeSelectedSong() {
        selectedSong().value = null
        val state = playState()
        state.value = PlayingSongState.None
        playwaveSoundPlayer.unload()
    }

    private fun playState() = (playingSongStateLiveData as MutableLiveData)

    private fun positionPercent(realPosition: Long, duration: Long): Int =
            ((realPosition / duration.toFloat()) * 100).roundToInt()

    private fun positionDisplay(percent: Int, duration: Long): String {
        val realPosition = realPosition(percent, duration)
        return prettySongDuration(realPosition)
    }

    private fun realPosition(percent: Int, duration: Long) = percent * duration

    private fun selectedSong() = (selectedSongLiveData as MutableLiveData)

    override fun onCleared() {
        playwaveSoundPlayer.release()
    }
}

sealed class PlayingSongState(val song: SongUI?) {
    object None : PlayingSongState(null)
    class Prepare(song: SongUI) : PlayingSongState(song)
    class Ready(song: SongUI) : PlayingSongState(song)
    class Stopped(song: SongUI) : PlayingSongState(song)
    class Playing(song: SongUI, val percent: Int, val positionDisplay: String) : PlayingSongState(song)
    class Paused(song: SongUI) : PlayingSongState(song)
    class Completed(song: SongUI) : PlayingSongState(song)
}